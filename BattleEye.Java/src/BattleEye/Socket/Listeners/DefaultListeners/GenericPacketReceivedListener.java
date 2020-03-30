package BattleEye.Socket.Listeners.DefaultListeners;

import BattleEye.Socket.Listeners.BattlEyePacketListener;

public class GenericPacketReceivedListener implements BattlEyePacketListener {
    @Override
    public void OnPacketReceived(byte type, byte sequence, byte[] data) {
        System.out.println("[BattlEye]:: Packet Received: Type: " + type + ", Sequence: " + sequence + ", Data: " + new String(data));
    }
}
