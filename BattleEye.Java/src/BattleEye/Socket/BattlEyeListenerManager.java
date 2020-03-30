package BattleEye.Socket;

import BattleEye.Socket.Listeners.BattlEyePacketListener;
import BattleEye.Socket.Listeners.BattlEyeQueueListener;

import java.util.concurrent.CopyOnWriteArrayList;

public class BattlEyeListenerManager {
    private CopyOnWriteArrayList<BattlEyeQueueListener> queueListeners;
    private CopyOnWriteArrayList<BattlEyePacketListener> packetListeners;

    public BattlEyeListenerManager() {
        queueListeners = new CopyOnWriteArrayList<>();
        packetListeners = new CopyOnWriteArrayList<>();
    }

    void addPacketListener(BattlEyePacketListener listener) {
        packetListeners.add(listener);
    }

    void addQueueListener(BattlEyeQueueListener listener) {
        queueListeners.add(listener);
    }

    void removeAllPacketListeners() {
        packetListeners.removeAll(packetListeners);
    }

    void removeAllQueueListeners() {
        queueListeners.removeAll(queueListeners);
    }

    public void sendOnPacketReceived(byte type, byte sequence, byte[] data) {
        for (BattlEyePacketListener listener : packetListeners) {
            listener.OnPacketReceived(type, sequence, data);
        }
    }

    public void sendOnPacketSent(byte type, byte sequence, byte[] data) {
        for (BattlEyeQueueListener listener : queueListeners) {
            listener.OnCommandSent(type, sequence, data);
        }
    }
}
