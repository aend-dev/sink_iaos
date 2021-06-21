package com.sinkleader.install.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.sinkleader.install.BuildConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static java.lang.StrictMath.max;

/**
 * Created by Snow on 12/04/2017.
 */

public class MediaManager {

    private final String TAG = "MediaManager";

    public final static int FAILED_BY_CRASH = 3000;
    public final static int FAILED_BY_SIZE_LIMIT = 3001;

    private final int MAX_RESOLUTION = 400;
    private final int MAX_IMAGE_SIZE = 5 * 1024;

    private Activity mActivity = null;
    private Fragment mFragment = null;
    private MediaCallback mCallback = null;

    private static Uri mUri = null;
    private static Uri mCropUri = null;

    public final static int SET_GALLERY = 1;
    public final static int SET_CAMERA = 2;
    public final static int SET_CAMERA_VIDEO = 3;
    public final static int CROP_IMAGE = 4;

    protected MediaManager() {

    }

    /************************************************************
     *  Public
     ************************************************************/

    public interface MediaCallback {
        void onSelected(Boolean isVideo, Uri uri, Bitmap bitmap, String videoPath, String thumbPath);
        void onFailed(int code, String err);
        void onDelete();
    }

    public MediaManager(Activity activity) {
        mActivity = activity;
    }

    public MediaManager(Fragment fragment) {
        mFragment = fragment;
    }

    private Context getContext() {
        if (mFragment != null)
            return mFragment.getContext();

        return mActivity;
    }

    public void getMediaFromGallery() {
        getMediaFromGallery(false);
    }

    public void getMediaFromGallery(boolean video) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE); // | MediaStore.Video.Media.CONTENT_TYPE
        if (video) {
            intent.setType("image/* video/*");
        } else {
            intent.setType("image/*");
        }
        if (mActivity != null) {
            mActivity.startActivityForResult(intent, SET_GALLERY);
        } else if (mFragment != null) {
            mFragment.startActivityForResult(intent, SET_GALLERY);
        }
    }

    public void getImageFromCamera() {
        File file = createFile(false);
        mUri = getUri(getContext(), file);

        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
            if (mActivity != null) {
                mActivity.startActivityForResult(intent, SET_CAMERA);
            } else if (mFragment != null) {
                mFragment.startActivityForResult(intent, SET_CAMERA);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == SET_GALLERY) {
            Uri uri = data.getData();

        } else if (requestCode == SET_CAMERA) { // 카메라로 사진을 캡쳐한 경우.
            BitmapFactory.Options options = getBitmapFactory(mUri);
            if (options.outWidth == -1 || options.outHeight == -1)
                return;

            double ratio = getRatio(options);
            Bitmap bitmap = resizeBitmap(mUri, ratio);

            if (checkHighSDK()) {
                File file = getFile(mUri);
                bitmap = checkRotate(bitmap, file.getAbsolutePath(), mUri);
                saveBitmap(bitmap, file.getAbsolutePath());
                mUri = getUriFromFile(getContext(), file);
            } else {
                bitmap = checkRotate(bitmap, mUri.getPath(), mUri);
                saveBitmap(bitmap, mUri.getPath());
            }

            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + getFolderPath())));
                } else {
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    File file = new File(getFolderPath());
                    intent.setData(Uri.fromFile(file));
                    getContext().sendBroadcast(intent);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /************************************************************
     *  Helper
     ************************************************************/

    public Uri getUrl(){
        return mUri;
    }

    private File createFile(boolean isVideo) {
        File folder = new File(getFolderPath());
        if (!folder.exists())
            folder.mkdirs();

        Long tsLong = System.currentTimeMillis() / 1000;
        String ext = isVideo ? ".mp4" : ".png";
        String filename = tsLong.toString() + ext;
        try {
            return File.createTempFile(tsLong.toString(), ext, folder);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getFolderPath() {
        File folder = new File(Environment.getExternalStorageDirectory()
                + "/" + Constant.SDCARD_FOLDER);
        if (folder.exists() == false) {
            folder.mkdir();
        }

        return Environment.getExternalStorageDirectory() + "/" + Constant.SDCARD_FOLDER;
    }

    private Uri getUri(Context context, File file) {
        if (checkHighSDK()) {
            return getUriFromFile(context, file);
        } else {
            return Uri.fromFile(file);
        }
    }

    private boolean checkHighSDK() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    private Uri getUriFromFile(Context context, File file) {
        Log.d(TAG, "file:" + file);
        Log.d(TAG, "BuildConfig.APPLICATION_ID:" + BuildConfig.APPLICATION_ID);
        return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
    }

    private BitmapFactory.Options getBitmapFactory(Uri uri) {
        InputStream input = null;

        try {
            input = getContext().getContentResolver().openInputStream(uri);

        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inDither = true; // optional
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;// optional

        BitmapFactory.decodeStream(input, null, options);

        try {
            input.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return options;
    }

    private double getRatio(BitmapFactory.Options options) {
        int size = max(options.outWidth, options.outHeight);
        return max(size / MAX_RESOLUTION, 1);
    }

    private Bitmap resizeBitmap(Uri uri, double ratio) {
        InputStream input = null;

        try {
            input = getContext().getContentResolver().openInputStream(uri);

        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        options.inDither = true;// optional
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;// optional

        Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);

        try {
            input.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    private static int getPowerOfTwoForSampleRatio(double ratio) {
        int k = Integer.highestOneBit((int) Math.floor(ratio));
        if (k == 0)
            return 1;
        else
            return k;
    }

    private File getFile(Uri url) {
        File folder = new File(getFolderPath());
        return new File(folder, new File(url.getPath()).getName());
    }

    private Bitmap checkRotate(Bitmap bitmap, String filename, Uri uri) {
        int orientation = -1;

        try {
            ExifInterface ei;
            InputStream input = getContext().getContentResolver().openInputStream(uri);
            if (Build.VERSION.SDK_INT > 23)
                ei = new ExifInterface(input);
            else
                ei = new ExifInterface(filename);

            orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        } catch (IOException e) {
            e.printStackTrace();
        }

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                bitmap = rotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                bitmap = rotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                bitmap = rotateImage(bitmap, 270);
                break;
        }

        return bitmap;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public void saveBitmap(Bitmap bitmap, String path) {
        OutputStream stream = null;

        try {
            File file = new File(path);
            stream = new FileOutputStream(file);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        bitmap.compress(CompressFormat.PNG, 100, stream);

        try {
            stream.flush();
            stream.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private String getRealPath(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContext().getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    private void RemoveImage(String filename) {
        File folder = new File(getFolderPath());
        if (!folder.exists())
            folder.mkdirs();

        File file = new File(folder.toString(), filename);
        if (file != null && file.exists()) {
            Log.d(TAG, "Remove previous temp profile file.");
            file.delete();
        }
    }

    private Bitmap resizeBitmap(Bitmap source, int maxResolution) {
        int width = source.getWidth();
        int height = source.getHeight();
        int newWidth = width;
        int newHeight = height;
        float rate = 0;

        if (width > height) {
            if (maxResolution < width) {
                rate = maxResolution / (float) width;
                newHeight = (int) (height * rate);
                newWidth = maxResolution;
            }
        } else {
            if (maxResolution < height) {
                rate = maxResolution / (float) height;
                newWidth = (int) (width * rate);
                newHeight = maxResolution;
            }
        }

        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
    }
}
