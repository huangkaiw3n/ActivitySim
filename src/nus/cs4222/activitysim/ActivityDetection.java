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
//    	if((timestamp > 1459056316716L && timestamp < 1459056557845L) || (timestamp > 1459058194698L)){
//    		//System.out.format("%f\n", y);
//    	}
//	    try{
//	    	File file =new File("lightdata-timestamp.txt");
//	
//	    	if(!file.exists()){
//	    	   file.createNewFile();
//	    	}
//
//	    	FileWriter fw = new FileWriter(file,true);
//	    	BufferedWriter bw = new BufferedWriter(fw);
//	    	bw.write(String.format("%d\n", timestamp));
//	    	bw.close();
//
//      }	catch(IOException ioe){
//	         System.out.println("Exception occurred:");
//	    	 ioe.printStackTrace();
//       }
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

        if (timestamp <=1459053128109l)
            System.out.println(convertUnixTimeToReadableString(timestamp) + " " + x + " " + isMagStillForDuration + " " + isPhoneMoving + " " + isFluctuating);
    }

    /**
     Checks if Mag was still for more than xxx milliseconds.
     @param   timestamp    Timestamp of this sensor event
     */
    private void checkMagStill(long timestamp){

        if (timestamp - phoneMovedTimestamp > 64000 && !isPhoneMoving){ //phone still for more than x seconds
            isMagStillForDuration = true;
            isFluctuating = false;
            return;
        }
        else if (timestamp - phoneMovedTimestamp > 15000 && isPhoneMoving){ //phone moved for more than x seconds
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
        else if (timestamp - fluctuatingTimestamp > 40000){ //phone was unstable for more than x seconds
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
    	
//		if(timestamp > 1459055129717L && timestamp < 1459055561633L) {
//		if(timestamp > 1459058194698L) {
//	    	if(currAlt == 0 && prevAlt != 0) {
//	    		currAlt = altitude;
//	    		// compare here
//	    		if(Math.abs(currAlt - prevAlt) < stdDevAltWalk) {
//	    			walkBoolQ.add(true);
//	    		}
//	    		else if(Math.abs(currAlt - prevAlt) < stdDevAltVehicle) {
//	    			vehBoolQ.add(true);
//	    		}
//
//	    		if(vehBoolQ.size() + walkBoolQ.size() == ALTITUDE_LIST_SIZE_LIMIT){
//	    			if(vehBoolQ.size() > walkBoolQ.size()){
//	    				System.out.println("Vehicle");
//	    			}
//	    			else{
//	    				System.out.println("Walking");
//	    			}
//	    			walkBoolQ = new LinkedList<Boolean>();
//	    			vehBoolQ = new LinkedList<Boolean>();
//	    		}
//
//
//
//	    		prevAlt = currAlt;
//	    		currAlt = 0;
//	    	}
//	    	if(prevAlt == 0) {
//	    		prevAlt = altitude;
//	    	}
//		}
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

        debugLight = luxAvg;

        if (luxAvg < 279f)
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
                    isSpeedHigh = false;
                    slowBusTimestamp = timestamp;
                }
                if (timestamp - slowBusTimestamp > 1000 * 130){ //delay to decide user is now off vehicle
                    isOnVehicle = false;
                }
                isSpeedHigh = false;
            }
        }

//        System.out.println(convertUnixTimeToReadableString(timestamp) + " " + isOnVehicle + " " + derivedSpeed);

