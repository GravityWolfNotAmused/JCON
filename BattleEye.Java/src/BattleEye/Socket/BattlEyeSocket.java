package BattleEye.Socket;

import BattleEye.Command.BattleEyeCommandType;
import BattleEye.Login.BattlEyeLoginInfo;
import BattleEye.Socket.Listeners.BattlEyePacketListener;
import BattleEye.Socket.Listeners.BattlEyeQueueListener;
import BattleEye.Socket.Listeners.DefaultListeners.CommandPacketListener;
import BattleEye.Socket.Listeners.DefaultListeners.GenericPacketListener;
import BattleEye.Socket.Listeners.DefaultListeners.LoginPacketListener;
import BattleEye.NumberIncrementer;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public class BattlEyeSocket implements BattleSocket {
    private DatagramSocket socket;
    private BattlEyeLoginInfo loginInformation;
    private ConcurrentLinkedQueue<BattlEyeCommand> commandQueue;
    private ArrayList<BattlEyeQueueListener> queueListeners;
    private ArrayList<BattlEyePacketListener> packetListeners;
    private NumberIncrementer incrementer;
    private boolean isDebug;

    private AtomicLong packetLastSent;

    public BattlEyeSocket(String address, int port, String password, boolean debug) throws SocketException {
        loginInformation = new BattlEyeLoginInfo(address, port, password);
        commandQueue = new ConcurrentLinkedQueue<>();
        incrementer = new NumberIncrementer();

        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            throw new SocketException(e.getMessage());
        }

        packetListeners = new ArrayList<>();
        queueListeners = new ArrayList<>();
        packetLastSent = new AtomicLong(0);

        isDebug = debug;

        packetListeners.add(new LoginPacketListener());
        packetListeners.add(new CommandPacketListener());

        if (isDebug)
            queueListeners.add(new GenericPacketListener());

        packetListeners.add((type, sequence, data) -> {
            if (type == 0x02) {
                System.out.println("[BattlEye]:: Message Received: " + sequence + ", Response: " + new String(data));

                BattlEyeCommand response = new BattlEyeCommand(null)
                        .setSequence(sequence)
                        .generatePacket(BattleEyeCommandType.MESSAGE);

                if (isConnected())
                    queueCommand(response);
            }
        });
    }

    public void addListener(BattlEyePacketListener listener) {
        packetListeners.add(listener);
    }

    public void addQueueListener(BattlEyeQueueListener listener) {
        queueListeners.add(listener);
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    @Override
    public boolean connect() {
        socket.connect(loginInformation.getAddress(), loginInformation.getPort());

        if (isConnected()) {
            System.out.println("[BattlEye]:: Connected to " + loginInformation.getAddress().getHostAddress());
            return true;
        }

        return false;
    }

    @Override
    public void login() throws InterruptedException {
        String passwordBytes = loginInformation.getPassword();

        BattlEyeCommand loginCommand = new BattlEyeCommand(passwordBytes)
                .setSequence((byte) -1)
                .generatePacket(BattleEyeCommandType.LOGIN);

        if (isConnected())
            queueCommand(loginCommand);

        Thread.sleep(1000);
    }

    @Override
    public void sendCommand(String command) {
        if (command == null) {
            BattlEyeCommand nullCommand = new BattlEyeCommand(null)
                    .setSequence(incrementer.next())
                    .generatePacket(BattleEyeCommandType.COMMAND);

            if (isConnected())
                queueCommand(nullCommand);

            return;
        }
        /* Get bytes of the command */
        byte[] commandBytes = null;

        if (command != null && !command.isEmpty())
            commandBytes = command.getBytes(StandardCharsets.UTF_8);

        BattlEyeCommand commandRequest = new BattlEyeCommand(command)
                .setSequence(incrementer.next())
                .generatePacket(BattleEyeCommandType.COMMAND);

        queueCommand(commandRequest);
    }

    public boolean hasNextCommand() {
        return commandQueue.size() > 0;
    }

    public void sendNextPacket() throws IOException {
        if (commandQueue.size() > 0) {
            BattlEyeCommand nextCommand = commandQueue.peek();
            try {
                if (isConnected()) {
                    sendPacket(nextCommand.getPacketBytes());
                    commandQueue.remove();
                }
            } catch (PortUnreachableException e) {
                e.printStackTrace();
                reconnect();
            }
        }
    }

    @Override
    public void receiveCallback() throws IOException {
        byte[] receivedData = new byte[socket.getOption(StandardSocketOptions.SO_SNDBUF)];
        DatagramPacket packet = new DatagramPacket(receivedData, receivedData.length);

        if (isConnected()) {

            try {
                socket.receive(packet);
            } catch (PortUnreachableException e) {
                reconnect();
                return;
            }

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

            byte[] headlessPacket = removeHeader(receivedData);

            byte type = headlessPacket[0];

            if (headlessPacket.length >= 2) {
                byte sequence = headlessPacket[1];
                if (type == 0x01) {
                    byte multipacketHeaderByte = headlessPacket[2];

                    if (multipacketHeaderByte == 0x00) {
                        byte packetCount = headlessPacket[3];
                        byte packetIndex;

                        byte[][] multipacket = new byte[packetCount][];

                        for (int i = 0; i < packetCount; i++) {
                            byte[] currentPacket = new byte[socket.getOption(StandardSocketOptions.SO_SNDBUF)];
                            packet = new DatagramPacket(currentPacket, currentPacket.length);

                            if (i > 0) {
                                socket.receive(packet);

                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            packetIndex = currentPacket[2];

                            byte[] temp = new byte[currentPacket.length - 12];
                            for (int j = 0; j < temp.length; j++) {
                                temp[j] = currentPacket[j + 12];
                            }

                            multipacket[packetIndex] = temp;
                        }

                        ArrayList<Byte> temp = new ArrayList<>();

                        for (byte[] bytes : multipacket) {
                            for (byte packetBits : bytes) {
                                temp.add(packetBits);
                            }
                        }

                        headlessPacket = new byte[temp.size()];

                        for (int x = 0; x < temp.size(); x++) {
                            headlessPacket[x] = temp.get(x);
                        }
                    }
                }

                byte[] temp = new byte[headlessPacket.length];

                for (int i = 2; i < temp.length; i++) {
                    temp[i - 2] = headlessPacket[i];
                }

                headlessPacket = temp;

                for (BattlEyePacketListener listener : packetListeners) {
                    listener.OnPacketReceived(type, sequence, headlessPacket);
                }
            }
        }
    }

    public BattlEyeSocket setDebug(boolean isDebug) {
        this.isDebug = isDebug;
        return this;
    }

    public long getTimeSinceLastPacketSent() {
        return packetLastSent.get();
    }

    private void sendPacket(byte[] data) throws IOException {
        if (isConnected()) {
            socket.send(new DatagramPacket(data, data.length, socket.getRemoteSocketAddress()));

            byte type = data[7];
            byte sequence = data[8];
            int removeFrom = 6;

            if (sequence > -1)
                removeFrom += 1;

            byte[] temp = new byte[data.length - removeFrom];
            for (int i = removeFrom; i < data.length; i++) {
                temp[i - removeFrom] = data[i];
            }

            data = temp;
            debugPrintBytes(data);

            if (type == 0x00)
                sequence = (byte) -1;

            for (BattlEyeQueueListener listener : queueListeners) {
                listener.OnCommandSent(type, sequence, data);
            }

            packetLastSent.set(System.currentTimeMillis());
        }
    }

    private void reconnect() {
        System.err.println("[BattlEye]:: Attempting Reconnect in 2 seconds.");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        incrementer = new NumberIncrementer();

        if (!socket.isConnected())
            connect();

        try {
            login();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void queueCommand(BattlEyeCommand command) {
        if (commandQueue.offer(command)) {
        } else
            System.err.println("[BattlEye]:: CommandQueue is full. Disregarding command: " + command.getCommandString());
    }

    private byte[] removeHeader(byte[] data) {
        byte[] temp = new byte[data.length - 6];

        for (int i = 7; i < data.length; i++) {
            temp[i - 7] = data[i];
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

    public static String getResponseString(byte[] data) {
        String responseString = "";
        debugPrintBytes(data);

        for (int i = 6; i < data.length; i++)
            if (data[i] != 0x00)
                responseString += (char) data[i];

        return responseString;
    }
}