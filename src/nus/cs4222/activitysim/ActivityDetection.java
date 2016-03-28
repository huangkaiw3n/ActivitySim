package nus.cs4222.activitysim;

import java.io.*;
import java.util.*;
import java.text.*;

import android.hardware.*;
import android.util.*;

import net.qxcg.svy21.*;

/**
   Class containing the activity detection algorithm.

   <p> You can code your activity detection algorithm in this class.
    (You may add more Java class files or add libraries in the 'libs' 
     folder if you need).
    The different callbacks are invoked as per the sensor log files, 
    in the increasing order of timestamps. In the best case, you will
    simply need to copy paste this class file (and any supporting class
    files and libraries) to the Android app without modification
    (in stage 2 of the project).

   <p> Remember that your detection algorithm executes as the sensor data arrives
    one by one. Once you have detected the user's current activity, output
    it using the {@link ActivitySimulator.outputDetectedActivity(UserActivities)}
    method. If the detected activity changes later on, then you need to output the
    newly detected activity using the same method, and so on.
    The detected activities are logged to the file "DetectedActivities.txt",
    in the same folder as your sensor logs.

   <p> To get the current simulator time, use the method
    {@link ActivitySimulator.currentTimeMillis()}. You can set timers using
    the {@link SimulatorTimer} class if you require. You can log to the 
    console/DDMS using either {@code System.out.println()} or using the
    {@link android.util.Log} class. You can use the {@code SensorManager.getRotationMatrix()}
    method (and any other helpful methods) as you would normally do on Android.

   <p> Note: Since this is a simulator, DO NOT create threads, DO NOT sleep(),
    or do anything that can cause the simulator to stall/pause. You 
    can however use timers if you require, see the documentation of the 
    {@link SimulatorTimer} class. 
    In the simulator, the timers are faked. When you copy the code into an
    actual Android app, the timers are real, but the code of this class
    does not need not be modified.
 */
public class ActivityDetection {

    /** 
       Called when the accelerometer sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   x            Accl x value (m/sec^2)
       @param   y            Accl y value (m/sec^2)
       @param   z            Accl z value (m/sec^2)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    public void onAcclSensorChanged( long timestamp , 
                                     float x , 
                                     float y , 
                                     float z , 
                                     int accuracy ) {

        // Process the sensor data as they arrive in each callback, 
        //  with all the processing in the callback itself (don't create threads).

        // You will most likely not need to use Timers at all, it is just 
        //  provided for convenience if you require.

        // Here, we just show a dummy example of creating a timer 
        //  to execute a task 10 minutes later.
        // Be careful not to create too many timers!
//        if( isFirstAcclReading ) {
//            isFirstAcclReading = false;
//            SimulatorTimer timer = new SimulatorTimer();
//            timer.schedule( this.task ,        // Task to be executed
//                            10 * 60 * 1000 );  // Delay in millisec (10 min)
//        }
    }

    /** 
       Called when the gravity sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   x            Gravity x value (m/sec^2)
       @param   y            Gravity y value (m/sec^2)
       @param   z            Gravity z value (m/sec^2)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    public void onGravitySensorChanged( long timestamp , 
                                        float x , 
                                        float y , 
                                        float z , 
                                        int accuracy ) {
    }

    /** 
       Called when the linear accelerometer sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   x            Linear Accl x value (m/sec^2)
       @param   y            Linear Accl y value (m/sec^2)
       @param   z            Linear Accl z value (m/sec^2)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    public void onLinearAcclSensorChanged( long timestamp , 
                                           float x , 
                                           float y , 
                                           float z , 
                                           int accuracy ) {
    }

    /** 
       Called when the magnetic sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   x            Magnetic x value (microTesla)
       @param   y            Magnetic y value (microTesla)
       @param   z            Magnetic z value (microTesla)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    public void onMagneticSensorChanged( long timestamp , 
                                         float x , 
                                         float y , 
                                         float z , 
                                         int accuracy ) {
//        if (isFirstMagReading) {
//            magXvalues = new float[NUM_AVERAGES];
//            magRunningAvgIndex = 0;
//            isFirstMagReading = false;
//            counter = 0;
//            phoneMovedTimestamp = timestamp;
//        }
//
//        if (timestamp > 1458699598032l && timestamp < 1458700132295l) {
//
//            double sum = 0;
//
//            magXvalues[magRunningAvgIndex] = x;
//            magRunningAvgIndex = (magRunningAvgIndex + 1) % NUM_AVERAGES;
//
//            for (float val : magXvalues) sum += val;
//            mxAvg = (int) (sum / NUM_AVERAGES);
//
//            int diff = Math.abs(mxAvg - (int) x);
//
//            if (diff > MX_THRESHOLD) {
//                if (!isPhoneMoving) {
//                    isPhoneMoving = true;
//                    phoneMovedTimestamp = timestamp;
//                    System.out.println("                    Moving!" + convertUnixTimeToReadableString( ActivitySimulator.currentTimeMillis() ));
//                }
//            }
//            else {
//                if (isPhoneMoving) {
//                    isPhoneMoving = false;
//                    phoneMovedTimestamp = timestamp;
//                    System.out.println("                    Stable!" + convertUnixTimeToReadableString( ActivitySimulator.currentTimeMillis() ));
//                }
//            }
//
//            checkMagStill(timestamp);
//
//            System.out.println(counter++ + " " + x + " " + isMagStillForDuration);
//        }
    }

    /**
     Checks if Mag was still for more than xxx milliseconds.
     @param   timestamp    Timestamp of this sensor event
     */
    private void checkMagStill(long timestamp){

        if (timestamp - phoneMovedTimestamp > 10000 && !isPhoneMoving){
            isMagStillForDuration = true;
        }
        else {
            isMagStillForDuration = false;
        }
    }

