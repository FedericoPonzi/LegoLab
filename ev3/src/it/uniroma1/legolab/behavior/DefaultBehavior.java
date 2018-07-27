package it.uniroma1.legolab.behavior;

import it.uniroma1.legolab.Legobot;
import it.uniroma1.legolab.Main;
import it.uniroma1.legolab.ConnectionHandler.Analysis;
import lejos.internal.ev3.EV3LED;

public class DefaultBehavior extends BehaviorAdapter
{
	public DefaultBehavior(Legobot legobot) {
		super(legobot);
	}
	public boolean takeControl() {
		return legobot.getAnalysis().equals(Analysis.NO_DETECTION);
	}
	public void action() {
		System.out.println("Running Default - forward behviour");
		legobot.setLight(EV3LED.COLOR_NONE);
		legobot.doForward();
		
	}
};
