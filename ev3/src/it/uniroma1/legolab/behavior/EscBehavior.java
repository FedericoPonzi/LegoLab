package it.uniroma1.legolab.behavior;

import it.uniroma1.legolab.Legobot;
import lejos.hardware.Button;
import lejos.hardware.Sound;

public class EscBehavior extends BehaviorAdapter
{
	public EscBehavior(Legobot legobot) {
		super(legobot);
	}

	public boolean takeControl() {
		return Button.ESCAPE.isDown();
	}

	public void action() {
		System.out.println("Esc behaviour. Goodbye!");

		Sound.beepSequenceUp();
		legobot.exit();
	}

}
