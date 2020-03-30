package BattleEye.Client;

import BattleEye.Socket.BattlEyeSocket;
import BattleEye.Socket.Listeners.BattlEyePacketListener;
import BattleEye.Socket.Listeners.BattlEyeQueueListener;
import BattleEye.Threading.JConReceivingThread;
import BattleEye.Threading.JConSendingThread;

import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;

public class JConClient {
    private Thread sendThread;
    private Thread receiveThread;
    private Timer connectionCommandTaskTimer;
    private BattlEyeSocket socket;

    public final int MONITOR_TIME = 30000; //Will Trigger at 31 Seconds.

    public JConClient(String address, int port, String password, boolean debug) {
        try {
            socket = new BattlEyeSocket(address, port, password, debug);
        } catch (SocketException e) {
            e.printStackTrace();
            System.err.println("[BattlEye]:: Exiting client...");
            System.exit(-1);
        }

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

                    if(debug)
                        System.out.println("Time Since: " + timeSince);

                    if (timeSince > MONITOR_TIME) {
                        if (!socket.hasNextCommand()) {
                            socket.sendCommand(null);
                        }
                    }
                }
            }
        }, 0, 1000);

        sendThread.start();
        receiveThread.start();

        if(debug) {
            StringBuilder builder = new StringBuilder()
                    .append("[BattlEye]:: Sending Thread IsAlive: " + sendThread.isAlive() + "\n")
                    .append("[BattlEye]:: Receive Thread IsAlive: " + receiveThread.isAlive() + "\n")
                    .append("[BattlEye]:: Socket IsConnected: " + socket.isConnected() + "\n");

            System.out.println(builder.toString());
        }
    }

    public void addPacketListener(BattlEyePacketListener listener) {
        socket.addPacketListener(listener);
    }

    public void addQueueListener(BattlEyeQueueListener listener) {
        socket.addQueueListener(listener);
    }

    public void sendCommand(String command) {
        if (socket.isConnected()) {
            socket.sendCommand(command);
        }
    }
}