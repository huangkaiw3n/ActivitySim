Group 12
Huang Kaiwen (A0096760W)
Lim Si Hui (A0100566N)
Liu Chengkai (A0101923U)

Activity Eval Summary:

* DURATION of the trace: 2.09 hrs (125.18 min)
* CONFUSION MATRIX (Rows are ground truth, columns are detected states):
Truth/Detect  IDLE_INDOOR IDLE_OUTDOOR      WALKING      VEHICLE        OTHER
 IDLE_INDOOR       97.05%        0.00%        0.00%        0.00%        2.95%
IDLE_OUTDOOR        0.00%       87.42%       12.58%        0.00%        0.00%
     WALKING        2.44%        0.11%       89.71%        7.75%        0.00%
     VEHICLE        0.00%        0.00%        4.46%       95.54%        0.00%
* ACCURACY:
	IDLE_INDOOR (33.37 min [0.56 hrs]): 97.05%
	IDLE_OUTDOOR (17.35 min [0.29 hrs]): 87.42%
	WALKING (30.77 min [0.51 hrs]): 89.71%
	VEHICLE (43.70 min [0.73 hrs]): 95.54%
	OVERALL Accuracy: 93.38%
* LATENCY:
	IDLE_INDOOR: 0.97 min (58.19 sec) [missed 0 out of 1]
	IDLE_OUTDOOR: 2.18 min (130.85 sec) [missed 0 out of 1]
	WALKING: 1.05 min (62.80 sec) [missed 0 out of 3]
	VEHICLE: 0.97 min (57.92 sec) [missed 0 out of 2]

The sensors that are in use for our algorithm are the Magnetometer, the Light Sensor, and the Location Data services (GPS only).

The states to be detected are IDLE_INDOOR, IDLE_OUTDOOR, WALKING, and BUS.
We added an additional state called NONE as the initial state to indicate that the algorithm has just started and needs to read in data for a while before outputting a detected state.

Magnetometer:
Detection of motion is done using the magnetometer values. Using the x and y values of the magnetometer, a running average of the x and y values are tracked and compared with the current x and y values. A deviation of the x and y values that is larger than our threshold would indicate there is motion (isPhoneMoving).

If the phone was not moving completely for A seconds, then the isMagStillForDuration would be true. If the phone was moving continuosly for B seconds, then isMagStillForDuration would be false. Otherwise, if the phone was fluctuating between still and movement for C seconds, isMagstillForDuration would be false and isFluctuating will be true.

Location Service:
Using purely gps data received, the user's speed is calculated using the distance and time between consecutive readings. The speed data provided by the GPS is not used as it does not seem to be accurate and fine enough. When the speed exceeds a threshold, isOnVehicle is set to true. However, when speed falls below the threshold, isOnVehicle is not set to false until D seconds later. This is to accomodate the times when the vehicle is stationary.

Light Sensor:
The light sensor data is used to determine whetehr the user is outdoor or indoor when IDLE is detected. Currently, a small running average of the 7 latest lux values are taken and compared with a threshold value E to determine outdoors or indoors, setting the boolean, isLowLight.

Main Algorithm:
At startup, the app is allowed to run for 70 seconds before deciding the first state. After that, the main algo function is called every 2 seconds. The format of the main algo function is a state machine.

On state NONE:
When isMagStillForDuration is true, user is considered to be IDLE, and OUTDOOR or INDOOR is determined by isLowLight.
When isMagStillForDuration is false, the user is considered to be walking or on bus determined by isOnVehicle or not.

On state IDLE_INDOOR or IDLE_OUTDOOR:
When isMagStillForDuration is true, and the light has not changed, the state is kept same.
When isMagStillForDuration is false, and isFluctuating is false, user is considered to be either walking or on bus, determined by isOnVehicle or not.
When isMagStillForDuration is false, and isFluctuating is true, user is considered to be either walking or on bus if the majority of the latest 40 isPhoneMoving readings are true, otherwise, user is considered to be still idling.

On state WALKING:
When isMagStillForDuration is false, and isOnVehicle is false, the state is kept same.
When isMagStillForDuration is true, the user is considered to be IDLE, and OUTDOOR or INDOOR is determined by isLowLight. 

On state BUS:
When isOnVehicle is true, the state is kept same.
Else, if isMagStillForDuration is true, the user will be considered to be IDLE, and OUTDOOR or INDOOR is determined by isLowLight.
If isMagStillForDuration is false, the user will be consdiered to be walking.

Currently, there is also a minimum time of 20 seconds before state is allowed to be changed in the main algo.