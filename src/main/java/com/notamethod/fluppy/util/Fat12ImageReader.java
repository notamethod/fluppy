package com.notamethod.fluppy.util;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.*;

/**
 * Parseur FAT12 minimal pour images disquette DOS (.img, .ima).
 * Lit le boot sector, la table FAT et les répertoires (racine + sous-dossiers)
 * pour lister les fichiers exécutables (.EXE, .COM, .BAT).
 * <p>
 * Aucune dépendance externe. Lecture seule.
 */
public class Fat12ImageReader implements AutoCloseable {

    private static final Set<String> EXECUTABLE_EXTENSIONS = new HashSet<>(Arrays.asList("EXE", "COM", "BAT"));

    /**
     * Représente un fichier trouvé dans l'image.
     */
    public record DosFile(String fullPath, String name, String extension, long sizeBytes, int firstCluster) {
        @Override
        public String toString() {
            return fullPath + "  (" + sizeBytes + " octets)";
        }
    }

    // --- Champs issus du boot sector ---
    private int bytesPerSector;
    private int sectorsPerCluster;
    private int reservedSectors;
    private int numFats;
    private int maxRootDirEntries;
    private int totalSectors;
    private int sectorsPerFat;
    private int sectorsPerTrack; // non utilisé pour le parsing logique, gardé pour info
    private int numHeads;

    private long fatStartByte;
    private long rootDirStartByte;
    private long dataStartByte; // début de la zone de données (cluster 2)

    private byte[] fatTable; // table FAT en mémoire (décompactée en accès facile)
    private final RandomAccessFile raf;

    public Fat12ImageReader(Path imgPath) throws IOException {
        this.raf = new RandomAccessFile(imgPath.toFile(), "r");
        readBootSector();
        readFat();
    }

    private void readBootSector() throws IOException {
        byte[] sector0 = new byte[512];
        raf.seek(0);
        raf.readFully(sector0);

        ByteBuffer bb = ByteBuffer.wrap(sector0).order(ByteOrder.LITTLE_ENDIAN);

        bytesPerSector = bb.getShort(11) & 0xFFFF;
        sectorsPerCluster = sector0[13] & 0xFF;
        reservedSectors = bb.getShort(14) & 0xFFFF;
        numFats = sector0[16] & 0xFF;
        maxRootDirEntries = bb.getShort(17) & 0xFFFF;
        totalSectors = bb.getShort(19) & 0xFFFF;
        if (totalSectors == 0) {
            totalSectors = bb.getInt(32); // cas FAT32-like / grandes images, rare pour disquette
        }
        sectorsPerFat = bb.getShort(22) & 0xFFFF;
        sectorsPerTrack = bb.getShort(24) & 0xFFFF;
        numHeads = bb.getShort(26) & 0xFFFF;

        if (bytesPerSector == 0 || sectorsPerCluster == 0) {
            throw new IOException("Boot sector invalide : ce fichier ne semble pas être une image FAT12 valide.");
        }

        fatStartByte = (long) reservedSectors * bytesPerSector;

        long rootDirStartSector = reservedSectors + (long) numFats * sectorsPerFat;
        rootDirStartByte = rootDirStartSector * bytesPerSector;

        int rootDirSectors = ((maxRootDirEntries * 32) + (bytesPerSector - 1)) / bytesPerSector;
        long dataStartSector = rootDirStartSector + rootDirSectors;
        dataStartByte = dataStartSector * bytesPerSector;
    }

    private void readFat() throws IOException {
        int fatSizeBytes = sectorsPerFat * bytesPerSector;
        fatTable = new byte[fatSizeBytes];
        raf.seek(fatStartByte);
        raf.readFully(fatTable);
    }

    /**
     * Lit la valeur de l'entrée FAT12 pour un cluster donné (12 bits packés).
     */
    private int getFatEntry(int cluster) {
        int offset = cluster + (cluster / 2); // cluster * 1.5
        int value;
        if (cluster % 2 == 0) {
            // 12 bits bas du premier octet + 4 bits bas du second
            value = (fatTable[offset] & 0xFF) | ((fatTable[offset + 1] & 0x0F) << 8);
        } else {
            // 4 bits hauts du premier octet + 8 bits du second
            value = ((fatTable[offset] & 0xF0) >> 4) | ((fatTable[offset + 1] & 0xFF) << 4);
        }
        return value;
    }

    private static boolean isEndOfChain(int fatEntry) {
        return fatEntry >= 0xFF8;
    }

