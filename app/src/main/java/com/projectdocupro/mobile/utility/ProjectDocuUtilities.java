package com.projectdocupro.mobile.utility;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import androidx.core.app.ActivityCompat;

import com.projectdocupro.mobile.fragments.add_direction.StorageUtils;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static com.projectdocupro.mobile.activities.SplashActivity.PHONE_BOOK_REQUEST_CODE;

public class ProjectDocuUtilities {
    public File getFileForPhotoStorage(String photoName) {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), photoName);
    }

    public File getFileForPhotoThumbnailStorage(Context context, String photoName) {
        return new File(context.getFilesDir(), photoName);
    }

    public static File getFileForPlanStorage(Context context, String planName) {
        //System.out.println("context.getFilesDir():"+context.getDir("data", context.MODE_PRIVATE));
        return new File(context.getDir("data", context.MODE_PRIVATE), planName);
        //return new File (context.getFilesDir(), planName);
    }

    public File getFileForMemoStorage(Context context, String memoName) {
        return new File(context.getFilesDir(), memoName);
    }

    public String checkStoragePath(boolean checkExternal) {
        StorageUtils storageUtils = new StorageUtils();
        List<StorageUtils.StorageInfo> storageList = storageUtils.getStorageList();

        String internalSDCardPath = null;
        String externalSDCardPath = null;

        for (int i = 0; i < storageList.size(); i++) {
            if (storageList.get(i).internal == false && storageList.get(i).readonly == false) {
                externalSDCardPath = storageList.get(i).path;
            } else if (storageList.get(i).internal == true && storageList.get(i).readonly == false) {
                internalSDCardPath = storageList.get(i).path;
            }
        }

        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
    }


    public Bitmap resizePhotoToDisplaySize(File photoFile, Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Matrix matrix = new Matrix();
        Bitmap resizedBitmap = null;
        Bitmap originalBitmap = null;

        if (photoFile.exists()) {
            originalBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            matrix.setRectToRect(new RectF(0, 0, originalBitmap.getWidth(), originalBitmap.getHeight()), new RectF(0, 0, display.getWidth(), display.getHeight()), Matrix.ScaleToFit.CENTER);
        }

        try {
            ExifInterface exifInterface = new ExifInterface(photoFile.getAbsolutePath());

            int exifPhotoRotation = Integer.parseInt(exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION));

            Configuration config = context.getResources().getConfiguration();

            int photoRotationPortraitCorrection = 0;
            int rotation = wm.getDefaultDisplay().getRotation();

            if (((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && config.orientation == Configuration.ORIENTATION_LANDSCAPE)
                    || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && config.orientation == Configuration.ORIENTATION_PORTRAIT)) {
                photoRotationPortraitCorrection = 0;
            } else {
                photoRotationPortraitCorrection = 90;
            }

            int photoRotation = 0;

            if (exifPhotoRotation == ExifInterface.ORIENTATION_NORMAL) {
                photoRotation = 0 + 1 * photoRotationPortraitCorrection;
            }

            if (exifPhotoRotation == ExifInterface.ORIENTATION_ROTATE_90) {
                photoRotation = 90 + 1 * photoRotationPortraitCorrection;
            }

            if (exifPhotoRotation == ExifInterface.ORIENTATION_ROTATE_180) {
                photoRotation = 180 + 1 * photoRotationPortraitCorrection;
            }

            if (exifPhotoRotation == ExifInterface.ORIENTATION_ROTATE_270) {
                photoRotation = 270 + 1 * photoRotationPortraitCorrection;
            }

            matrix.postRotate(photoRotation);
        } catch (Exception e) {
        }

        if (originalBitmap != null) {
            resizedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, false);
        }

        return resizedBitmap;
    }


    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }

    /**
     * Calculate angle of point with x, y coordinates
     *
     * @param x int    - x coordinate
     * @param y int    - y coordinate
     * @return double   - angle in radians
     */
    private static double getAngle(double x, double y) {
        if (x > 0) {
            return Math.atan(y / x);
        } else if (x < 0 && y >= 0) {
            return Math.atan(y / x) + Math.PI;
        } else if (x < 0 && y < 0) {
            return Math.atan(y / x) - Math.PI;
        } else if (x == 0 && y > 0) {
            return Math.PI;
        } else if (x == 0 && y < 0) {
            return -1 * Math.PI;
        } else if (x == 0 && y == 0) {
            return 0;
        }

        return Math.atan2(y, x);
    }

    /**
     * Calculate map coordinates
     *
     * @param Points array     - Array of point objects with params: x, y, lat, lon.
     *                           All elements are to have lat and lon params.
     *                           Elements with key p1 and p2 should have also x and y params
     * @param p1 int           - First key of Points with x and y (used for calculation)
     * @param p2 int           - Second key of Points with x and y (used for calculation)
     * @return array           - Points with lon and lat
     */

    /**
     * Prüft ob String eine Nummer ist
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static int stringToInt(String str) {
        int myNum = -1;

        try {
            myNum = Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            //System.out.println("Could not parse String:" + nfe);
        }
        return myNum;
    }


    public static void copy(File src, File dst) {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = new FileInputStream(src);
            out = new FileOutputStream(dst);

            byte[] buf = new byte[1024];

            int len;

            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (Exception e) {
        }

        if (in != null) {
            try {
                in.close();
            } catch (Exception e) {
            }
        }

        if (out != null) {
            try {
                out.close();
            } catch (Exception e) {
            }
        }
    }

    public static String getMD5(String s) {
        final String MD5 = "MD5";

        try {
            MessageDigest digest = MessageDigest.getInstance(MD5);

            digest.update(s.getBytes());

            byte messageDigest[] = digest.digest();

            StringBuilder hexString = new StringBuilder();

            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);

                while (h.length() < 2) {
                    h = "0" + h;
                }

                hexString.append(h);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;

        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static BitmapFactory.Options getImageSize(File image) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        //Returns null, sizes are in the options variable
        BitmapFactory.decodeFile(image.getAbsolutePath(), options);
        //int width = options.outWidth;
        //int height = options.outHeight;

        return options;
    }


    public static Bitmap decodeSampledBitmapFromResource(File image, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(image.getAbsolutePath(), options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(image.getAbsolutePath(), options);
    }

    public static int calculateNoOfColumns(Context context, float columnWidthDp) { // For example columnWidthdp=180
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (screenWidthDp / columnWidthDp + 0.5); // +0.5 for correct rounding to int.
        return noOfColumns;
    }

    public static int getColumnSpam(Context context) {
        int columnCount = 0;
        int orientation = context.getResources().getConfiguration().orientation;
        int screenSize = context.getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;
        if (screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE && orientation == Configuration.ORIENTATION_PORTRAIT) {
            columnCount = 2;

        } else if (screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE && orientation == Configuration.ORIENTATION_LANDSCAPE) {
            columnCount = 2;

        } else if (screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE && orientation == Configuration.ORIENTATION_PORTRAIT) {
            columnCount = 2;
        } else if (screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE && orientation == Configuration.ORIENTATION_LANDSCAPE) {
            columnCount = 3;
        } else if (screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL && orientation == Configuration.ORIENTATION_PORTRAIT) {
            columnCount = 1;
        } else if (screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL && orientation == Configuration.ORIENTATION_LANDSCAPE) {
            columnCount = 2;
        } else {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                columnCount = 1;
            } else {
                columnCount = 2;
            }

        }
        return columnCount;
    }

    @SuppressLint({"HardwareIds", "SetTextI18n"})
    public static String getDeviceId(Activity activity) {

        try {
            String imei = "";
            String OS = String.valueOf(Build.VERSION.RELEASE);

            TelephonyManager telephonyManager = (TelephonyManager)activity. getSystemService(Context.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (activity.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    if (telephonyManager != null) {
                        try {

                            imei = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, PHONE_BOOK_REQUEST_CODE);
                }
            } else {
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    if (telephonyManager != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            imei = telephonyManager.getImei();
                        }else{
                            imei = telephonyManager.getDeviceId();
                        }
                    }
                } else {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, PHONE_BOOK_REQUEST_CODE);
                }
            }
            return imei;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

}
