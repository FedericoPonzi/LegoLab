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
