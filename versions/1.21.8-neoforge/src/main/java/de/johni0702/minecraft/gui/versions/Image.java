package de.johni0702.minecraft.gui.versions;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * As of LWJGL 3, AWT must never be initialized, otherwise GLFW will be broken on OSX.
 * Any usage of BufferedImage will initialize AWT.
 */
public class Image implements AutoCloseable {
    private NativeImage inner;

    public Image(int width, int height) {
        this(new NativeImage(NativeImage.Format.RGBA, width, height, true));
    }

    public Image(NativeImage inner) {
        this.inner = inner;
    }

    public NativeImage getInner() {
        return inner;
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    @Override
    public void close() {
        if (inner != null) {
            inner.close();
            inner = null;
        }
    }

    public int getWidth() {
        return inner.getWidth();
    }

    public int getHeight() {
        return inner.getHeight();
    }

    public void setRGBA(int x, int y, int r, int g, int b, int a) {
        inner.setColorArgb(x, y, ((a & 0xff) << 24) | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff));
    }

    public static Image read(Path path) throws IOException {
        return read(Files.newInputStream(path));
    }

    public static Image read(InputStream in) throws IOException {
        byte[] bytes;
        try (InputStream input = in) {
            bytes = input.readAllBytes();
        }
        in = new ByteArrayInputStream(bytes);
        if (bytes[0] != (byte) 0x89 || bytes[1] != 0x50 || bytes[2] != 0x4e || bytes[3] != 0x47) {
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(bytes));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", out);
            in = new ByteArrayInputStream(out.toByteArray());
        }

        return new Image(NativeImage.read(in));
    }

    public void writePNG(File file) throws IOException {
        inner.writeTo(file);
    }

    public void writePNG(OutputStream outputStream) throws IOException {
        Path tmp = Files.createTempFile("tmp", ".png");
        try {
            inner.writeTo(tmp);
            Files.copy(tmp, outputStream);
        } finally {
            Files.delete(tmp);
        }
    }

    public Image scaledSubRect(int x, int y, int width, int height, int scaledWidth, int scaledHeight) {
        NativeImage dst = new NativeImage(inner.getFormat(), scaledWidth, scaledHeight, false);
        inner.resizeSubRectTo(x, y, width, height, dst);
        return new Image(dst);
    }

    @Deprecated
    public BufferedImage toBufferedImage() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            writePNG(out);
            return ImageIO.read(new ByteArrayInputStream(out.toByteArray()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public NativeImageBackedTexture toTexture() {
        return new NativeImageBackedTexture(null, inner);
    }
}
