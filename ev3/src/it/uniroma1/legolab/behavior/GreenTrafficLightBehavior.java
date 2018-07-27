package it.uniroma1.legolab.behavior;

import it.uniroma1.legolab.ConnectionHandler.Analysis;
import it.uniroma1.legolab.Legobot;
import lejos.internal.ev3.EV3LED;

public class GreenTrafficLightBehavior extends BehaviorAdapter {

	
	public GreenTrafficLightBehavior(Legobot legobot) {
		super(legobot);
	}

	@Override
	public boolean takeControl() {
		return legobot.getAnalysis().equals(Analysis.SEMAPHORE_GREEN);
	}

	@Override
	public void action() {
		System.out.println("Running Green traffic light behviour");
		legobot.setLight(EV3LED.COLOR_GREEN);
		legobot.doForward();
	}
}