package com.projectdocupro.mobile.fragments.add_direction;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.location.Location;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;


import com.projectdocupro.mobile.R;
import com.projectdocupro.mobile.models.PlansModel;
import com.projectdocupro.mobile.models.ReferPointJSONPlanModel;
import com.projectdocupro.mobile.utility.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class ProjectDocuUtilities {
	private static Bitmap myBitmap;

	public File getFileForPhotoStorage (String photoName) {
		return new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), photoName);
	}
	
	public File getFileForPhotoThumbnailStorage (Context context, String photoName) {
		return new File (context.getFilesDir(), photoName);
	}
	
	public static File getFileForPlanStorage (Context context, String planName) {
		//System.out.println("context.getFilesDir():"+context.getDir("data", context.MODE_PRIVATE));
		return new File (context.getDir("data", context.MODE_PRIVATE), planName);
		//return new File (context.getFilesDir(), planName);
	}
	
	public File getFileForMemoStorage (Context context, String memoName) {
		return new File (context.getFilesDir(), memoName);
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
	
	
/*	public static String getPlanPath(ProjectDocuMainActivity projectDocuMainActivity) {
		String planName = "Plan_"+projectDocuMainActivity.currentPlanId;
		File planFile = getFileForPlanStorage(projectDocuMainActivity, planName);
		//System.out.println("planFile.getAbsolutePath():"+planFile.getAbsolutePath());
        String imagePath = "file://"+planFile.getAbsolutePath()+".jpg";
		
		return imagePath;
	}*/
	
    public Bitmap resizePhotoToDisplaySize(File photoFile, Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
    	
        Matrix matrix = new Matrix ();	
		Bitmap resizedBitmap = null;
		Bitmap originalBitmap = null;
		
	    if(photoFile.exists()) {
	    	originalBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
	        matrix.setRectToRect(new RectF(0, 0, originalBitmap.getWidth(), originalBitmap.getHeight()), new RectF(0, 0, display.getWidth(), display.getHeight()), Matrix.ScaleToFit.CENTER);
	    }
	    
	    try {
	    	ExifInterface exifInterface = new ExifInterface (photoFile.getAbsolutePath());
	    	
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
	    } catch(Exception e) {}
	    
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
	private static double getAngle (double x, double y) {
		if (x > 0) {
			return Math.atan(y / x);
		}
		else if (x < 0 && y >= 0) {
		    return Math.atan(y / x) + Math.PI;
		}
		else if (x < 0 && y < 0) {
		    return Math.atan(y / x) - Math.PI;
		}
		else if (x ==0 && y > 0) {
		    return Math.PI;
		}
		else if (x == 0 && y < 0) {
		    return -1 * Math.PI;
		}
		else if (x == 0 && y == 0) {
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
	public static GeoPoint getLocations (GeoPoint points, GeoPoint p1, GeoPoint p2) {
		double distanceMap = Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p1.y - p2.y, 2));
		  	
		double distanceGeo = Math.sqrt(Math.pow((p2.lat - p1.lat) * 111, 2) + Math.pow((p2.lon - p1.lon) * 111 * Math.cos(Math.PI * (p1.lat + p2.lat) / 360), 2));
		  		
		double distanceMultiply = distanceMap / distanceGeo;
		  
		double angleMap = getAngle((p2.x - p1.x), (p1.y - p2.y));
		  
		double angleGeo = getAngle(((p2.lon - p1.lon) * Math.cos(Math.PI * (p1.lat + p2.lat) / 360)), (p2.lat - p1.lat));
		  
		double angleDifference = angleMap - angleGeo;
		  		
		distanceGeo = Math.sqrt(Math.pow((p1.lat - points.lat) * 111, 2) + Math.pow((p1.lon - points.lon) * 111 * Math.cos(Math.PI * (p1.lat + points.lat) / 360), 2));
		  
		angleGeo = getAngle(((points.lon - p1.lon) * Math.cos(Math.PI * (p1.lat + points.lat) / 360)), (points.lat - p1.lat));
		  
		double angle = (angleGeo + angleDifference) % (2 * Math.PI);

		points.x = p1.x + distanceGeo * distanceMultiply * Math.cos(angle);
		points.y = p1.y - distanceGeo * distanceMultiply * Math.sin(angle);

		Log.d("ZAIN SB DEBUGGING", "x = " + points.x + "y = " + points.y);
		Utils.showLogger("x AND y"+"x = " + points.x + "y = " + points.y);
		  
		return points;
	}
	
	/**
	 * Prüft ob String eine Nummer ist
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str)  
	{  
	  try  
	  {  
	    double d = Double.parseDouble(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}
	
	public static int stringToInt(String str){
		int myNum = -1;

		try {
		    myNum = Integer.parseInt(str);
		} catch(NumberFormatException nfe) {
		   //System.out.println("Could not parse String:" + nfe);
		} 
		return myNum;
	}
	
	public static GeoPoint getPlanLocationFromGps(Context context, Location location, PlansModel plansModelOBJ, List<ReferPointJSONPlanModel>referPointList) {
		GeoPoint [] refGeoPoints = null;
		GeoPoint locationPoint = new GeoPoint();
		int planWidth = 0;
		int planHeight = 0;


		int duration = Toast.LENGTH_LONG;
		
		// Wenn eine gültige Location vorhanden ist...
		if (location != null) {
			locationPoint.lat = location.getLatitude();
			locationPoint.lon = location.getLongitude();
		}
		else {
			Toast toast = Toast.makeText(context, context.getResources().getString(R.string.toast_plan_no_gps_reference), duration);
			toast.show();
			return null;
		}
		if (plansModelOBJ != null && plansModelOBJ.getPlanPhotoPathLargeSize() != null && !plansModelOBJ.getPlanPhotoPathLargeSize().equals("")) {

		// Größe des Plans ermitteln
				Bitmap planImage = BitmapFactory.decodeFile(plansModelOBJ.getPlanPhotoPathLargeSize());;
				 
				if (planImage != null) {
					planWidth = planImage.getWidth();
					planHeight = planImage.getHeight();
					planImage.recycle();
					planImage = null;
				}

			
			// GeoPunkte anhand der aktuellen GPS Location berechnen
			if (referPointList != null && referPointList.size() > 0) {
				try {
					// Starte das Parsen des Check-for-Image-JSON vom Backend
					// Geopoint array nur initialisieren wenn mindestens zwei RefPoints vom Backend kommen
					if (referPointList.size() >= 2) {

						refGeoPoints = new GeoPoint[2];

						refGeoPoints[0] = new GeoPoint();
						refGeoPoints[1] = new GeoPoint();

						if (referPointList.get(0) != null && referPointList.get(1) != null) {
							refGeoPoints[0].x = referPointList.get(0).getxCoord();
							refGeoPoints[0].y = referPointList.get(0).getyCoord();
							refGeoPoints[0].lon = referPointList.get(0).getLon();
							refGeoPoints[0].lat = referPointList.get(0).getLat();

							refGeoPoints[1].x = referPointList.get(1).getxCoord();
							refGeoPoints[1].y = referPointList.get(1).getyCoord();
							refGeoPoints[1].lon = referPointList.get(1).getLon();
							refGeoPoints[1].lat = referPointList.get(1).getLat();
							locationPoint = ProjectDocuUtilities.getLocations(locationPoint, refGeoPoints[0], refGeoPoints[1]);
		 
							// Wenn GPS Position innerhalb des gewählten Plans ist, Position auf Karte setzen
							if (Math.abs(locationPoint.y) > 0 && Math.abs(locationPoint.y) < planHeight / 2 && Math.abs(locationPoint.x) > 0 && Math.abs(locationPoint.x) < planWidth) {
								return locationPoint;
							} else {
								Toast toast = Toast.makeText(context, context.getResources().getString(R.string.toast_gps_out_of_range), duration);
								toast.show();
								refGeoPoints = null;
							}
						} else {
							Toast toast = Toast.makeText(context, context.getResources().getString(R.string.toast_plan_no_gps_reference), duration);
							toast.show();
	
							refGeoPoints = null;
						}
					}
				} catch (Exception e) {
					Log.e("loca_excep",e.getMessage());

				}
			}	
		}
		return null;
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
		} catch (Exception e) {}

		if (in != null) {
			try {
				in.close();
			} catch (Exception e) {}
		}

		if (out != null) {
			try {
				out.close();
			} catch (Exception e) {}
		}
	}

	public static String getMD5 (String s) {
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

	public static Bitmap RotateBitmap(Bitmap source, float angle)
	{
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

	public static boolean isNetworkConnected(Context c) {
		ConnectivityManager connMgr = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
		return activeInfo != null && activeInfo.isConnected();

	}
	public static String givenFile_MD5_Hash(String filename)  {
		String hashtext="";

		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		File initialFile = new File(filename);
		InputStream targetStream = null;
		try {
			targetStream = new FileInputStream(initialFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		DigestInputStream dis = new DigestInputStream(targetStream, md);
//            Read decorated stream(dis) to EOF as normal...
		byte[] digest = md.digest(filename.getBytes());
		BigInteger no = new BigInteger(1,digest);

		// Convert message digest into hex value
		hashtext = no.toString(16);
		while (hashtext.length() < 32) {
			hashtext = "0" + hashtext;
		}
		return hashtext;
	}
}
