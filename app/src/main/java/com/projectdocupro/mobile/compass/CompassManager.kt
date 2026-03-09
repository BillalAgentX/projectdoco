/*
 * This file is part of Compass.
 * Copyright (C) 2023 Philipp Bobek <philipp.bobek@mailbox.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Compass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.projectdocupro.mobile.compass

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.util.Log
import android.view.Display
import android.view.Surface
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.projectdocupro.mobile.utility.Utils


private const val TAG = "CompassFragment"

private const val LOCATION_UPDATES_MIN_TIME_MS = 1000L
private const val LOCATION_UPDATES_MIN_DISTANCE_M = 10.0f

class CompassManager {
    var isEnverted: Boolean = false;
    var onAZUAngleChange: OnAZUAngleChange? = null;
    private val compassSensorEventListener = CompassSensorEventListener()
    //private val compassLocationListener = CompassLocationListener()


    private var sensorManager: SensorManager? = null
    var myActivity: Activity? = null;


    public fun onCreate(activity: Activity, onAZUAngleChange: OnAZUAngleChange) {
        myActivity = activity;
        this.onAZUAngleChange = onAZUAngleChange;
        setupSystemServices()
    }


    private fun setupSystemServices() {
        sensorManager = ActivityCompat.getSystemService(myActivity!!, SensorManager::class.java)
    }


    fun onResume() {
        startSystemServiceFunctionalities()


        Log.i(TAG, "Started compass")
    }


    private fun startSystemServiceFunctionalities() {
        registerSensorListener()


    }

    private fun registerSensorListener() {
        sensorManager?.also { sensorManager ->
            sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
                ?.also { rotationVectorSensor ->
                    val success = sensorManager.registerListener(
                        compassSensorEventListener,
                        rotationVectorSensor,
                        SensorManager.SENSOR_DELAY_GAME
                    )
                    if (success) {
                        Log.d(TAG, "Registered listener for rotation vector sensor")
                    } else {
                        Log.w(TAG, "Could not enable rotation vector sensor")
                        showErrorDialog(AppError.ROTATION_VECTOR_SENSOR_FAILED)
                    }
                } ?: run {
                Log.w(TAG, "Rotation vector sensor not available")
                showErrorDialog(AppError.ROTATION_VECTOR_SENSOR_NOT_AVAILABLE)
            }
        } ?: run {
            Log.w(TAG, "SensorManager not present")
            showErrorDialog(AppError.SENSOR_MANAGER_NOT_PRESENT)
        }
    }


    @RequiresPermission(value = ACCESS_COARSE_LOCATION)
    private fun registerCoarseLocationListener() {
        /*      locationManager?.also { locationManager ->
                  locationManager.requestLocationUpdates(
                      NETWORK_PROVIDER,
                      LOCATION_UPDATES_MIN_TIME_MS,
                      LOCATION_UPDATES_MIN_DISTANCE_M,
                      compassLocationListener
                  )
              } ?: run {
                  Log.w(TAG, "LocationManager not present")
                  showErrorDialog(AppError.LOCATION_MANAGER_NOT_PRESENT)
              }*/
    }

    private fun showErrorDialog(appError: AppError) {
        /*     MaterialAlertDialogBuilder(requireContext())
                 .setTitle(R.string.error)
                 .setIcon(R.drawable.ic_error)
                 .setMessage(getString(R.string.error_message, getString(appError.messageId), appError.name))
                 .setNeutralButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
                 .show()
        */
    }

    fun onPause() {
        sensorManager?.unregisterListener(compassSensorEventListener)
        //locationManager?.removeUpdates(compassLocationListener)
        Log.i(TAG, "Stopped compass")
    }


    private inner class CompassSensorEventListener : SensorEventListener {

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            when (sensor.type) {
                Sensor.TYPE_ROTATION_VECTOR -> setSensorAccuracy(accuracy)
                else -> Log.w(TAG, "Unexpected accuracy changed event of type ${sensor.type}")
            }
        }

        private fun setSensorAccuracy(accuracy: Int) {
            //    val sensorAccuracy = adaptSensorAccuracy(accuracy)
            //   setSensorAccuracy(sensorAccuracy)
        }


        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {

                Sensor.TYPE_ROTATION_VECTOR -> updateCompass(event)
                else -> Log.w(TAG, "Unexpected sensor changed event of type ${event.sensor.type}")
            }
        }

        private fun updateCompass(event: SensorEvent) {
            if (event.values[2] < 0.50f)
                isEnverted = true;
            else
                isEnverted = false;
             var result = event.values[2];

             Utils.showLogger2(result.toString())
            val rotationVector = RotationVector(event.values[0], event.values[1], event.values[2])
            val displayRotation = getDisplayRotation()
            val magneticAzimuth = MathUtils.calculateAzimuth(rotationVector, displayRotation)


            Log.i("asfsaefeasfa", magneticAzimuth.degrees.toString())
            var oldValue = magneticAzimuth.degrees.toInt();
            if(isEnverted)
                oldValue=oldValue+150;
           // if(oldValue>360)
           //     oldValue = 360;
            onAZUAngleChange?.onAzimuthChange(oldValue);
           // setAzimuth(magneticAzimuth.degrees)

        }

        private fun getDisplayRotation(): DisplayRotation {
            return when (getDisplayCompat()?.rotation) {
                Surface.ROTATION_90 -> DisplayRotation.ROTATION_90
                Surface.ROTATION_180 -> DisplayRotation.ROTATION_180
                Surface.ROTATION_270 -> DisplayRotation.ROTATION_270
                else -> DisplayRotation.ROTATION_0
            }
        }

        private fun getDisplayCompat(): Display? {
            return if (VERSION.SDK_INT >= VERSION_CODES.R) {
                myActivity?.display
            } else {
                myActivity?.windowManager?.defaultDisplay
            }
        }


    }


    internal fun setAzimuth(azimuth: Azimuth) {
        Log.v(TAG, "Azimuth $azimuth")
    }

    public interface OnAZUAngleChange {
        fun onAzimuthChange(v: Int);
    }
}
