package com.notamethod.fluppy.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public class VideoBackground extends Application {

    private static final String VIDEO_PATH = "video.mp4";
    private static final int FPS = 30;

    private Process ffmpegProcess;
    private WritableImage writableImage;
    private PixelWriter pixelWriter;
    private ImageView imageView;

    private int currentWidth = 1280;
    private int currentHeight = 720;

    // Anti-rebond : évite de relancer ffmpeg à chaque pixel de resize
    private final AtomicBoolean resizePending = new AtomicBoolean(false);
    private Thread readerThread;

    @Override
    public void start(Stage stage) {

        imageView = new ImageView();
        imageView.setPreserveRatio(false); // on gère nous-mêmes

        StackPane root = new StackPane(imageView);
        // Ajoute ici tes autres composants par-dessus
        // root.getChildren().add(tonUI);

        Scene scene = new Scene(root, currentWidth, currentHeight);
        stage.setScene(scene);
        stage.setTitle("Video Background");
        stage.show();

        // Démarrage initial
        startFFmpeg(currentWidth, currentHeight);

        // Listener de redimensionnement avec anti-rebond (300ms)
        scene.widthProperty().addListener((obs, oldW, newW) ->
                scheduleResize((int) newW.doubleValue(), (int) scene.getHeight()));
        scene.heightProperty().addListener((obs, oldH, newH) ->
                scheduleResize((int) scene.getWidth(), (int) newH.doubleValue()));
    }

    // ----------------------------------------------------------------
    // Anti-rebond : attend 300ms d'inactivité avant de relancer ffmpeg
    // ----------------------------------------------------------------
    private void scheduleResize(int w, int h) {
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

    private void restartFFmpeg(int w, int h) {
        stopFFmpeg();
        currentWidth = Math.max(w, 1);
        currentHeight = Math.max(h, 1);
        startFFmpeg(currentWidth, currentHeight);
    }

    // ----------------------------------------------------------------
    // Lancement ffmpeg
    // ----------------------------------------------------------------
    private void startFFmpeg(int w, int h) {
        int bytesPerFrame = w * h * 3;

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-stream_loop", "-1",
                    "-i", VIDEO_PATH,
                    "-vf", "scale=" + w + ":" + h,
                    "-r", String.valueOf(FPS),
                    "-f", "rawvideo",
                    "-pix_fmt", "bgr24",
                    "pipe:1"
            );
            pb.redirectError(ProcessBuilder.Redirect.DISCARD);
            ffmpegProcess = pb.start();

            writableImage = new WritableImage(w, h);
            pixelWriter = writableImage.getPixelWriter();
            imageView.setImage(writableImage);
            imageView.setFitWidth(w);
            imageView.setFitHeight(h);

            InputStream stream = ffmpegProcess.getInputStream();
            byte[] frameBuffer = new byte[bytesPerFrame];

            readerThread = new Thread(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        // Lecture d'une frame complète
                        int offset = 0;
                        while (offset < bytesPerFrame) {
                            int read = stream.read(
                                    frameBuffer, offset, bytesPerFrame - offset);
                            if (read == -1) return;
                            offset += read;
                        }

                        // Conversion BGR → BGRA
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

    private byte[] bgrToBgra(byte[] frameBuffer, int w, int h) {
        byte[] bgra = new byte[w * h * 4];
        for (int i = 0, j = 0; i < frameBuffer.length; i += 3, j += 4) {
            bgra[j] = frameBuffer[i];       // B
            bgra[j + 1] = frameBuffer[i + 1];  // G
            bgra[j + 2] = frameBuffer[i + 2];  // R
            bgra[j + 3] = (byte) 0xFF;  // A
        }
        return bgra;
    }

    // ----------------------------------------------------------------
    // Arrêt propre
    // ----------------------------------------------------------------
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

    @Override
    public void stop() {
        stopFFmpeg();
    }

}
    // ----------------------------------------------------------------
