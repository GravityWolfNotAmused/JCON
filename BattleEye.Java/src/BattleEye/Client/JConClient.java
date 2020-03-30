package BattleEye.Client;

import BattleEye.Command.BattleEyeCommandType;
import BattleEye.Socket.BattlEyeSocket;
import BattleEye.Socket.Listeners.BattlEyePacketListener;
import BattleEye.Socket.Listeners.BattlEyeQueueListener;
import BattleEye.Threading.JConReceivingThread;
import BattleEye.Threading.JConSendingThread;

import java.io.IOException;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;

public class JConClient {
    private Thread sendThread;
    private Thread receiveThread;
    private Timer connectionCommandTaskTimer;
    private BattlEyeSocket socket;

    public JConClient(String address, int port, String password, boolean debug) throws SocketException {
        socket = new BattlEyeSocket(address, port, password, debug);
        connectionCommandTaskTimer = new Timer();
        sendThread = new JConSendingThread(socket);
        receiveThread = new JConReceivingThread(socket);

        try {
            socket.connect();
            socket.login();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        connectionCommandTaskTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (socket.isConnected()) {
                    long lastTime = socket.getTimeSinceLastPacketSent();
                    long curTime = System.currentTimeMillis();
                    long timeSince = curTime - lastTime;

                    if(timeSince > 44000)
                    {
                        if (!socket.hasNextCommand())
                        {
                            socket.sendCommand(null);
                        }
                    }
                }
            }
        }, 0, 1000);

        sendThread.start();
        receiveThread.start();

        System.out.println("Send: " + sendThread.isAlive());
        System.out.println("Receive: " + receiveThread.isAlive());
        System.out.println("IsConnected: " + socket.isConnected());
    }

    public void addPacketListener(BattlEyePacketListener listener)
    {
        socket.addListener(listener);
    }

    public void addQueueListener(BattlEyeQueueListener listener)
    {
        socket.addQueueListener(listener);
    }

    public void sendCommand(String command) throws IOException {
        if (socket.isConnected()) {
            socket.sendCommand(command);
        }
    }
}