package BattleEye.Client;

import BattleEye.Command.BattlEyeCommandType;
import BattleEye.Logger.BLogger;
import BattleEye.Logger.BattlEyeLogger;
import BattleEye.Login.BattlEyeLoginInfo;
import BattleEye.Socket.BattlEyeSocket;
import BattleEye.Socket.Listeners.BattlEyePacketListener;
import BattleEye.Socket.Listeners.BattlEyeQueueListener;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class JConClient {
    private Timer receiveTaskTimer;
    private Timer sendTaskTimer;
    private Timer connectionCommandTaskTimer;

    private BattlEyeSocket socket;

    public final int MONITOR_TIME = 30000;
    public final int TIMEOUT_TIME = 10000;

    public final int SEND_TICK = 1;
    public final int RECEIVE_TICK = 1;
    public final int CONNECTION_TICK = 1000;

    public JConClient(BattlEyeLoginInfo battlEyeLoginInfo, boolean debug) throws IOException {
        if (battlEyeLoginInfo == null)
            throw new IllegalArgumentException("BattlEyeLoginInfo cannot be null");

        if(battlEyeLoginInfo.getAddress().getHostAddress().isEmpty())
            throw new IllegalArgumentException("Host Address cannot be empty.");

        if(battlEyeLoginInfo.getAddress().getHostAddress().isBlank())
            throw new IllegalArgumentException("Host Address cannot be blank.");

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
        }, 0, RECEIVE_TICK);

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
        }, 0, SEND_TICK);

        socket.connect();
        socket.login();

        connectionCommandTaskTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (socket.isConnected()) {
                    long lastSentTime = socket.getTimeSinceLastPacketSent();
                    long lastReceived = socket.getTimeSinceLastPacketReceived();
                    long curTime = System.currentTimeMillis();
                    long timeSince = curTime - lastSentTime;

                    if (debug) {
                        //System.out.println("Time Since: " + timeSince);
                    }

                    if (timeSince > MONITOR_TIME) {
                        if (!socket.hasNextCommand()) {
                            socket.sendCommand(null);
                        }
                    }
                }
            }
        }, 0, CONNECTION_TICK);

        if (debug)
            BattlEyeLogger.GetLogger().log("Socket IsConnected: " + socket.isConnected());
    }

    public JConClient(String address, int port, String password, boolean debug) throws IOException {
        this(new BattlEyeLoginInfo(address, port, password), debug);
    }

    public JConClient(String address, int port, String password) throws IOException {
        this(address, port, password, false);
    }

    public JConClient(BattlEyeLoginInfo battlEyeLoginInfo) throws IOException {
        this(battlEyeLoginInfo, false);
    }

    public void addPacketListener(BattlEyePacketListener listener) {
        socket.addPacketListener(listener);
    }

    public void addQueueListener(BattlEyeQueueListener listener) {
        socket.addQueueListener(listener);
    }

    public void sendCommand(String command) {
        if (socket.isConnected()) {
            boolean isValidCommand = BattlEyeCommandType.isValidCommand(command);

            if (isValidCommand) {
                socket.sendCommand(command);
            }

            if (!isValidCommand) {
                BattlEyeLogger.GetLogger().error("Invalid Command! Command not sent to RCON.");
            }
        }
    }
}