package BattleEye.Client;

import BattleEye.Command.BattlEyeCommandType;
import BattleEye.Login.BattlEyeLoginInfo;
import BattleEye.Socket.BattlEyeSocket;
import BattleEye.Socket.Listeners.BattlEyePacketListener;
import BattleEye.Socket.Listeners.BattlEyeQueueListener;

import java.io.IOException;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;

public class JConClient {
    private Timer receiveTaskTimer;
    private Timer sendTaskTimer;
    private Timer connectionCommandTaskTimer;

    private BattlEyeSocket socket;

    public final int MONITOR_TIME = 29000;
    public final int TIMEOUT_TIME = 10000;

    public JConClient(BattlEyeLoginInfo battlEyeLoginInfo, boolean debug) throws SocketException {
        if (battlEyeLoginInfo == null)
            throw new IllegalArgumentException("BattlEyeLoginInfo cannot be null");

        socket = new BattlEyeSocket(battlEyeLoginInfo.getAddress().getHostAddress(), battlEyeLoginInfo.getPort(), battlEyeLoginInfo.getPassword(), debug);

        connectionCommandTaskTimer = new Timer();
        receiveTaskTimer = new Timer();
        sendTaskTimer = new Timer();

        receiveTaskTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (socket.isConnected()) {
                    try {
                        socket.receiveCallback();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 0, 500);

        sendTaskTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (socket.isConnected()) {
                    try {
                        socket.sendNextPacket();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 0, 600);

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
                    long lastSentTime = socket.getTimeSinceLastPacketSent();
                    long lastReceived = socket.getTimeSinceLastPacketReceived();
                    long curTime = System.currentTimeMillis();
                    long timeSince = curTime - lastSentTime;

                    if (debug) {
                        System.out.println("Time Since: " + timeSince);
                    }

                    if (timeSince > MONITOR_TIME) {
                        if (!socket.hasNextCommand()) {
                            socket.sendCommand(null);
                        }
                    }
                }
            }
        }, 0, 1000);

        if (debug) {
            StringBuilder builder = new StringBuilder()
                    .append("[BattlEye]:: Socket IsConnected: " + socket.isConnected() + "\n");

            System.out.println(builder.toString());
        }
    }

    public JConClient(String address, int port, String password, boolean debug) throws SocketException {
        this(new BattlEyeLoginInfo(address, port, password), debug);
    }

    public JConClient(String address, int port, String password) throws SocketException {
        this(address, port, password, false);
    }

    public JConClient(BattlEyeLoginInfo battlEyeLoginInfo) throws SocketException {
        this(battlEyeLoginInfo, false);
    }

    public void addPacketListener(BattlEyePacketListener listener) {
        socket.addPacketListener(listener);
    }

    public void addQueueListener(BattlEyeQueueListener listener) {
        socket.addQueueListener(listener);
    }

    public void sendCommand(String command) {
        String[] cmd = command.split(" ");

        if (socket.isConnected()) {
            BattlEyeCommandType[] commands = BattlEyeCommandType.values();
            boolean isValidCommand = false;

            for(BattlEyeCommandType type : commands)
            {
                if(type.getCommandString().equals(cmd[0]))
                {
                    isValidCommand = true;
                    break;
                }
            }

            if(isValidCommand)
                socket.sendCommand(command);

            if(!isValidCommand)
                System.err.println("[BattlEye]:: Invalid Command! Command not sent to RCON.");
        }
    }
}