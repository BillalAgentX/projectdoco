package com.projectdocupro.mobile.compass;

import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.OrientationEventListener;

import com.projectdocupro.mobile.utility.Utils;

public class Compass implements SensorEventListener {
    private static final String TAG = "Compass";
    private MYORIENTATION myorientation;
    int lastAccuracyResult = -1;

    enum MYORIENTATION {
        PORTRAIT,
        LANDSCAPE,
        REVERSE_LANDSCAPE
    }

    public interface CompassListener {
        void onNewAzimuth(float azimuth);

        void onAccuracyCorrect();

        void onAccuracyWrong();
    }

    private CompassListener listener;

    private SensorManager sensorManager;
    private Sensor mOrientationSensor;
    private Sensor mMagneticSensor;

    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float[] R = new float[9];
    private float[] I = new float[9];

    private float azimuth;
    private float azimuthFix;

    public Compass(Activity context) {
        sensorManager = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);

        new OrientationEventListener(context) {
            @Override
            public void onOrientationChanged(int orientation) {
//                Log.d(TAG, "Orientation New: " + orientation);


                int currentOrientation = context.getWindowManager().getDefaultDisplay().getRotation();

                switch (currentOrientation) {
                    case 0:
                        //. SCREEN_ORIENTATION_PORTRAIT

                        myorientation = MYORIENTATION.PORTRAIT;
                        break;
                    //----------------------------------------
                    case 2:
                        //. SCREEN_ORIENTATION_REVERSE_PORTRAIT

                        myorientation = MYORIENTATION.PORTRAIT;
                        break;
                    //----------------------------------------
                    case 1:
                        //. SCREEN_ORIENTATION_LANDSCAPE

                        myorientation = MYORIENTATION.LANDSCAPE;
                        break;
                    //----------------------------------------
                    case 3:
                        //. SCREEN_ORIENTATION_REVERSE_LANDSCAPE

                        myorientation = MYORIENTATION.REVERSE_LANDSCAPE;
                        break;
                    //----------------------------------------
                }

//                deviceOrientation = orientation;
            }
        }.enable();



    }

    public void start() {


        if (orientaionSensorNonExistent(this.sensorManager)) {
            mOrientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            mMagneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        } else
            this.mOrientationSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);


        sensorManager.registerListener(this, mOrientationSensor,
                SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, mMagneticSensor,
                SensorManager.SENSOR_DELAY_GAME);


    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    public void setAzimuthFix(float fix) {
        azimuthFix = fix;
    }

    public void resetAzimuthFix() {
        setAzimuthFix(0);
    }

    public void setListener(CompassListener l) {
        listener = l;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {


        synchronized (this) {


            if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

                if (lastAccuracyResult == sensorEvent.accuracy)
                    return;

                if (sensorEvent.accuracy == SENSOR_STATUS_ACCURACY_LOW) {
                    if (this.listener != null)
                        listener.onAccuracyWrong();
                } else {

                    listener.onAccuracyCorrect();
                }
                lastAccuracyResult = sensorEvent.accuracy;
            } else {


                float[] fArr = sensorEvent.values;

                if (orientaionSensorNonExistent(sensorManager)) {
                    float[] fArr2 = new float[9];
                    SensorHelper.quatToRotMat(fArr2, (float[]) sensorEvent.values.clone());
                    SensorHelper.rotMatToOrient(fArr, fArr2);
                }

                float f = fArr[0] * -1.0f;
                azimuth = normalizeDegree(f);
                azimuth = normalizeDegree(azimuth * -1);
              //  Log.i("azimuthV>>", azimuth + "");


                if (myorientation != null) {
                    if (myorientation == MYORIENTATION.PORTRAIT) {
                        //testOrientation = "PORTRAIT";
                        //  compassDegrees = values.values[0];
                    } else if (myorientation == MYORIENTATION.LANDSCAPE) {
                        /// testOrientation = "LANDSCAPE";
                        azimuth = azimuth + 90;
                    } else if (myorientation == MYORIENTATION.REVERSE_LANDSCAPE) {
                        //  testOrientation = "REVERSE_LANDSCAPE";
                        azimuth = azimuth - 90;
                        //  Utils.showLogger("reveresLand");
                    }
                }

                if (azimuth < 0)
                    azimuth = azimuth + 360;
                else if (azimuth > 360)
                    azimuth = azimuth - 360;

                if (listener != null)
                    listener.onNewAzimuth(azimuth);
            }
        }


    }


    public String getDirection(float normalizeDegree) {
        String result = "";
        if (normalizeDegree > 22.5f && normalizeDegree <= 67.5f) {
            result = "NE";
        } else if (normalizeDegree > 67.5f && normalizeDegree <= 112.5f) {
            result = "E";
        } else if (normalizeDegree > 112.5f && normalizeDegree <= 157.5f) {
            result = "SE";
        } else if (normalizeDegree > 157.5f && normalizeDegree <= 202.5f) {
            result = "S";

        } else if (normalizeDegree > 202.5f && normalizeDegree <= 247.5f) {
            result = "SW";

        } else if (normalizeDegree > 247.5f && normalizeDegree <= 292.5f) {
            result = "W";

        } else if (normalizeDegree <= 292.5f || normalizeDegree > 337.5f) {
            result = "N";

        } else {
            result = "NW";

        }
        return result;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /* access modifiers changed from: private */
    public boolean orientaionSensorNonExistent(SensorManager sensorManager) {

        return sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION) == null;
       // return false;
    }

    private float normalizeDegree2(float f) {
        return (f + 720.0f) % 360.0f;
    }

    public static float normalizeDegree(float f) {
        return (f + 720.0f) % 360.0f;
    }
}
