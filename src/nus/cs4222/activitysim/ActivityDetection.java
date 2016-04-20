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

    /** Initialises the detection algorithm. */
    public void initDetection() 
        throws Exception {
        // Add initialisation code here, if any

        // Here, we just show a dummy example of a timer that runs every 10 min, 
        //  outputting WALKING and INDOOR alternatively.
        // You will most likely not need to use Timers at all, it is just 
        //  provided for convenience if you require.
        // REMOVE THIS DUMMY CODE (2 lines below), otherwise it will mess up your algorithm's output
//        SimulatorTimer timer = new SimulatorTimer();
//        timer.schedule( this.task ,        // Task to be executed
//                        10 * 60 * 1000 );  // Delay in millisec (10 min)
    }

    /** De-initialises the detection algorithm. */
    public void deinitDetection() 
        throws Exception {
        // Add de-initialisation code here, if any
    }

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
        if (isFirstMagReading) {
            magXvalues = new float[NUM_AVERAGES_MX];
            magYvalues = new float[NUM_AVERAGES_MX];
            magRunningAvgIndex = 0;
            isFirstMagReading = false;
            magCounter = 0;
            phoneMovedTimestamp = timestamp;
            mainAlgo.run();
        }

        magXvalues[magRunningAvgIndex] = x;
        magYvalues[magRunningAvgIndex] = y;
        magRunningAvgIndex = (magRunningAvgIndex + 1) % NUM_AVERAGES_MX;

        if (magCounter < NUM_AVERAGES_MX-1){
            magCounter++;
            return;
        }

        double sumX, sumY;
        sumX = sumY = 0;
        for (float val : magXvalues) sumX += val;
        for (float val : magYvalues) sumY += val;
        mxAvg = (int) (sumX / NUM_AVERAGES_MX);
        myAvg = (int) (sumY / NUM_AVERAGES_MX);

        int diffX = Math.abs(mxAvg - (int) x);
        int diffY = Math.abs(myAvg - (int) y);

        if (diffX > MX_THRESHOLD || diffY > MX_THRESHOLD) {
            if (!isPhoneMoving) {
                isPhoneMoving = true;
                phoneMovedTimestamp = timestamp;
//                System.out.println("                    Moving!" + convertUnixTimeToReadableString( ActivitySimulator.currentTimeMillis() ));
            }
        }
        else {
            if (isPhoneMoving) {
                isPhoneMoving = false;
                phoneMovedTimestamp = timestamp;
//                System.out.println("                    Stable!" + convertUnixTimeToReadableString( ActivitySimulator.currentTimeMillis() ));
            }
        }

        phoneMovementRecord[magRunningAvgIndex] = isPhoneMoving;

        checkMagStill(timestamp);

