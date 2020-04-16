package BattleEye.Socket.Listeners.DefaultListeners;

import BattleEye.Socket.Listeners.BattlEyeQueueListener;

public class GenericPacketListener implements BattlEyeQueueListener {
    @Override
    public void onCommandSent(byte type, int sequence, byte[] data) {
        System.out.println("[BattlEye]:: Packet Sent: Type: " + type + ", Sequence: " + sequence + ", Data: " + new String(data));
    }
}
