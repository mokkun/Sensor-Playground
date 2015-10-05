## About
This is a really simple app that tracks the user activity (walking, running, standing). 
The app consists of one `MainActivity`, two `Fragments` to display some data and one `Service`
that tracks the activity time and listen to sensor events to detect movement.

The main point of this sample app is just to show an application that uses such structure. 
Therefore, the app makes use of fusion sensors instead of using raw data from the hardware 
for the sake of simplicity (although some filtering is used).

The app doesn't make use of GPS for location accuracy/distance/speed tracking, the data displayed is 
an approximation and will not be exactly precise for all users. Also, since the user won't 
be facing the UI all the time while using the app, the data is not displayed in real time in favour
of precision.

## Requirements
Android 5.0 or newer. The app was tested on a Nexus 5 device, compatibility with other devices is 
unknown at the moment, but any device with an accelerator and gyroscope should work.

## Setup
The project uses the Gradle build system. Make sure you use `Android Studio` or that you have
`Gradle` installed on your computer.

Open a terminal and clone the existent repo:
```
git clone git@github.com:mokkun/Sensor-Playground.git
```
Enter the cloned directory:
```
cd Sensor-Playground
```
Compile the project on debug mode (if you desire to check the logs) and install it into the 
connected device:
```
./gradlew installDebug
```
And the last step, run the app:
```
adb shell am start -a android.intent.action.MAIN -n com.github.mokkun.playground/.activity.MainActivity
```

## License
```
Copyright 2015 Mozart Petter

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```