package com.yannick.mychatapp;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.exifinterface.media.ExifInterface;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ImageOperations {

    private final ContentResolver contentResolver;

    public static final int PICK_IMAGE_REQUEST = 0;
    public static final int CAPTURE_IMAGE_REQUEST = 1;
    public static final int PICK_ROOM_IMAGE_REQUEST = 2;
    public static final int PICK_PROFILE_IMAGE_REQUEST = 3;
    public static final int PICK_PROFILE_BANNER_REQUEST = 4;

    public ImageOperations(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public byte[] getImageAsBytes(Context context, Uri filePath, final int type) {
        byte[] byteArray = new byte[0];
        if (contentResolver.getType(filePath).equals("image/gif") && type == PICK_IMAGE_REQUEST) {
            try {
                InputStream iStream = contentResolver.openInputStream(filePath);
                byteArray = getBytes(iStream);
            } catch (IOException e) {
                Log.e("open input stream failed", e.toString());
            }
        } else {
            InputStream imageStream = null;
            try {
                imageStream = contentResolver.openInputStream(filePath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            Bitmap bmp = BitmapFactory.decodeStream(imageStream);

            if (bmp.getWidth() < bmp.getHeight() && (type == PICK_ROOM_IMAGE_REQUEST || type == ImageOperations.PICK_PROFILE_IMAGE_REQUEST)) {
                bmp = Bitmap.createBitmap(bmp, 0, bmp.getHeight()/2-bmp.getWidth()/2, bmp.getWidth(), bmp.getWidth());
            } else if (bmp.getWidth() > bmp.getHeight() && (type == PICK_ROOM_IMAGE_REQUEST || type == ImageOperations.PICK_PROFILE_IMAGE_REQUEST)) {
                bmp = Bitmap.createBitmap(bmp, bmp.getWidth()/2-bmp.getHeight()/2, 0, bmp.getHeight(), bmp.getHeight());
            } else if (bmp.getWidth()/16*9 < bmp.getHeight() && type == ImageOperations.PICK_PROFILE_BANNER_REQUEST) {
                bmp = Bitmap.createBitmap(bmp, 0, bmp.getHeight()/2-bmp.getWidth()/16*9/2, bmp.getWidth(), bmp.getWidth()/16*9);
            } else if (bmp.getWidth()/16*9 > bmp.getHeight() && type == ImageOperations.PICK_PROFILE_BANNER_REQUEST) {
                bmp = Bitmap.createBitmap(bmp, bmp.getWidth()/2-bmp.getHeight()/9*16/2, 0, bmp.getHeight()/9*16, bmp.getHeight());
            }

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            int compression = 100;
            int compressFactor = 2;
            int height = bmp.getHeight();
            int width = bmp.getWidth();
            if (getImageSize(filePath) > height * width) {
                compressFactor = 4;
            }

            if (type == PICK_ROOM_IMAGE_REQUEST || type == PICK_PROFILE_IMAGE_REQUEST) {
                while (height * width > 500 * 500) {
                    height /= 1.1;
                    width /= 1.1;
                    compression -= compressFactor;
                }
            } else {
                while (height * width > 1920 * 1080) {
                    height /= 1.1;
                    width /= 1.1;
                    compression -= compressFactor;
                }
            }

            bmp = Bitmap.createScaledBitmap(bmp, width, height, false);
            try {
                bmp = rotateImageIfRequired(context, bmp, filePath);
            } catch (IOException e) {
                Log.e("open input stream failed", e.toString());
            }
            bmp.compress(Bitmap.CompressFormat.JPEG, compression, stream);
            byteArray = stream.toByteArray();
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return byteArray;
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public Long getImageSize(Uri filePath) {
        Cursor returnCursor = contentResolver.query(filePath, null, null, null, null);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        return returnCursor.getLong(sizeIndex);
    }

    public Bitmap rotateImageIfRequired(Context context, Bitmap image, Uri selectedImage) throws IOException {
        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei = new ExifInterface(input);

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(image, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(image, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(image, 270);
            default:
                return image;
        }
    }

    private Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}
