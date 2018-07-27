package it.uniroma1.legolab.behavior;

import it.uniroma1.legolab.Legobot;
import it.uniroma1.legolab.ConnectionHandler.Analysis;
import lejos.internal.ev3.EV3LED;

public class StopBehavior extends BehaviorAdapter {


	public StopBehavior(Legobot legobot) {
		super(legobot);
	}

	@Override
	public boolean takeControl() {
		return legobot.getAnalysis().equals(Analysis.SIGN_STOP);
	}

	@Override
	public void action() {	
		System.out.println("Running Stop behviour");
		legobot.setLight(EV3LED.COLOR_ORANGE);

//		legobot.doStop();
		legobot.doForwardStep(10);
		while(legobot.hasObstacles())
			legobot.doStop();
	}
};