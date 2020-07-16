package hcmut.thesis.htr2020.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import androidx.camera.core.ImageProxy;
import androidx.exifinterface.media.ExifInterface;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class BitmapUtil {

    private BitmapUtil() {
    }

    public static Bitmap decodeWithExif(InputStream inputStream) throws IOException {
        Bitmap decodedImage = BitmapFactory.decodeStream(inputStream);
        ExifInterface exifInterface = new ExifInterface(inputStream);
        int orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        Bitmap rotated;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotated = rotate(decodedImage, 90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotated = rotate(decodedImage, 180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotated = rotate(decodedImage, 270);
                break;
            default:
                rotated = decodedImage;
        }
        return resizeToMaxWidth(rotated, 1024f);
    }

    public static Bitmap fromImageProxy(ImageProxy imageProxy) {
        ByteBuffer byteBuffer = imageProxy.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Bitmap rotated = rotate(decodedBitmap, imageProxy.getImageInfo().getRotationDegrees());
        return resizeToMaxWidth(rotated, 1024f);
    }

    public static Bitmap rotate(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static Bitmap resizeToMaxWidth(Bitmap bitmap, float width) {
        if (bitmap.getWidth() < width) {
            return bitmap;
        }
        float height = bitmap.getHeight() * width / bitmap.getWidth();
        return Bitmap.createScaledBitmap(bitmap, (int) width, (int) height, true);
    }
}