    private static boolean isFreeCluster(int fatEntry) {
        return fatEntry == 0x000;
    }

    private static boolean isBadCluster(int fatEntry) {
        return fatEntry == 0xFF7;
    }

    /**
     * Lit le contenu complet d'une chaîne de clusters, en suivant la FAT.
     */
    private byte[] readClusterChain(int startCluster) throws IOException {
        int clusterSizeBytes = sectorsPerCluster * bytesPerSector;
        List<byte[]> chunks = new ArrayList<>();
        int cluster = startCluster;
        int safetyLimit = 65536; // évite une boucle infinie sur image corrompue

        while (cluster >= 2 && !isEndOfChain(cluster) && safetyLimit-- > 0) {
            if (isFreeCluster(cluster) || isBadCluster(cluster)) {
                break; // chaîne corrompue, on s'arrête proprement
            }
            long offset = dataStartByte + (long) (cluster - 2) * clusterSizeBytes;
            byte[] data = new byte[clusterSizeBytes];
            raf.seek(offset);
            raf.readFully(data);
            chunks.add(data);
            cluster = getFatEntry(cluster);
        }

        byte[] result = new byte[chunks.size() * clusterSizeBytes];
        int pos = 0;
        for (byte[] chunk : chunks) {
            System.arraycopy(chunk, 0, result, pos, chunk.length);
            pos += chunk.length;
        }
        return result;
    }

    /**
     * Scanne l'image entière et retourne la liste des exécutables trouvés
     * (récursif dans tous les sous-dossiers).
     */
    public List<DosFile> findExecutables() throws IOException {
        List<DosFile> results = new ArrayList<>();
        byte[] rootDirRaw = new byte[maxRootDirEntries * 32];
        raf.seek(rootDirStartByte);
        raf.readFully(rootDirRaw);

        scanDirectory(rootDirRaw, "", results);
        return results;
    }

    /**
     * Parcourt un buffer de répertoire (32 octets par entrée), collecte les
     * exécutables, et descend récursivement dans les sous-dossiers.
     */
    private void scanDirectory(byte[] dirData, String pathPrefix, List<DosFile> results) throws IOException {
        int entryCount = dirData.length / 32;

        for (int i = 0; i < entryCount; i++) {
            int base = i * 32;
            int firstByte = dirData[base] & 0xFF;

            if (firstByte == 0x00) {
                break; // fin du répertoire
            }
            if (firstByte == 0xE5) {
                continue; // entrée supprimée
            }

            int attributes = dirData[base + 11] & 0xFF;
            boolean isVolumeLabel = (attributes & 0x08) != 0;
            boolean isDirectory = (attributes & 0x10) != 0;
            boolean isLongNamePart = (attributes & 0x0F) == 0x0F; // entrée LFN, on l'ignore (on reste en 8.3)

            if (isVolumeLabel || isLongNamePart) {
                continue;
            }

            String rawName = new String(dirData, base, 8).trim();
            String rawExt = new String(dirData, base + 8, 3).trim();

            if (rawName.isEmpty() || rawName.equals(".") || rawName.equals("..")) {
                continue; // entrées spéciales "." et ".."
            }

            int firstCluster = ((dirData[base + 27] & 0xFF) << 8) | (dirData[base + 26] & 0xFF);
            long fileSize = ((long) (dirData[base + 31] & 0xFF) << 24)
                    | ((dirData[base + 30] & 0xFF) << 16)
                    | ((dirData[base + 29] & 0xFF) << 8)
                    | (dirData[base + 28] & 0xFF);

            String entryName = rawExt.isEmpty() ? rawName : rawName + "." + rawExt;
            String fullPath = pathPrefix + "\\" + entryName;

            if (isDirectory) {
                if (firstCluster >= 2) {
                    byte[] subDirData = readClusterChain(firstCluster);
                    scanDirectory(subDirData, fullPath, results);
                }
            } else {
                if (EXECUTABLE_EXTENSIONS.contains(rawExt.toUpperCase())) {
                    results.add(new DosFile(fullPath, rawName, rawExt.toUpperCase(), fileSize, firstCluster));
                }
            }
        }
    }

    // --- Heuristique de sélection du "meilleur" exécutable à lancer ---

    /**
     * Noms qui désignent presque toujours un installeur, un désinstalleur ou un
     * utilitaire annexe plutôt que le jeu lui-même. On les pénalise fortement.
     */
    private static final Set<String> LOW_PRIORITY_NAMES = new HashSet<>(Arrays.asList(
            "INSTALL", "SETUP", "UNINSTAL", "UNINST", "CONFIG", "SETSOUND",
            "READTHIS", "MOVE", "INSTALL1", "INSTALL2", "DISKCOPY", "FORMAT"
    ));

