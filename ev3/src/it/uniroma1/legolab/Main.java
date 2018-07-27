package it.uniroma1.legolab;

import java.io.IOException;
import java.net.Socket;

import lejos.hardware.Sound;

public class Main {
	public static boolean DEBUG = false;
	public static void main(String[] args) throws IOException
	{
		ConnectionHandler conn = new ConnectionHandler(8888);
		Thread t = new Thread(conn);
		//t.run();
		t.start();
		
		Legobot legobot = new Legobot(conn);
		legobot.start();
		Sound.beepSequence();
	}	
}
