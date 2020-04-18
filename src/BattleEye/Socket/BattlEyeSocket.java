package BattleEye.Socket;

import BattleEye.Command.BattlEyeCommand;
import BattleEye.Login.BattlEyeLoginInfo;
import BattleEye.Socket.Listeners.BattlEyePacketListener;
import BattleEye.Socket.Listeners.BattlEyeQueueListener;
import BattleEye.Socket.Listeners.DefaultListeners.CommandPacketListener;
import BattleEye.Socket.Listeners.DefaultListeners.GenericPacketListener;
import BattleEye.Socket.Listeners.DefaultListeners.LoginPacketListener;
import BattleEye.Socket.Sequence.NumberIncrementer;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class BattlEyeSocket implements BattleSocket {
    private DatagramSocket socket;
    private BattlEyeLoginInfo loginInformation;
    private BattlEyeListenerManager listenerManager;
    private ConcurrentLinkedQueue<BattlEyeCommand> commandQueue;

    private NumberIncrementer incrementer;
    private AtomicLong packetLastSent;
    private AtomicLong packetLastReceived;

    private AtomicBoolean isDebug;

    public BattlEyeSocket(String address, int port, String password, boolean debug) throws SocketException {
        loginInformation = new BattlEyeLoginInfo(address, port, password);
        commandQueue = new ConcurrentLinkedQueue<>();
        incrementer = new NumberIncrementer();
        listenerManager = new BattlEyeListenerManager();
        isDebug = new AtomicBoolean(debug);
        socket = new DatagramSocket();
        packetLastSent = new AtomicLong(0);
        packetLastReceived = new AtomicLong(0);

        listenerManager.addPacketListener(new LoginPacketListener());
        listenerManager.addPacketListener(new CommandPacketListener());

        if (isDebug.get())
            listenerManager.addQueueListener(new GenericPacketListener());

        listenerManager.addPacketListener((type, sequence, data) -> {
            if (type == 0x02) {
                System.out.println("[BattlEye]:: Message Received: " + sequence + ", Response: " + new String(data));

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
        socket.connect(loginInformation.getAddress(), loginInformation.getPort());

        if (isConnected()) {
            System.out.println("[BattlEye]:: Connected to " + loginInformation.getAddress().getHostAddress());
            return true;
        }

        return false;
    }

    @Override
    public void login(){
        String passwordBytes = loginInformation.getPassword();

        BattlEyeCommand loginCommand = new BattlEyeCommand(passwordBytes)
                .setSequence(-1)
                .generatePacket(BattlEyePacketType.LOGIN);

        if (isConnected())
            queueCommand(loginCommand);
    }

    @Override
    public void sendCommand(String command) {
        BattlEyeCommand commandRequest = null;

        if (command == null)
            commandRequest = new BattlEyeCommand(null);

        if (command != null)
            commandRequest = new BattlEyeCommand(command);

        if (commandRequest != null) {
            commandRequest.setSequence(incrementer.next())
                    .generatePacket(BattlEyePacketType.COMMAND);

            if (isConnected())
                queueCommand(commandRequest);
        }
    }

    @Override
    public void receiveCallback() throws IOException {
        if (isConnected()) {
            byte[] receivedData = new byte[socket.getOption(StandardSocketOptions.SO_RCVBUF)];
            DatagramPacket packet = new DatagramPacket(receivedData, receivedData.length);

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
                int sequence = Byte.toUnsignedInt(headlessPacket[1]);

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
                e.printStackTrace();
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
            socket.send(new DatagramPacket(data, data.length, socket.getRemoteSocketAddress()));

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
        System.err.println("[BattlEye]:: Attempting Reconnect in 2 seconds.");

        if(isDebug.get())
            System.exit(69420666);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        incrementer = new NumberIncrementer();

        if (!socket.isConnected())
            connect();

        login();
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
}