    /**
     * Noms classiques d'utilitaires DOS souvent présents sur une disquette
     * sans être le jeu (copies de COMMAND.COM, pilotes, etc.).
     */
    private static final Set<String> SYSTEM_NAMES = new HashSet<>(Arrays.asList(
            "COMMAND", "DOS", "HIMEM", "EMM386", "MOUSE", "SHARE", "SMARTDRV"
    ));

    /**
     * Calcule un score heuristique pour un exécutable : plus le score est élevé,
     * plus il est probable que ce soit le programme principal du jeu à lancer.
     * <p>
     * Critères, du plus au moins déterminant :
     * - extension .EXE/.COM privilégiée sur .BAT (un .BAT est souvent un simple lanceur,
     * mais peut aussi être LE point d'entrée attendu — donc pénalité légère, pas exclusion)
     * - nom correspondant au dossier parent (convention fréquente : DOOM\DOOM.EXE)
     * - nom NE figurant PAS dans la liste des installeurs/utilitaires connus
     * - profondeur faible dans l'arborescence (le jeu est souvent à la racine du dossier)
     * - taille de fichier plus grande (un installeur est parfois un petit stub,
     * le jeu principal est généralement le plus gros binaire)
     */
    private int scoreExecutable(DosFile file) {
        int score = 0;

        String nameUpper = file.name().toUpperCase();
        String extUpper = file.extension().toUpperCase();

        // Extension
        if (extUpper.equals("EXE")) score += 30;
        else if (extUpper.equals("COM")) score += 20;
        else if (extUpper.equals("BAT")) score += 10;

        // Pénalité si nom d'installeur/utilitaire connu
        if (LOW_PRIORITY_NAMES.contains(nameUpper)) score -= 50;
        if (SYSTEM_NAMES.contains(nameUpper)) score -= 40;

        // Bonus si le nom du fichier correspond au nom du dossier parent
        // (ex: \DOOM\DOOM.EXE), convention très répandue dans les jeux DOS
        String[] pathParts = file.fullPath().split("\\\\");
        if (pathParts.length >= 2) {
            String parentDir = pathParts[pathParts.length - 2];
            if (!parentDir.isEmpty() && parentDir.equalsIgnoreCase(nameUpper)) {
                score += 25;
            }
        }

        // Pénalité de profondeur : un fichier loin dans l'arborescence
        // (utilitaires, docs annexes) est moins probable comme point d'entrée
        int depth = (int) file.fullPath().chars().filter(c -> c == '\\').count();
        score -= depth * 5;

        // Bonus léger à la taille (gros binaire = plus probablement le jeu,
        // pas un petit stub d'installeur). Plafonné pour ne pas dominer le score.
        score += (int) Math.min(file.sizeBytes() / 10_000, 15);

        return score;
    }

    /**
     * Retourne tous les exécutables triés du plus probable au moins probable
     * pour être le programme principal du jeu (meilleur candidat en premier).
     */
    public List<DosFile> findExecutablesSorted() throws IOException {
        List<DosFile> executables = findExecutables();
        executables.sort((a, b) -> Integer.compare(scoreExecutable(b), scoreExecutable(a)));
        return executables;
    }

    /**
     * Retourne le meilleur candidat unique pour lancer le jeu, ou {@code null}
     * si l'image ne contient aucun exécutable.
     */
    public DosFile findBestExecutable() throws IOException {
        List<DosFile> sorted = findExecutablesSorted();
        return sorted.isEmpty() ? null : sorted.get(0);
    }

    @Override
    public void close() throws IOException {
        raf.close();
    }


    // --- Petit point d'entrée pour test manuel ---
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: java Fat12ImageReader <fichier.img>");
            return;
        }
        try (Fat12ImageReader reader = new Fat12ImageReader(Path.of(args[0]))) {
            List<DosFile> sorted = reader.findExecutablesSorted();
            if (sorted.isEmpty()) {
                System.out.println("Aucun exécutable trouvé.");
            } else {
                System.out.println(sorted.size() + " exécutable(s) trouvé(s), du plus au moins probable :");
                for (DosFile f : sorted) {
                    System.out.println("  " + f);
                }
                System.out.println();
                System.out.println("Meilleur candidat : " + sorted.get(0).fullPath());
            }
        }
    }
}