//        if (timestamp <=1459053128109l)
//            System.out.println(convertUnixTimeToReadableString(timestamp) + " " + x + " " + isMagStillForDuration + " " + isPhoneMoving + " " + isFluctuating);
    }

    /**
     Checks if Mag was still for more than xxx milliseconds.
     @param   timestamp    Timestamp of this sensor event
     */
    private void checkMagStill(long timestamp){

        if (timestamp - phoneMovedTimestamp > MAG_STABLE_DURATION && !isPhoneMoving){ //phone still for more than x seconds
            isMagStillForDuration = true;
            isFluctuating = false;
            return;
        }
        else if (timestamp - phoneMovedTimestamp > MAG_MOVING_DURATION && isPhoneMoving){ //phone moved for more than x seconds
            isMagStillForDuration = false;
            isFluctuating = false;
            return;
        }

        if (currentState == UserActivities.NONE)
            return;

        if (!isFluctuating) {
            fluctuatingTimestamp = timestamp;
            isFluctuating = true;
        }
        else if (timestamp - fluctuatingTimestamp > MAG_FLUCTUATING_DURATION){ //phone was unstable for more than x seconds
            isMagStillForDuration = false;
            return;
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

        if (isFirstLuxReading){
            luxValues = new float[NUM_AVERAGES_LUX];
            isFirstLuxReading = false;
            luxRunningAverageIndex = 0;
            luxCounter = 0;
        }

        float sum = 0;

        luxValues[luxRunningAverageIndex] = light;
        luxRunningAverageIndex = (luxRunningAverageIndex + 1) % NUM_AVERAGES_LUX;

        if (luxCounter < NUM_AVERAGES_LUX){
            luxCounter++;
        }

        for (float val : luxValues) sum += val;
        luxAvg = (sum / luxCounter);

        if (luxAvg < LIGHT_THRESHOLD)
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

        if (provider.equals("gps") && isFirstLocReading){
            isFirstLocReading = false;
            previousCoord = new LatLonCoordinate(latitude,longitude).asSVY21();
            previousCoordTimestamp = timestamp;
        }

        if (provider.equals("gps") && !isFirstLocReading && timestamp != previousCoordTimestamp) {
            SVY21Coordinate currentCoord = new LatLonCoordinate(latitude, longitude).asSVY21();
            changeInDistance = Math.sqrt(((Math.pow(currentCoord.getEasting() - previousCoord.getEasting(), 2)) +
                    (Math.pow(currentCoord.getNorthing() - previousCoord.getNorthing(), 2))));
            derivedSpeed = (float) (changeInDistance / ((timestamp - previousCoordTimestamp) / 1000));
            previousCoord = currentCoord;
            previousCoordTimestamp = timestamp;
//            System.out.println("DistanceChange: " + changeInDistance + " " + "Speed: " + speed + " DerivedSpeed: " + derivedSpeed + " " + convertUnixTimeToReadableString(ActivitySimulator.currentTimeMillis()) + " Bus: " + isOnBus);

            if (derivedSpeed > SPEED_THRESHOLD){
                isSpeedHigh = true;
                isOnVehicle = true;
            }
            else {
                if (isSpeedHigh) {
                    slowBusTimestamp = timestamp;
                }
                if (timestamp - slowBusTimestamp > LOW_SPEED_DURATION){ //delay to decide user is now off vehicle
                    isOnVehicle = false;
                }
                isSpeedHigh = false;
            }
        }
    }

    /** Helper method to convert UNIX millis time into a human-readable string. */
    private static String convertUnixTimeToReadableString( long millisec ) {
        return sdf.format( new Date( millisec ) );
    }

    /** To format the UNIX millis time as a human-readable string. */
    private static final SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd-h-mm-ssa" );

    private void executeLater(Runnable toRun, int mililiseconds){
        SimulatorTimer timer = new SimulatorTimer();
        timer.schedule(toRun, mililiseconds);
    }

    private void outputStateVechicle(){
        ActivitySimulator.outputDetectedActivity(UserActivities.BUS);
        currentState = UserActivities.BUS;
        lastStateChangeTimestamp = ActivitySimulator.currentTimeMillis();
    }

    private void outputStateWalking(){
        ActivitySimulator.outputDetectedActivity(UserActivities.WALKING);
        currentState = UserActivities.WALKING;
        lastStateChangeTimestamp = ActivitySimulator.currentTimeMillis();
    }

    private void outputStateIdleIndoor(){
        ActivitySimulator.outputDetectedActivity(UserActivities.IDLE_INDOOR);
        currentState = UserActivities.IDLE_INDOOR;
        lastStateChangeTimestamp = ActivitySimulator.currentTimeMillis();
    }

    private void outputStateIdleOutdoor(){
        ActivitySimulator.outputDetectedActivity(UserActivities.IDLE_OUTDOOR);
        currentState = UserActivities.IDLE_OUTDOOR;
        lastStateChangeTimestamp = ActivitySimulator.currentTimeMillis();
    }

    private void vehicleOrWalking(){
        if(isOnVehicle)
            outputStateVechicle();
        else
            outputStateWalking();
    }

    private void idlingIndoorOrOutdoor(){
        if(isLowLight)
            outputStateIdleIndoor();
        else
            outputStateIdleOutdoor();
    }

    private boolean determineStability(){
        int positives = 0, negatives = 0;

        for (boolean val: phoneMovementRecord){
            if (val)
                positives++;
            else
                negatives++;
        }

        if (positives > negatives)
            return true;
        else
            return false;
    }

    private Runnable mainAlgo = new Runnable() {
        public void run() {
            if (mainAlgoFirstRun){
                executeLater(mainAlgo, MAINALGO_INITIAL_DELAY);
                mainAlgoFirstRun = false;
                return;
            }
            else
                executeLater(mainAlgo, MAINALGO_POLL_DELAY);

            switch (currentState){
                case NONE: {
                    if (isMagStillForDuration)
                        idlingIndoorOrOutdoor();
                    else
                        vehicleOrWalking();
                    break;
                }
                case IDLE_INDOOR: {
                    if (ActivitySimulator.currentTimeMillis() - lastStateChangeTimestamp < MAINALGO_STATECHANGE_MINTIME)
                        return;
                    if (isMagStillForDuration) {
                        if (isLowLight)
                            return;
                        else
                            outputStateIdleOutdoor();
                    } else if (!isFluctuating)
                        vehicleOrWalking();
                    else{
                        if(determineStability())
                            vehicleOrWalking();
                        else
                            idlingIndoorOrOutdoor();
                    }
                    break;
                }
                case IDLE_OUTDOOR: {
                    if (ActivitySimulator.currentTimeMillis() - lastStateChangeTimestamp < MAINALGO_STATECHANGE_MINTIME)
                        return;
                    if (isMagStillForDuration) {
                        if (!isLowLight)
                            return;
                        else
                            outputStateIdleIndoor();
                    } else if (!isFluctuating)
                        vehicleOrWalking();
                    else{
                        if(determineStability())
                            vehicleOrWalking();
                        else
                            idlingIndoorOrOutdoor();
                    }
                    break;
                }
                case WALKING: {
                    if (ActivitySimulator.currentTimeMillis() - lastStateChangeTimestamp < MAINALGO_STATECHANGE_MINTIME)
                        return;
                    if (!isMagStillForDuration) {
                        if (!isOnVehicle)
                            return;
                        else
                            outputStateVechicle();
                    }
                    else
                        idlingIndoorOrOutdoor();
                    break;
                }
                case BUS: {
                    if (ActivitySimulator.currentTimeMillis() - lastStateChangeTimestamp < MAINALGO_STATECHANGE_MINTIME)
                        return;
                    if (isOnVehicle)
                        return;
                    else if (isMagStillForDuration)
                        idlingIndoorOrOutdoor();
                    else
                        outputStateWalking();
                    break;
                }
                default:
                    break;
            }
        }
    };

    //Variables for Mag Stabilisation detection
    private boolean isFirstMagReading = true;
    private boolean isPhoneMoving = false;
    private long phoneMovedTimestamp = 0;
    private float[] magXvalues, magYvalues;
    private int mxAvg, myAvg;
    private int magRunningAvgIndex;
    private static final int NUM_AVERAGES_MX = 40;
    private int magCounter;
    private long fluctuatingTimestamp = 0;
    private boolean isFluctuating = false;
    private boolean isMagStillForDuration = true;
    private boolean[] phoneMovementRecord = new boolean[40];

    //Variables for Loc Data processing
    private boolean isFirstLocReading = true;
    private double changeInDistance = 0;
    private float derivedSpeed = 0;
    private SVY21Coordinate previousCoord;
    private long previousCoordTimestamp;
    private boolean isSpeedHigh = false;
    private boolean isOnVehicle = false;

    //Variables Lux Data processing
    private boolean isLowLight = false;
    private float[] luxValues;
    private boolean isFirstLuxReading = true;
    private static final int NUM_AVERAGES_LUX = 7;
    private float luxAvg;
    private int luxRunningAverageIndex;
    private int luxCounter;

    //Main algo
    private UserActivities currentState = UserActivities.NONE;
    private boolean mainAlgoFirstRun = true;
    private long slowBusTimestamp = 0;
    private long lastStateChangeTimestamp = 0;

    /* ------------ Delays AND Variables for Tweaking Algo Performance ---------- */
    /* -------------------------------------------------------------------------- */

    //User is moving if the current mag x or current mag y values differs with the
    //running average of the mag x or mag y values by this threshold.
    private static final int MX_THRESHOLD = 2;

    //User is considered to be Idling if mag was completely stable (not moving) for this duration
    private static final int MAG_STABLE_DURATION = 25000;

    //User is either Walking or Vechicle if mag was continuously unstable (Moving) for this duration
    private static final int MAG_MOVING_DURATION = 15000;

    //If Mag was not completely stable nor was it continuously unstable after this duration, then mag is fluctuating.
    //Decision of User is idling or not will then be based on whether the majority of the previous readings was Moving
    //or not moving.
    private static final int MAG_FLUCTUATING_DURATION = 40000;

    //If speed has been low for longer than this duration, user is considered to be off vehicle
    private static final float LOW_SPEED_DURATION = 125000;

    //User is on vehicle if speed exceeds this threshold. If speed is lower than this threshold for LOW_SPEED_DURATION,
    //user is no longer on vechicle
    private static final float SPEED_THRESHOLD = 5.5f;

    //If light is below this threshold, user is indoors, else, outdoors.
    private static final float LIGHT_THRESHOLD = 350f;

    //Initial delay to collect sensory data before outputing the first state
    private static final int MAINALGO_INITIAL_DELAY = 30000;

    //Rate the main algo is called, i.e, every 2000ms.
    private static final int MAINALGO_POLL_DELAY = 2000;

    //Minimum elapsed time to allow a state change after the state has just been updated
    private static final int MAINALGO_STATECHANGE_MINTIME = 20000;

    /* -------------------------------------------------------------------------- */
    /* -------------------------------------------------------------------------- */
}
