
# LegoLab 2017-2018
 * Maria Giulia Cecchini 
 * [Federico Ponzi](https://fponzi.me)
 
[Prof. Sterbini](http://twiki.di.uniroma1.it/twiki/view/Users/AndreaSterbini) - [Legolab course page](http://twiki.di.uniroma1.it/twiki/view/Legolab/WebHome)

## Aim of the project
Realize a prototype of a simple autonomous-car, using computer vison and the Lego EV3 kit.

## Overview of the system
 <img src="http://i63.tinypic.com/33zbln8.jpg">
For the hardware part, we choose to do all the heavy computer vision computation part on an android handheld device packed with a 13mp camera, communicating via bluetooth with the EV3.
The software architecture we made is capable, at the moment, of handling a stop signal, and a semaphore - but the system is easily extensible with new capabilities.
 
# Part 1 : Android App
The Android App is made in Java, using the OpenCV library.
The app is made of one MainActivity, and some classes.

After the initialization of the OpenCV library, the app enters in the main loop.
The class `ConnectionHandler` is, as the name suggest, in charge of handling the connection. It extends the Thread class and run in a separate thread in order to not block the main UI thread.
 
### Detection loop
After getting a new frame, the detection loop will loops all the detectors and adds the findings to the LegoBot class. This loop is executed - hopefully - many times per second:
```java
private void detect() {  
    Imgproc.equalizeHist(mGray, mGray);  
 if(mDetectors == null) return;  
 for(AbstractDetector detector : mDetectors)  
    {  
            d = detector.detect(mRgba, mGray);  
 if(!d.equals(Legobot.Analysis.NO_DETECTION))  
            {  
                legobot.addAnalysis(d);  
  }  
    }  
}
```
### Detectors
A detector interface, is defined as: 
```java
public abstract class AbstractDetector{
 public abstract Legobot.Analysis detect(Mat mRgba, Mat mGray);
}
 ```
 This abstract class defines also helpful methods for debugging purposes.
In order to add a new detector, one should:
 * Extend the `AbstractDetector` class, and implement the detect method.
 * Add a new element in the `Legobot.Analysis` enum.

Legobot.Analysis defines the possible findings - with a color for debug purposes:
```java
NO_DETECTION(new Scalar(0, 0, 0)),
SEMAPHORE_RED(new Scalar(255, 0, 0)),  
SEMAPHORE_GREEN(new Scalar(83, 244, 66)),  
SIGN_STOP(new Scalar(0, 255, 0));
```
#### Stop detector
<p align="center"><img src="https://raw.githubusercontent.com/FedericoPonzi/LegoLab/master/media/physical-stop-signal.jpg" height="300"></p>

The stop detector is implemented through an Haar classifier, taken from [here](https://github.com/cfizette/road-sign-cascades/blob/master/Stop%20Signs/StopSign_HAAR/Stopsign_HAAR_19Stages.xml).
The classifier is a result of 19 stages of training. Even though there are many over available classifier for stop sign online - and one can train its own - this was considered good enough.

After loading the classifier:
```java
String stopSignPath = Utils.copyFromRaw(context, R.raw.stopsign_classifier, "stop_sign_haar");  
mClassifier = new CascadeClassifier(stopSignPath);  
```  
We can use it using the detect multiscale method:
```java
mClassifier.detectMultiScale(mGray, 
							stopDetected,  
							1.1, 
							2, 
							Objdetect.CASCADE_SCALE_IMAGE  
							  | Objdetect.CASCADE_FIND_BIGGEST_OBJECT,
							new Size(120,120), 
							new Size(360,360));
 ```
 In principle, it's possible to add new classifiers trained to detect more street signs and create a more complete system

#### Semaphore Detector
<p align="center"><img src="https://raw.githubusercontent.com/FedericoPonzi/LegoLab/master/media/physical-traffic-light.jpg" height="300"></p>

The semaphore detector is handmade, using opencv. The idea is to:
 * Move from rgb to hsv color space:
 ```java
 Imgproc.cvtColor(mRgb, hsv, Imgproc.COLOR_RGB2HSV);
 ```
 * Blur the image:
 ```java
 Imgproc.GaussianBlur(hsv, hsv, new Size(5, 5), 0);  
Imgproc.medianBlur(hsv, hsv, 5);
```
 * Remove all the pixel not in the desired colorspace. For each pixel, this computes "white if pixel is in range, black otherwise".
 ```java
 Core.inRange(hsv, greenColors.get(0).first,  
  greenColors.get(0).second, hsv);
  ```
 * Apply the HoughCircles algorithm to detect circles.
 ```java
 Imgproc.HoughCircles(hsv, circles, Imgproc.HOUGH_GRADIENT, 2,
                      hsv.size().height / 4, 100, 30, (int)hsv.size().height/24,
                      (int) hsv.size().height/15);
  ```
  The parameters are tuned accordingly, in order to detect the traffic light not too close or too far back.
  
### Communication
The `ConnectionHandler` class run in another thread and is constructed with a reference of the Legobot object. It sets up a socket to the EV3, and create another Timer thread. 
```java
Timer timer = new Timer();  
timer.schedule(new SendAction(), 0, N_MILLISECS);
```
Every `N_MILLISECS`, the ConnectionHandler will ask Legobot for an Analysis to send:
```java
Legobot.Analysis  a = legobot.getAnalysis();
```
Legobot will return the `Legobot.Analysis` most detected in the last second - and the `ConnectionHandler` will send it to the EV3.

With a *100% rate detection* system one could in principle send the Analysis as soon as something is detected. Since this is not the case, we create a summary of findings of the last second to enhance the resiliency of the detection process.
 
# Part 2: EV3 Bot
<p align="center"><img src="https://raw.githubusercontent.com/FedericoPonzi/LegoLab/master/media/ev3-1.jpg" width="30%"><img src="https://raw.githubusercontent.com/FedericoPonzi/LegoLab/master/media/ev3-2.jpg" width="30%"><img src="https://raw.githubusercontent.com/FedericoPonzi/LegoLab/master/media/ev3-3.jpg" width="30%"></p>

While the Android App has the duty to analyse the environment and detect the street signs, the EV3 has to implement the behaviour based on the findings.

## Behaviour programming
The behaviour programming is a design pattern which offers a valid alternative to if-else. 
From the [Lejos documentation](http://www.lejos.org/nxt/nxj/tutorial/Behaviors/BehaviorProgramming.htm):
> The concepts of Behavior Programming as implemented in leJOS NXJ are very simple: 
> -   Only one behavior can be active and in control of the robot at any time.
> -   Each behavior has a fixed priority.
> -   Each behavior can determine if it should take control.
> -   The active behavior has higher priority than any other behavior that should take control.

### Behaviours
We have defined a package `it.uniroma1.legolab.behaviors` with a convenient abstract adapter class:
```java
package it.uniroma1.legolab.behavior;

import it.uniroma1.legolab.Legobot;
import it.uniroma1.legolab.MovePilotCustom;
import lejos.robotics.subsumption.Behavior;

public abstract class BehaviorAdapter implements Behavior
{
	Legobot legobot;
	public BehaviorAdapter(Legobot legobot)
	{
		this.legobot = legobot;
	}
	@Override
	public boolean takeControl() {
		return false;
	}

	@Override
	public void action() {
	}
	@Override
	public void suppress() {	
	}
}
```
The behaviour classes then, look like an if-then class:
```java
public class GreenTrafficLightBehavior extends BehaviorAdapter {
	@Override
	public boolean takeControl() {
		return legobot.getAnalysis().equals(Analysis.SEMAPHORE_GREEN);
	}
	@Override
	public void action() {
		legobot.doForward();
	}
}
```
The behaviour classes takeControl in the order defined in the Legobot class:
```java
		this.behaviors = new Behavior[]{ 
				new DefaultBehavior(this), 
				new StopBehavior(this), 
				new RedTrafficLightBehavior(this), 
				new GreenTrafficLightBehavior(this), 
				new EscBehavior(this) 
		};
```

### Communication
In the EV3 project, we can find the "other side" of the`ConnectionHandler` class. 
This class creates a server socket, and listen to incoming connections on port `8888`.
The idea is that in principle we could connect multiple input sensors (e.g. more phones/cameras, temperature, light etc), in order to realize even more complex behaviours.

The communication protocol is very simple, and is based on the Analysis Enum:
```java
public enum Analysis {
        NO_DETECTION,
        SEMAPHORE_RED,
        SEMAPHORE_GREEN,
        SIGN_STOP;
    }
 ```
 The android app passes the `Analysis.ordinal()` to the EV3, using an `ObjectInputStream`.
 
 ### Video of an example run:
 A video of the Legobot in action is available [here](https://www.youtube.com/watch?v=RuUpUt2jXuI).
<p align="center">

[![LegoLab](https://img.youtube.com/vi/RuUpUt2jXuI/0.jpg)](https://www.youtube.com/watch?v=RuUpUt2jXuI)

</p>

 There is also a video from the prespective of the [car](https://www.youtube.com/watch?v=fPX_oRDAnWo).
 
 <p align="center">
	
[![LegoLab](https://img.youtube.com/vi/fPX_oRDAnWo/0.jpg)](https://www.youtube.com/watch?v=fPX_oRDAnWo)
 </p>

 
 ## HSV color picker
 In order to find the right color range, we used an HSV threshold color picker:
 <p align="center">
 <img src="https://raw.githubusercontent.com/FedericoPonzi/LegoLab/master/media/hsv-colour.png">
 </p>
 
The source code is available [here](https://gist.github.com/FedericoPonzi/1728542ae0c8057e43658a3216e385c5).


