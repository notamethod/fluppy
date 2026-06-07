package com.notamethod.fluppy.gui;

import com.notamethod.fluppy.core.Configuration;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Gère le décodage vidéo via FFmpeg et l'affichage dans un ImageView JavaFX.
 * Indépendant de Application : peut être utilisé dans n'importe quelle classe JavaFX.
 *
 * Usage minimal :
 *   FFmpegVideoPlayer player = new FFmpegVideoPlayer("video.mp4", 30);
 *   player.start(width, height);
 *   root.getChildren().add(0, player.getImageView()); // en arrière-plan
 *
 *   // Sur resize :
 *   player.scheduleResize(newW, newH);
 *
 *   // À la fermeture :
 *   player.stop();
 */
public class FFmpegVideoPlayer {

    private final String videoPath;
    private final int fps;

    private Process ffmpegProcess;
    private WritableImage writableImage;
    private PixelWriter pixelWriter;
    private final ImageView imageView;

    private final AtomicBoolean resizePending = new AtomicBoolean(false);
    private Thread readerThread;

    // ----------------------------------------------------------------
    // Constructeur
    // ----------------------------------------------------------------

    public FFmpegVideoPlayer(String videoPath, int fps) {
        this.videoPath = videoPath;
        this.fps = fps;
        this.imageView = new ImageView();
        this.imageView.setPreserveRatio(false);
    }

    // ----------------------------------------------------------------
    // API publique
    // ----------------------------------------------------------------

    /** Retourne l'ImageView à insérer dans le scenegraph. */
    public ImageView getImageView() {
        return imageView;
    }

    /** Démarre la lecture à la résolution donnée. */
    public void start(int width, int height) {
        startFFmpeg(Math.max(width, 1), Math.max(height, 1));
    }

    /**
     * Planifie un redémarrage avec anti-rebond de 300 ms.
     * À appeler depuis les listeners de resize de la Scene.
     */
    public void scheduleResize(int w, int h) {
        resizePending.set(true);
        Thread debounce = new Thread(() -> {
            try {
                Thread.sleep(300);
                if (resizePending.compareAndSet(true, false)) {
                    Platform.runLater(() -> restartFFmpeg(w, h));
                }
            } catch (InterruptedException ignored) {
            }
        });
        debounce.setDaemon(true);
        debounce.start();
    }

    /** Arrête proprement FFmpeg et le thread de lecture. */
    public void stop() {
        stopFFmpeg();
    }

    // ----------------------------------------------------------------
    // Implémentation interne
    // ----------------------------------------------------------------

    private void restartFFmpeg(int w, int h) {
        stopFFmpeg();
        startFFmpeg(Math.max(w, 1), Math.max(h, 1));
    }

    private void startFFmpeg(int w, int h) {
        int bytesPerFrame = w * h * 3;
        String command=null;
        try {
             command= Configuration.getFFMpeg();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    command,
                    "-stream_loop", "-1",
                    "-i", videoPath,
                    "-vf", "scale=" + w + ":" + h,
                    "-r", String.valueOf(fps),
                    "-f", "rawvideo",
                    "-pix_fmt", "bgr24",
                    "pipe:1"
            );
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            ffmpegProcess = pb.start();
            System.out.println("[FFmpeg] Processus démarré, PID : " + ffmpegProcess.pid());
            writableImage = new WritableImage(w, h);
            pixelWriter = writableImage.getPixelWriter();
            imageView.setImage(writableImage);
            imageView.setFitWidth(w);
            imageView.setFitHeight(h);

            InputStream stream = ffmpegProcess.getInputStream();
            byte[] frameBuffer = new byte[bytesPerFrame];

            readerThread = new Thread(() -> {
                try {
                    System.out.println("[FFmpeg] Reader thread démarré, bytesPerFrame=" + bytesPerFrame);
                    while (!Thread.currentThread().isInterrupted()) {
                        int offset = 0;
                        while (offset < bytesPerFrame) {
                            int read = stream.read(frameBuffer, offset, bytesPerFrame - offset);
                            if (read == -1) return;
                            offset += read;
                        }

                        byte[] bgra = bgrToBgra(frameBuffer, w, h);

                        Platform.runLater(() ->
                                pixelWriter.setPixels(
                                        0, 0, w, h,
                                        PixelFormat.getByteBgraPreInstance(),
                                        bgra, 0, w * 4
                                )
                        );
                    }
                } catch (Exception ignored) {
                }
            });
            readerThread.setDaemon(true);
            readerThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopFFmpeg() {
        if (readerThread != null) {
            readerThread.interrupt();
            readerThread = null;
        }
        if (ffmpegProcess != null) {
            ffmpegProcess.destroyForcibly();
            ffmpegProcess = null;
        }
    }

    private byte[] bgrToBgra(byte[] frameBuffer, int w, int h) {
        byte[] bgra = new byte[w * h * 4];
        for (int i = 0, j = 0; i < frameBuffer.length; i += 3, j += 4) {
            bgra[j]     = frameBuffer[i];       // B
            bgra[j + 1] = frameBuffer[i + 1];   // G
            bgra[j + 2] = frameBuffer[i + 2];   // R
            bgra[j + 3] = (byte) 0xFF;           // A
        }
        return bgra;
    }
}
