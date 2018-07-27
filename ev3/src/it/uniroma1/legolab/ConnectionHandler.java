package it.uniroma1.legolab;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionHandler implements Runnable{

	private int port;


	private ObjectInputStream oin = null;
	private ObjectOutputStream oout = null;


	private Analysis currentAnalysis = Analysis.NO_DETECTION;

	public ConnectionHandler(int port)
	{
		this.port = port;
		
	}
	
	public enum Analysis {
        NO_DETECTION,
        SEMAPHORE_RED,
        SEMAPHORE_GREEN,
        SIGN_STOP;
    }
	
	public Analysis getAnalysis()
	{
		return this.currentAnalysis;
	}
	
	public void runServer() throws IOException
	{
		System.out.println("Waiting for connection..");

		ServerSocket serversocket = new ServerSocket(this.port);
		Socket socket = serversocket.accept();

		oout = new ObjectOutputStream(socket.getOutputStream());
		oin = new ObjectInputStream(socket.getInputStream());
		
		
		System.out.println("READING PARAMS");
		while(true)
		{
			int analysis = oin.readInt();
			System.out.println("Ricevuto : " + analysis);
			currentAnalysis = Analysis.values()[analysis];
		}
		
	}

	@Override
	public void run() {
		try {
			this.runServer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
	}
}
