package it.uniroma1.legolab.behavior;

import it.uniroma1.legolab.Legobot;
import it.uniroma1.legolab.ConnectionHandler.Analysis;
import lejos.internal.ev3.EV3LED;

public class RedTrafficLightBehavior extends BehaviorAdapter {

	
	public RedTrafficLightBehavior(Legobot legobot) {
		super(legobot);
	}
	private boolean flag = true;
	@Override
	public boolean takeControl() {
		return legobot.getAnalysis().equals(Analysis.SEMAPHORE_RED);
			
	}

	@Override
	public void action() {
		System.out.println("Running Red Traffic Light behviour");
		legobot.setLight(EV3LED.COLOR_RED);
		while(!legobot.getAnalysis().equals(Analysis.SEMAPHORE_GREEN) && !legobot.getAnalysis().equals(Analysis.SIGN_STOP));
			legobot.doStop();
	}
};
