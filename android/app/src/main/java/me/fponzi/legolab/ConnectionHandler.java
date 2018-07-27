package me.fponzi.legolab;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ConnectionHandler implements Runnable
{
    private static final String TAG = ConnectionHandler.class.getSimpleName();
    private static final int N_MILLISECS = 1000;

    private static final int SERVER_PORT = 8888;
    private static final String SERVER_IP = "10.0.1.1";
    private final Legobot legobot;

    //private ObjectInputStream ois = null;
    private ObjectOutputStream oos = null;
    private void setup() throws IOException
    {
        Socket socket = new Socket(SERVER_IP, SERVER_PORT);
        oos = new ObjectOutputStream(socket.getOutputStream());


        Log.i(TAG, "CONNECTED");
        //ois = new ObjectInputStream(socket.getInputStream());
        Log.i(TAG, "OOS CREATED");


        Timer timer = new Timer();
        timer.schedule(new SendAction(), 0, N_MILLISECS);
    }

    public ConnectionHandler(Legobot legobot)
    {
        this.legobot = legobot;

    }
    /**
     * Sends the action to the robot for the execution every N_MILLISECS.
     */

    private void sendAction() {
        try {
            Legobot.Analysis  a = legobot.getAnalysis();
            Log.d(TAG, "Sending Analysis: " + a);
            oos.writeInt(a.ordinal());
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        try {
            setup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class SendAction extends TimerTask {
        public void run() {
            Log.i(TAG, "Called sendaction...");
            sendAction();
        }
    }

}
