package nus.cs4222.activitysim;

import java.io.*;
import java.util.*;

/**
   Simulator to simulate the sensor data collected on the phone.

   This is a single-threaded discrete event simulator. The purpose 
   of the simulator is two-fold:
   1. To easily port the activity detection algorithm code to an 
      Android app (later on).
   2. Take care of parsing the input sensor data files, and output 
      the detection results, in the correct format, for evaluating the 
      accuracy using 'ActivityEval'.

   Code your activity detection algorithm in the class 
    'ActivityDetection.java'. Then, compile and execute the simulator
    on the sensor data you collected.
   While executing the program, you must provide a path to the folder
    containing the sensor data (log files from the sdcard).

   Compiling:
   $ ant jarify
   Executing:
   $ java -jar ActivitySim.jar <path-to-your-trace-folder>
 */
public class ActivitySimulator {

    /** Main starting point of the simulator. */
    public static void main( String[] args ) {
        try {

            // Check the number of arguments
            if( args.length != 1 ) {
                System.err.println( "Usage: java -jar ActivitySim.jar <path-to-your-trace-folder>" );
                return;
            }

            // Store and check the trace folder
            checkTraceFolder( args[0] );
            // Create all the parsers for the sensor log files
            initParsers();
            // Open the log file for detected activities
            resultLogger.openLogFile( traceFolder , DETECTED_ACTIVITIES_LOG_FILENAME );

            // Initialise the simulator time
            simulatorTime = 0L;
            // Populate the event data structure with the first few sensor events
            populateFewEvents();
            // Run the simulator
            runSimulator();
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
        finally {
            // Close the detected activities log file
            resultLogger.closeLogFile();
        }
    }

    /** 
       Returns the simulator time.

       <p> This method returns the simulator or real time, depending on
       whether this code is running on the simulator or on the Android 
       device. The time is in millisecs since the epoch.

       <p> Note that time does not advance automatically in the 
       simulator, so DO NOT busy wait using this method.

       @return   Real or simulator time in millisec since epoch
     */
    public static long currentTimeMillis() {

        // In the simulator, we return a 'fake' time
        return simulatorTime;
    }

    /** Logs the specified detected activity with a timestamp. */
    public static void outputDetectedActivity( UserActivities activity ) {

        // Check the args
        if( activity == null ) {
            throw new NullPointerException( "Detected activity object cannot be null" );
        }

        // Log the detected activity (ONLY if it is different from the previous detection)
        if( ! activity.equals( prevDetectedActivity ) ) {
            resultLogger.logEvent( activity.toString() );
            prevDetectedActivity = activity;
        }
    }

    /** Stores the path to the trace folder, and checks if all traces are present. */
    private static void checkTraceFolder( String traceFolderPath ) {

        // Check if the path is a valid directory
        traceFolder = new File( traceFolderPath );
        if( ! traceFolder.isDirectory() ) {
            throw new IllegalArgumentException( "Invalid trace folder: \'" + 
                                                traceFolderPath + "\' is not a valid directory" );
        }

        // Check if the folder has all the sensor log files
        String[] logFilenameList = 
            new String[] { LOCATION_LOG_FILENAME , 
                           ACCELEROMETER_LOG_FILENAME , 
                           GRAVITY_LOG_FILENAME , 
                           LINEAR_ACCELEROMETER_LOG_FILENAME , 
                           MAGNETIC_LOG_FILENAME , 
                           GYROSCOPE_LOG_FILENAME , 
                           ROTATION_VECTOR_LOG_FILENAME , 
                           BAROMETER_LOG_FILENAME , 
                           LIGHT_LOG_FILENAME , 
                           PROXIMITY_LOG_FILENAME , 
                           GROUND_TRUTH_LOG_FILENAME };
        for( String logFilename : logFilenameList ) {
            File logFile = new File( traceFolder , logFilename );
            if( ! logFile.isFile() ) {
                throw new IllegalArgumentException( "Trace folder does not contain the log file \'" + 
                                                    logFilename + "\'" );
            }
        }
    }

    /** Initialises the parsers for all the sensor log files. */
    private static void initParsers() 
        throws IOException {

        // Add parsers for each sensor
        parserList.add( new LocationSensorLogParser( new File( traceFolder , LOCATION_LOG_FILENAME ) ) );
        parserList.add( new AcclSensorLogParser( new File( traceFolder , ACCELEROMETER_LOG_FILENAME ) ) );
        parserList.add( new GravitySensorLogParser( new File( traceFolder , GRAVITY_LOG_FILENAME ) ) );
        parserList.add( new LinAcclSensorLogParser( new File( traceFolder , LINEAR_ACCELEROMETER_LOG_FILENAME ) ) );
        parserList.add( new MagneticSensorLogParser( new File( traceFolder , MAGNETIC_LOG_FILENAME ) ) );
        parserList.add( new GyroscopeSensorLogParser( new File( traceFolder , GYROSCOPE_LOG_FILENAME ) ) );
        parserList.add( new RotationVectorSensorLogParser( new File( traceFolder , ROTATION_VECTOR_LOG_FILENAME ) ) );
        parserList.add( new BarometerSensorLogParser( new File( traceFolder , BAROMETER_LOG_FILENAME ) ) );
        parserList.add( new LightSensorLogParser( new File( traceFolder , LIGHT_LOG_FILENAME ) ) );
        parserList.add( new ProximitySensorLogParser( new File( traceFolder , PROXIMITY_LOG_FILENAME ) ) );
    }

    /** Populates the event data structure with the first sensor log events. */
    private static void populateFewEvents() 
        throws IOException {

        // For each parser, add the first sensor log event to the event data structure
        for( SensorLogParser parser : parserList ) {

            // Get the first sensor log event
            if( parser.hasNextEvent() ) {
                SimulatorEvent event = parser.getNextEvent();
                EventTuple eventTuple = new EventTuple( event , 
                                                        parser );
                // Add it to the event data structure
                eventSet.add( eventTuple );
            }
        }
    }

    /** Runs the simulator by consuming events from the event data structure. */
    private static void runSimulator() 
        throws IOException {

        // Consume each simulator event one by one until there are no more events.
        // The events are processed in the order of timestamps.
        while( ! eventSet.isEmpty() ) {

            // Get the earliest event
            EventTuple eventTuple = eventSet.first();
            eventSet.remove( eventTuple );
            SimulatorEvent event = eventTuple.event;
            SensorLogParser parser = eventTuple.parser;

            // For a sensor log event, add the next event to the event data structure
            if( parser != null && 
                parser.hasNextEvent() ) {
                SimulatorEvent nextEvent = parser.getNextEvent();
                EventTuple nextEventTuple = new EventTuple( nextEvent , 
                                                            parser );
                eventSet.add( nextEventTuple );
            }

            // Update the simulator time
            simulatorTime = event.getTimestamp();
            // Handle the event
            // NOTE: The event data structure may be modified in this call
            event.handleEvent();
        }
    }

    /** Gets the reference to the activity detection algorithm. */
    public static ActivityDetection getDetectionAlgorithm() {
        return detectionAlgo;
    }

    /** Adds a timer event. */
    public static void addTimerEvent( TimerEvent event ) {
        eventSet.add( new EventTuple( event , null ) );
    }

    /** Removes a timer event. */
    public static void removeTimerEvent( TimerEvent event ) {
        eventSet.remove( new EventTuple( event , null ) );
    }

    /** Event data structure (chosen to have log(n) insertion and deletion). */
    private static TreeSet< EventTuple > eventSet = 
        new TreeSet< EventTuple >();
    /** List of sensor log file parsers. */
    private static List< SensorLogParser > parserList = 
        new ArrayList< SensorLogParser >();

    /** Current simulator time (millisecs since epoch). */
    private static long simulatorTime;

    /** Detection algorithm. */
    private static ActivityDetection detectionAlgo = 
        new ActivityDetection();
    /** Folder containing the sensor traces. */
    private static File traceFolder;

    /** Tuple of (simulator event, sensor log parser). */
    private static class EventTuple 
        implements Comparable< EventTuple > {
        public SimulatorEvent event;
        public SensorLogParser parser;
        public EventTuple( SimulatorEvent event , 
                           SensorLogParser parser ) {
            this.event = event;
            this.parser = parser;
        }
        public int hashCode() {
            return event.hashCode();
        }
        public boolean equals( Object otherObject ) {
            EventTuple other = (EventTuple) otherObject;
            return this.event.equals( other.event );
        }
        public int compareTo( EventTuple other ) {
            return this.event.compareTo( other.event );
        }
    }

    /** Logger to log detected activities by the activity detection algorithm. */
    private static FileLogger resultLogger = new FileLogger();
    /** Previous detected activity logged (to avoid duplicates). */
    private static UserActivities prevDetectedActivity = UserActivities.Confirm;
    /** Name of the detected activities log file. */
    private static final String DETECTED_ACTIVITIES_LOG_FILENAME = "DetectedActivities.txt";

    // Sensor log filenames
    private static final String LOCATION_LOG_FILENAME = "Loc.txt";
    private static final String ACCELEROMETER_LOG_FILENAME = "Accl.txt";
    private static final String GRAVITY_LOG_FILENAME = "Gravity.txt";
    private static final String LINEAR_ACCELEROMETER_LOG_FILENAME = "LinAccl.txt";
    private static final String MAGNETIC_LOG_FILENAME = "Mag.txt";
    private static final String GYROSCOPE_LOG_FILENAME = "Gyro.txt";
    private static final String ROTATION_VECTOR_LOG_FILENAME = "RotVec.txt";
    private static final String BAROMETER_LOG_FILENAME = "Baro.txt";
    private static final String LIGHT_LOG_FILENAME = "Light.txt";
    private static final String PROXIMITY_LOG_FILENAME = "Proximity.txt";
    private static final String GROUND_TRUTH_LOG_FILENAME = "GroundTruth.txt";
}