//            speed = derivedSpeed;
//
//            if (speed != -1) {
//                if (speedList.size() == SPEED_LIST_SIZE_LIMIT) {
//                    speedList.remove();
//                }
//                speedList.add(speed);
//                float averageSpeed = 0;
//                float totalSpeed = 0;
//                for (int i = 0; i < speedList.size(); i++) {
//                    totalSpeed = totalSpeed + speed;
//                }
//                averageSpeed = totalSpeed / speedList.size();
//
//                if (averageSpeed > AVERAGE_HUMAN_WALKING_SPEED) {
//                    System.out.format("%s\t%s\t%f\t%s\n", sdf.format(timestamp), "VEHICLE", speed, isOnBus);
//
//                } else if (averageSpeed > 0) {
//                    System.out.format("%s\t%s\t%f\t%s\n", sdf.format(timestamp), "WALKING", speed, isOnBus);
//
//                } else {
//                    System.out.format("%s\t%s\t%f\t%s\n", sdf.format(timestamp), "IDLE", speed, isOnBus);
//
//                }
//            }
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

    private void executeLater(Runnable toRun, int mililiseconds){
        SimulatorTimer timer = new SimulatorTimer();
        timer.schedule(toRun, mililiseconds);
    }

    private void vehicleOrWalking(){
        if(isOnVehicle) {
            ActivitySimulator.outputDetectedActivity(UserActivities.BUS);
            currentState = UserActivities.BUS;
            lastStateChangeTimestamp = ActivitySimulator.currentTimeMillis();
        }
        else {
            ActivitySimulator.outputDetectedActivity(UserActivities.WALKING);
            currentState = UserActivities.WALKING;
            lastStateChangeTimestamp = ActivitySimulator.currentTimeMillis();
        }
    }

    private void idlingIndoorOrOutdoor(){
        if(isLowLight) {
            ActivitySimulator.outputDetectedActivity(UserActivities.IDLE_INDOOR);
            currentState = UserActivities.IDLE_INDOOR;
            lastStateChangeTimestamp = ActivitySimulator.currentTimeMillis();
            System.out.println(debugLight);
        }
        else{
            ActivitySimulator.outputDetectedActivity(UserActivities.IDLE_OUTDOOR);
            currentState = UserActivities.IDLE_OUTDOOR;
            lastStateChangeTimestamp = ActivitySimulator.currentTimeMillis();

        }
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
                executeLater(mainAlgo, 70000);
                mainAlgoFirstRun = false;
                return;
            } else if (ActivitySimulator.currentTimeMillis() < 1459060560796l)
                executeLater(mainAlgo, 2000);

            switch (currentState){
                case NONE: {
                    if (isMagStillForDuration)
                        idlingIndoorOrOutdoor();
                    else
                        vehicleOrWalking();
                    break;
                }
                case IDLE_INDOOR: {
                    if (ActivitySimulator.currentTimeMillis() - lastStateChangeTimestamp < 20000)
                        return;
                    if (isMagStillForDuration) {
                        if (isLowLight)
                            return;
                        else
                            idlingIndoorOrOutdoor();
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
                    if (ActivitySimulator.currentTimeMillis() - lastStateChangeTimestamp < 20000)
                        return;
                    if (isMagStillForDuration) {
                        if (!isLowLight)
                            return;
                        else
                            idlingIndoorOrOutdoor();
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
                    if (ActivitySimulator.currentTimeMillis() - lastStateChangeTimestamp < 20000)
                        return;
                    if (!isMagStillForDuration) {
                        if (!isOnVehicle)
                            return;
                        else
                            vehicleOrWalking();
                    }
                    else
                        idlingIndoorOrOutdoor();
                    break;
                }
                case BUS: {
                    if (ActivitySimulator.currentTimeMillis() - lastStateChangeTimestamp < 20000)
                        return;
                    if (isOnVehicle)
                        return;
                    else if (isMagStillForDuration)
                        idlingIndoorOrOutdoor();
                    else
                        vehicleOrWalking();
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
    private static final int MX_THRESHOLD = 3;
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
    private static final float SPEED_THRESHOLD = 6;
    private boolean isOnVehicle = false;

    //Variables Lux Data processing
    private boolean isLowLight = false;
    private float[] luxValues;
    private boolean isFirstLuxReading = true;
    private static final int NUM_AVERAGES_LUX = 7;
    private float luxAvg;
    private int luxRunningAverageIndex;
    private int luxCounter;
	
    private Queue<Float> speedList = new LinkedList<Float>();
    private static final int SPEED_LIST_SIZE_LIMIT = 5;
    private static final double AVERAGE_HUMAN_WALKING_SPEED = 1.4;
    

    private static final int ALTITUDE_LIST_SIZE_LIMIT = 9;
	private float prevAlt = 0;
	private float currAlt = 0;
	private Queue<Boolean> walkBoolQ = new LinkedList<Boolean>();
	private Queue<Boolean> vehBoolQ = new LinkedList<Boolean>();
	
    private double stdDevAltWalk = 0.280311076;
    private double stdDevAltVehicle = 0.5416665;

    //Main algo
    private UserActivities currentState = UserActivities.NONE;
    private boolean mainAlgoFirstRun = true;
    private long slowBusTimestamp = 0;
    private long timeDiff;
    private long lastStateChangeTimestamp = 0;
    private float debugLight;
}
