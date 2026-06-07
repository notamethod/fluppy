package com.notamethod.fluppy.gui;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class BackgroundVideo extends Application {

    private static final int WIDTH  = 1280;
    private static final int HEIGHT = 720;
    private static final int BYTES_PER_FRAME = WIDTH * HEIGHT * 3; // BGR24

    private Process ffmpegProcess;
    private WritableImage writableImage;
    private PixelWriter pixelWriter;
    private final byte[] frameBuffer = new byte[BYTES_PER_FRAME];

    @Override
    public void start(Stage stage) throws Exception {

        // 1. Lancer ffmpeg en pipe
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-stream_loop", "-1",        // boucle infinie
                "-i", "video.mp4",
                "-vf", "scale=" + WIDTH + ":" + HEIGHT,
                "-r", "30",                   // 30 fps
                "-f", "rawvideo",
                "-pix_fmt", "bgr24",
                "pipe:1"                      // sortie sur stdout
        );
        pb.redirectErrorStream(false); // évite de mélanger stderr et stdout
        ffmpegProcess = pb.start();

        // 2. Préparer l'image JavaFX
        writableImage = new WritableImage(WIDTH, HEIGHT);
        pixelWriter = writableImage.getPixelWriter();
        ImageView imageView = new ImageView(writableImage);
        imageView.setFitWidth(WIDTH);
        imageView.setFitHeight(HEIGHT);

        // 3. Thread de lecture des frames
        InputStream stream = ffmpegProcess.getInputStream();
        Thread readerThread = new Thread(() -> {
            try {
                while (true) {
                    // Lire exactement une frame
                    int offset = 0;
                    while (offset < BYTES_PER_FRAME) {
                        int read = stream.read(frameBuffer, offset,
                                BYTES_PER_FRAME - offset);
                        if (read == -1) return;
                        offset += read;
                    }

                    // Écrire dans la WritableImage (thread JavaFX)
                    byte[] copy = frameBuffer.clone();
                    javafx.application.Platform.runLater(() -> {
                        pixelWriter.setPixels(
                                0, 0, WIDTH, HEIGHT,
                                PixelFormat.getByteBgraPreInstance(),
                                convertBGRtoBGRA(copy), 0, WIDTH * 4
                        );
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        readerThread.setDaemon(true);
        readerThread.start();

        // 4. Scène avec la vidéo en fond
        StackPane root = new StackPane(imageView);
        // Ajoute ici tes autres composants UI par-dessus
        // root.getChildren().add(tonAutreContenu);

        stage.setScene(new Scene(root, WIDTH, HEIGHT));
        stage.setTitle("Video Background");
        stage.show();
    }

    // Convertit BGR24 → BGRA pour JavaFX
    private byte[] convertBGRtoBGRA(byte[] bgr) {
        byte[] bgra = new byte[WIDTH * HEIGHT * 4];
        for (int i = 0, j = 0; i < bgr.length; i += 3, j += 4) {
            bgra[j]     = bgr[i];       // B
            bgra[j + 1] = bgr[i + 1];  // G
            bgra[j + 2] = bgr[i + 2];  // R
            bgra[j + 3] = (byte) 0xFF;  // A
        }
        return bgra;
    }

    @Override
    public void stop() {
        if (ffmpegProcess != null) ffmpegProcess.destroy();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
