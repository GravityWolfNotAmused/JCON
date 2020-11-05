package BattleEye.Socket;

import BattleEye.Client.JConClient;
import BattleEye.Command.BattlEyeCommand;
import BattleEye.Logger.BattlEyeLogger;
import BattleEye.Login.BattlEyeLoginInfo;
import BattleEye.Socket.Listeners.BattlEyePacketListener;
import BattleEye.Socket.Listeners.BattlEyeQueueListener;
import BattleEye.Socket.Listeners.DefaultListeners.CommandPacketListener;
import BattleEye.Socket.Listeners.DefaultListeners.GenericPacketListener;
import BattleEye.Socket.Listeners.DefaultListeners.LoginPacketListener;
import BattleEye.Socket.Sequence.NumberIncrementer;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class BattlEyeSocket implements BattleSocket {
    private DatagramChannel socket;
    private BattlEyeLoginInfo loginInformation;
    private BattlEyeListenerManager listenerManager;
    private ConcurrentLinkedQueue<BattlEyeCommand> commandQueue;

    private NumberIncrementer incrementer;
    private AtomicLong packetLastSent;
    private AtomicLong packetLastReceived;
    private AtomicInteger reconnectAttempts;
    private AtomicBoolean isDebug;

    public BattlEyeSocket(String address, int port, String password, boolean debug) throws IOException {
        loginInformation = new BattlEyeLoginInfo(address, port, password);
        reconnectAttempts = new AtomicInteger();
        commandQueue = new ConcurrentLinkedQueue<>();
        incrementer = new NumberIncrementer();
        listenerManager = new BattlEyeListenerManager();
        isDebug = new AtomicBoolean(debug);
        socket = DatagramChannel.open();
        socket.configureBlocking(false);
        packetLastSent = new AtomicLong(0);
        packetLastReceived = new AtomicLong(0);

        listenerManager.addPacketListener(new LoginPacketListener());
        listenerManager.addPacketListener(new CommandPacketListener());

        if (isDebug.get())
            listenerManager.addQueueListener(new GenericPacketListener());

        listenerManager.addPacketListener((type, sequence, data) -> {
            if (type == 0x02) {
                BattlEyeLogger.GetLogger().log("Message Received: " + sequence + ", Response: " + new String(data));
                BattlEyeCommand response = new BattlEyeCommand(null)
                        .setSequence(sequence)
                        .generatePacket(BattlEyePacketType.MESSAGE);

                if (isConnected())
                    queueCommand(response);
            }
        });
    }

    @Override
    public boolean connect() {
        try {
            socket.connect(loginInformation);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (isConnected()) {
            BattlEyeLogger.GetLogger().log("Connected to " + loginInformation.getAddress().getHostAddress());
            reconnectAttempts.set(0);
            return true;
        }

        return false;
    }

    @Override
    public void login() {
        String passwordBytes = loginInformation.getPassword();

        BattlEyeCommand loginCommand = new BattlEyeCommand(passwordBytes)
                .setSequence(-1)
                .generatePacket(BattlEyePacketType.LOGIN);

        if (isConnected()) {
            queueCommand(loginCommand);
        }
    }

    @Override
    public void sendCommand(String command) {
        BattlEyeCommand commandRequest = null;

        if (command == null) {
            commandRequest = new BattlEyeCommand(null);
        }
        if (command != null)
            commandRequest = new BattlEyeCommand(command);

        commandRequest.setSequence(incrementer.next())
                .generatePacket(BattlEyePacketType.COMMAND);

        if (command == null) {
            if (isDebug.get()) {
                debugPrintBytes(commandRequest.getPacketBytes());
            }
        }

        if (isConnected()) {
            queueCommand(commandRequest);
        }
    }

    @Override
    public void receiveCallback() throws IOException {
        if (isConnected()) {
            ByteBuffer buffer = ByteBuffer.allocate(SocketOptions.SO_RCVBUF);
            buffer.clear();
            try {
                socket.receive(buffer);
                buffer.flip();
            } catch (PortUnreachableException e) {
                reconnect();
                return;
            }

            byte[] receivedData = buffer.array();

            if (receivedData.length < 7) {
                BattlEyeLogger.GetLogger().error("Packet received has an invalid header.");
                return;
            }

            if (receivedData[0] != (byte) 'B' || receivedData[1] != (byte) 'E') {
                if (receivedData[0] == (byte) 0 && receivedData[1] == (byte) 0)
                    return;

                BattlEyeLogger.GetLogger().error("Invalid Header, Does not contain BE in packet.");
                return;
            }

            if (receivedData[6] != (byte) 0xFF) {
                BattlEyeLogger.GetLogger().error("Invalid Header, Missing Byte: 0xFF");
                return;
            }

            byte[] headlessPacket = removeHeader(receivedData);

            byte type = headlessPacket[0];

            if (headlessPacket.length >= 2) {
                int sequence = Byte.toUnsignedInt(headlessPacket[1]);

                if (type == 0x01) {
                    byte multipacketHeaderByte = headlessPacket[2];

                    if (multipacketHeaderByte == 0x00) {
                        byte packetCount = headlessPacket[3];
                        byte packetIndex;

                        byte[][] multipacket = new byte[packetCount][];

                        for (int i = 0; i < packetCount; i++) {
                            ByteBuffer currentBuffer = ByteBuffer.allocate(SocketOptions.SO_RCVBUF);

                            if (i > 0) {
                                socket.receive(currentBuffer);
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            byte[] currentPacket = currentBuffer.array();
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

                packetLastReceived.set(System.currentTimeMillis());
                listenerManager.sendOnPacketReceived(type, sequence, headlessPacket);
            }
        }
    }

    public void addPacketListener(BattlEyePacketListener listener) {
        listenerManager.addPacketListener(listener);
    }

    public void addQueueListener(BattlEyeQueueListener listener) {
        listenerManager.addQueueListener(listener);
    }

    public void removePacketListeners() {
        listenerManager.removeAllPacketListeners();
    }

    public void removeQueueListeners() {
        listenerManager.removeAllQueueListeners();
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
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
                }
            } catch (PortUnreachableException e) {
                BattlEyeLogger.GetLogger().error(e.getMessage());

                reconnect();
            } finally {
                commandQueue.remove();
            }
        }
    }

    public BattlEyeSocket setDebug(boolean debug) {
        isDebug.set(debug);
        return this;
    }

    public long getTimeSinceLastPacketSent() {
        return packetLastSent.get();
    }

    public long getTimeSinceLastPacketReceived() {
        return packetLastSent.get();
    }

    private void sendPacket(byte[] data) throws IOException {
        if (isConnected()) {
            socket.send(ByteBuffer.wrap(data).put(data).flip(), loginInformation);

            byte type = data[7];
            int sequence = Byte.toUnsignedInt(data[8]);
            int removeFrom = 8;

            if (sequence > -1 && type != 0x00)
                removeFrom += 1;

            byte[] temp = new byte[data.length - removeFrom];
            for (int i = removeFrom; i < data.length; i++) {
                temp[i - removeFrom] = data[i];
            }

            data = temp;

            if (type == 0x00)
                sequence = -1;

            listenerManager.sendOnPacketSent(type, sequence, data);
            packetLastSent.set(System.currentTimeMillis());
        }
    }

    private void reconnect() {
        reconnectAttempts.set(reconnectAttempts.get() + 1);

        if (reconnectAttempts.get() > 3) {
            try {
                socket.disconnect();
                BattlEyeLogger.GetLogger().error("Attempt To Reconnect has failed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (reconnectAttempts.get() <= 3) {
            try {
                BattlEyeLogger.GetLogger().error("Attempting to reconnect in 2 seconds. Attempt #" + reconnectAttempts.get());
                Thread.sleep(1000);
                BattlEyeLogger.GetLogger().error("Attempting to reconnect in 1 seconds. Attempt #" + reconnectAttempts.get());
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            incrementer = new NumberIncrementer();

            if (!socket.isConnected())
                connect();

            login();
        }
    }

    private void queueCommand(BattlEyeCommand command) {
        if (commandQueue.offer(command)) {
            if (isDebug.get()) {
                BattlEyeLogger.GetLogger().log("Command: " + command.getCommandString() + " has been successfully added to the process queue");
            }
        } else
            BattlEyeLogger.GetLogger().log("CommandQueue is full. Disregarding command: " + command.getCommandString());
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
        BattlEyeLogger.GetLogger().log(byteS);
    }
}