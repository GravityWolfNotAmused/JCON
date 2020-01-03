package BattleEye.Socket;

import BattleEye.Command.BattleEyeCommandType;
import BattleEye.Login.BattleEyeLoginInfo;
import BattleEye.NumberIncrementer;
import BattleEye.Socket.Listeners.BattlEyeCommandListener;
import BattleEye.Socket.Listeners.BattlEyeCommandQueueListener;
import BattleEye.Socket.Listeners.BattlEyeMessageListener;
import BattleEye.Socket.Listeners.BattlEyePacketListener;
import BattleEye.Socket.Listeners.DefaultListeners.DefaultBattlEyeCommandListener;
import BattleEye.Socket.Listeners.DefaultListeners.DefaultBattlEyeCommandQueueListener;
import BattleEye.Socket.Listeners.DefaultListeners.DefaultBattlEyeMessageListener;
import BattleEye.Socket.Listeners.DefaultListeners.DefaultBattlEyePacketListener;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BattlEyeSocket implements BattleSocket {
    private DatagramSocket socket;
    private BattleEyeLoginInfo loginInformation;
    private static NumberIncrementer incrementer;
    private BattlEyeListenerManager listeners;
    private ConcurrentLinkedQueue<BattlEyeCommand> commandQueue;

    private boolean isDebug = false;

    public BattlEyeSocket(String address, int port, String password, boolean debug) throws SocketException {
        loginInformation = new BattleEyeLoginInfo(address, port, password);
        listeners = new BattlEyeListenerManager();
        commandQueue = new ConcurrentLinkedQueue<>();

        socket = new DatagramSocket();
        incrementer = new NumberIncrementer();
        isDebug = debug;
        initListeners();
    }

    public BattlEyeSocket(String address, int port, String password) throws SocketException {
        loginInformation = new BattleEyeLoginInfo(address, port, password);
        listeners = new BattlEyeListenerManager();
        commandQueue = new ConcurrentLinkedQueue<>();

        socket = new DatagramSocket();
        incrementer = new NumberIncrementer();
        initListeners();
    }

    private void initListeners()
    {
        addPacketListener(new DefaultBattlEyePacketListener(isDebug));
        addMessageListener(new DefaultBattlEyeMessageListener(isDebug));
        addCommandListener(new DefaultBattlEyeCommandListener(isDebug));
        addCommandQueueListener(new DefaultBattlEyeCommandQueueListener(isDebug));
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    @Override
    public boolean connect() throws IOException {
        socket.connect(loginInformation.getAddress(), loginInformation.getPort());

        if (isConnected()) {
            System.out.println("[BattlEye]:: Connected to " + loginInformation.getAddress().getHostAddress());
        }

        return isConnected();
    }

    @Override
    public void login() throws IOException, InterruptedException {
        String passwordBytes = loginInformation.getPassword();

        BattlEyeCommand loginCommand = new BattlEyeCommand(passwordBytes);
        loginCommand.setSequence((byte) -1);
        loginCommand.generatePacket(BattleEyeCommandType.LOGIN);

        if (isConnected()) {
            queueCommand(loginCommand);
        }
        Thread.sleep(1000);
    }

    @Override
    public void sendCommand(String command) throws IOException {
        /* Get bytes of the command */
        byte[] commandBytes = null;

        if (!command.isEmpty())
            commandBytes = command.getBytes(StandardCharsets.UTF_8);

        BattlEyeCommand commandRequest = new BattlEyeCommand(command);

        commandRequest.setSequence(incrementer.next());
        commandRequest.generatePacket(BattleEyeCommandType.COMMAND);
        queueCommand(commandRequest);
    }

    public void sendNextPacket() throws IOException {
        synchronized (this) {
            if (commandQueue.size() == 0) {
                BattlEyeCommand emptyCMD = new BattlEyeCommand(null);
                emptyCMD.setSequence((byte) 0);
                emptyCMD.generatePacket(BattleEyeCommandType.COMMAND);

                if (isConnected()) {
                    queueCommand(emptyCMD);
                }
            }

            if (commandQueue.size() > 0) {
                BattlEyeCommand nextCommand = commandQueue.peek();

                if (isConnected()) {
                    sendPacket(nextCommand.getPacketBytes());
                }
                listeners.sendCommandRemovedToQueueEvent(nextCommand);
                listeners.sendPacketEvent(removeHeader(nextCommand.getPacketBytes()), nextCommand.getCommandString());
                commandQueue.remove();
            }
        }
    }

    public void addCommandListener(BattlEyeCommandListener listener) {
        listeners.addCommandListener(listener);
    }

    public void addPacketListener(BattlEyePacketListener listener) {
        listeners.addPacketListener(listener);
    }

    public void addMessageListener(BattlEyeMessageListener listener) {
        listeners.addMessageListener(listener);
    }

    public void addCommandQueueListener(BattlEyeCommandQueueListener listener) {
        listeners.addCommandQueueListener(listener);
    }

    @Override
    public void receiveCallback() throws IOException {
        byte[] receivedData = new byte[4012];
        byte[] headlessPacket = new byte[0];
        DatagramPacket packet = new DatagramPacket(receivedData, receivedData.length);

        if (isConnected()) {
            socket.receive(packet);

            headlessPacket = removeHeader(packet.getData());

            if (receivedData.length < 7) {
                System.err.println("[BattlEye]:: Packet received has an invalid header.");
                return;
            }

            if (receivedData[0] != (byte) 'B' || receivedData[1] != (byte) 'E') {
                System.err.println("[BattlEye]:: Invalid Header");
                return;
            }

            if (receivedData[6] != (byte) 0xFF) {
                System.err.println("[BattlEye]:: Invalid Header");
                return;
            }

            switch (headlessPacket[0]) {
                case 0x00:
                    processLoginEvent();
                    break;
                case 0x01:
                    if (headlessPacket.length > 1)
                        listeners.sendCommandEvent(headlessPacket[1], getResponseString(headlessPacket));
                    else
                        listeners.sendCommandEvent((byte) -1, getResponseString(headlessPacket));

                    break;

                case 0x02:
                    processServerMessage(headlessPacket);
                    break;
            }

            if (headlessPacket[0] < 0x03)
                listeners.sendPacketEvent(headlessPacket, getResponseString(headlessPacket));
        }
    }

    public BattlEyeSocket setDebug(boolean isDebug)
    {
        this.isDebug = isDebug;
        return this;
    }

    public static String getResponseString(byte[] data) {
        String responseString = "";

        for (int i = 2; i < data.length; i++)
            responseString += (char) data[i];

        return responseString;
    }

    private void sendPacket(byte[] data) throws IOException {
        if (isConnected())
            socket.send(new DatagramPacket(data, data.length, socket.getRemoteSocketAddress()));

        listeners.sendCommandSentEvent(removeHeader(data), getResponseString(removeHeader(data)));
    }

    private void processServerMessage(byte[] data) throws IOException {
        byte seq = data[1];
        String responseString = getResponseString(data);
        listeners.sendMessageEvent(seq, responseString);

        BattlEyeCommand responseToServer = new BattlEyeCommand(null);
        responseToServer.setSequence(seq);
        responseToServer.generatePacket(BattleEyeCommandType.MESSAGE);

        if (isConnected())
            queueCommand(responseToServer);
    }

    private void queueCommand(BattlEyeCommand command) {
        if (commandQueue.offer(command)) listeners.sendCommandAddedToQueueEvent(command);
        else System.err.println("[BattlEye]:: CommandQueue is full. Disregarding command: " + command.getCommandString());
    }

    private void processLoginEvent() throws IOException {
        System.out.println("[BattlEye]:: Login Request to " + socket.getRemoteSocketAddress());
    }

    private byte[] removeHeader(byte[] data) {
        ArrayList<Byte> bytes = new ArrayList<>();

        for (int i = 7; i < data.length; i++) {
            if (i + 1 < data.length - 1) {
                if (data[i + 1] == (byte) 0x00 && data[i - 1] == (byte) 0x00 && data[i] == (byte) 0x00) {
                    bytes.remove(bytes.size() - 1);
                    break;
                }
            }

            bytes.add(data[i]);
        }
        byte[] temp = new byte[bytes.size()];

        for (int i = 0; i < temp.length; i++) {
            temp[i] = bytes.get(i);
        }

        return temp;
    }

    public static void debugPrintBytes(byte[] data) {
        String byteS = "";
        for (byte b : data) {
            byteS += String.format("%02X ", b);
        }
        System.out.println(byteS);
    }
}