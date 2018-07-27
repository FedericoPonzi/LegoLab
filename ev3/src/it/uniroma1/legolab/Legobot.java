package it.uniroma1.legolab;

import it.uniroma1.legolab.ConnectionHandler.Analysis;
import it.uniroma1.legolab.behavior.DefaultBehavior;
import it.uniroma1.legolab.behavior.EscBehavior;
import it.uniroma1.legolab.behavior.GreenTrafficLightBehavior;
import it.uniroma1.legolab.behavior.RedTrafficLightBehavior;
import it.uniroma1.legolab.behavior.StopBehavior;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.internal.ev3.EV3LED;
import lejos.robotics.RangeFinderAdapter;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;

public class Legobot 
{
	private static EV3UltrasonicSensor us = new EV3UltrasonicSensor(SensorPort.S1);
	private static RangeFinderAdapter range = new RangeFinderAdapter(us);
	private EV3LED led = new EV3LED();
	private static MovePilotCustom pilot = new MovePilotCustom(5.6, // diametro delle ruote in cm
			12.0, // distanza tra le ruote in cm (interasse)
			Motor.C, // Left motor
			Motor.D // Right motor
	);
	private Behavior[] behaviors;
	private Arbitrator arbitrator;
	private ConnectionHandler conn;
	static {
		pilot.setLinearAcceleration(5); // accelerazione di 50 cm al secondo al
		// secondo
		pilot.setAngularAcceleration(80); // accelerazione nella rotazione 90°
				// al secondo al secondo
		pilot.setLinearSpeed(10); // max velocità 100 cm al secondo
		pilot.setAngularSpeed(100); // ruota di 90° al secondo
	}
	
	public Legobot(ConnectionHandler conn)
	{
		this.conn = conn;

		this.behaviors = new Behavior[]{ 
				new DefaultBehavior(this), 
				new StopBehavior(this), 
				new RedTrafficLightBehavior(this), 
				new GreenTrafficLightBehavior(this), 
				new EscBehavior(this) 
		};
		this.arbitrator = new Arbitrator(behaviors);
	}
	
	public void start()
	{
		arbitrator.go();
		System.out.println("Abirtrator is gone, thanks for playing!");
	}
	public Analysis getAnalysis() 
	{
		return conn.getAnalysis();
	}
	
	public void doStop() {
		if(!Main.DEBUG)
		pilot.stop();
	}
	public void doForward() {
		if(!Main.DEBUG)
			doForwardStep();
	}
	public void doForwardStep() {
		if(!Main.DEBUG)
			pilot.travel(5, true);
	}
	public void doForwardStep(int i) {
		if(!Main.DEBUG)
			pilot.travel(i, false);
	}
	
	public void exit() {
		pilot.stop();
		arbitrator.stop();
		System.exit(0);
	}
	
	public boolean hasObstacles() {
		boolean obst =  (range.getRange() < 25);
		System.out.println("Has Obstacles?" + obst);
		return obst; //Anything in a 20 cm range?
	}

	public void setLight(int pattern) {
		led.setPattern(pattern, led.PATTERN_ON);
		
	}

	

}