    /**
       Called when the gyroscope sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   x            Gyroscope x value (rad/sec)
       @param   y            Gyroscope y value (rad/sec)
       @param   z            Gyroscope z value (rad/sec)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    public void onGyroscopeSensorChanged( long timestamp ,
                                          float x ,
                                          float y ,
                                          float z ,
                                          int accuracy ) {
    }

    /**
       Called when the rotation vector sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   x            Rotation vector x value (unitless)
       @param   y            Rotation vector y value (unitless)
       @param   z            Rotation vector z value (unitless)
       @param   scalar       Rotation vector scalar value (unitless)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    public void onRotationVectorSensorChanged( long timestamp ,
                                               float x ,
                                               float y ,
                                               float z ,
                                               float scalar ,
                                               int accuracy ) {
    }

    /**
       Called when the barometer sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   pressure     Barometer pressure value (millibar)
       @param   altitude     Barometer altitude value w.r.t. standard sea level reference (meters)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    public void onBarometerSensorChanged( long timestamp ,
                                          float pressure ,
                                          float altitude ,
                                          int accuracy ) {
    }

    /**
       Called when the light sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   light        Light value (lux)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    public void onLightSensorChanged( long timestamp ,
                                      float light ,
                                      int accuracy ) {
        if (light < 289f)
            isLowLight = true;
        else
            isLowLight = false;
    }

    /**
       Called when the proximity sensor has changed.

       @param   timestamp    Timestamp of this sensor event
       @param   proximity    Proximity value (cm)
       @param   accuracy     Accuracy of the sensor data (you can ignore this)
     */
    public void onProximitySensorChanged( long timestamp ,
                                          float proximity ,
                                          int accuracy ) {
    }

    /**
       Called when the location sensor has changed.

       @param   timestamp    Timestamp of this location event
       @param   provider     "gps" or "network"
       @param   latitude     Latitude (deg)
       @param   longitude    Longitude (deg)
       @param   accuracy     Accuracy of the location data (you may use this) (meters)
       @param   altitude     Altitude (meters) (may be -1 if unavailable)
       @param   bearing      Bearing (deg) (may be -1 if unavailable)
       @param   speed        Speed (m/sec) (may be -1 if unavailable)
     */
    public void onLocationSensorChanged( long timestamp ,
                                         String provider ,
                                         double latitude ,
                                         double longitude ,
                                         float accuracy ,
                                         double altitude ,
                                         float bearing ,
                                         float speed ) {
//        boolean isOnBus = false;
//        if (timestamp >= 1458700132295l && timestamp <= 1458701610135l)
//            isOnBus = true;
//
//        if (timestamp >= 1458701995994l && timestamp <= 1458702225037l)
//            isOnBus = true;
//
//        if (timestamp >= 1458705414330l && timestamp <= 1458705595448l)
//            isOnBus = true;
//
//        if (provider.equals("gps") && isFirstLocReading){
//            isFirstLocReading = false;
//            previousCoord = new LatLonCoordinate(latitude,longitude).asSVY21();
//            previousCoordTimestamp = timestamp;
//        }
//
//        if (provider.equals("gps") && !isFirstLocReading && timestamp != previousCoordTimestamp) {
//            SVY21Coordinate currentCoord = new LatLonCoordinate(latitude, longitude).asSVY21();
//            changeInDistance = Math.sqrt(((Math.pow(currentCoord.getEasting() - previousCoord.getEasting(),2)) +
//                    (Math.pow(currentCoord.getNorthing() - previousCoord.getNorthing(),2))));
//            derivedSpeed = (float)(changeInDistance / ((timestamp - previousCoordTimestamp)/1000));
//            previousCoord = currentCoord;
//            previousCoordTimestamp = timestamp;
//            System.out.println("DistanceChange: " + changeInDistance + " " + "DerivedSpeed: " + derivedSpeed + " " + convertUnixTimeToReadableString( ActivitySimulator.currentTimeMillis() ) + " Bus: " + isOnBus);
//        }
    }

    /** Helper method to convert UNIX millis time into a human-readable string. */
    private static String convertUnixTimeToReadableString( long millisec ) {
        return sdf.format( new Date( millisec ) );
    }

    /** To format the UNIX millis time as a human-readable string. */
    private static final SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd-h-mm-ssa" );

    // Dummy variables used in the dummy timer code example
    private boolean isFirstAcclReading = true;
    private boolean isUserOutside = false;
    private int numberTimers = 1;
    private Runnable task = new Runnable() {
            public void run() {

                // Logging to the DDMS (in the simulator, the DDMS log is to the console)
                System.out.println();
                Log.i( "ActivitySim" , "Timer " + numberTimers + ": Current simulator time: " +
                       convertUnixTimeToReadableString( ActivitySimulator.currentTimeMillis() ) );
                System.out.println( "Timer " + numberTimers + ": Current simulator time: " +
                                    convertUnixTimeToReadableString( ActivitySimulator.currentTimeMillis() ) );

                // Dummy example of outputting a detected activity
                //  (to the file "DetectedActivities.txt" in the trace folder).
                //  (here we just alternate between indoor and walking every 10 min)
                if( ! isUserOutside ) {
                    ActivitySimulator.outputDetectedActivity( UserActivities.IDLE_INDOOR );
                }
                else {
                    ActivitySimulator.outputDetectedActivity( UserActivities.WALKING );
                }
                isUserOutside = !isUserOutside;

                // Set a second timer to execute the same task 10 min later
                ++numberTimers;
                if( numberTimers <= 2 ) {
                    SimulatorTimer timer = new SimulatorTimer();
                    timer.schedule( task ,             // Task to be executed
                                    10 * 60 * 1000 );  // Delay in millisec (10 min)
                }
            }
        };

    private Runnable mainAlgo = new Runnable() {
        public void run() {
            //still requires termination criteria
            SimulatorTimer timer = new SimulatorTimer();
            timer.schedule( mainAlgo ,             // Task to be executed
                    1000 );  // Delay in millisec (1 sec)
            System.out.println("yup " + convertUnixTimeToReadableString( ActivitySimulator.currentTimeMillis() ));
        }
    };

    //Variables for Mag Stabilisation detection
    private boolean isFirstMagReading = true;
    private boolean isPhoneMoving = false;
    private long phoneMovedTimestamp = 0;
    private float[] magXvalues;
    private int mxAvg;
    private int magRunningAvgIndex;
    private static final int NUM_AVERAGES = 40;
    private static final int MX_THRESHOLD = 3;
    private int counter;

    private boolean isMagStillForDuration = true;

    //Variables for Loc Data processing
    private boolean isFirstLocReading = true;
    private double changeInDistance = 0;
    private float derivedSpeed = 0;
    private SVY21Coordinate previousCoord;
    private long previousCoordTimestamp;

    //Variables Lux Data processing
    private boolean isLowLight = false;
}