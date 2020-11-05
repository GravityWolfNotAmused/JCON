package BattleEye.Socket.Listeners.DefaultListeners;

import BattleEye.Logger.BattlEyeLogger;
import BattleEye.Socket.Listeners.BattlEyePacketListener;

public class GenericPacketReceivedListener implements BattlEyePacketListener {
    @Override
    public void onPacketReceived(byte type, int sequence, byte[] data) {
        BattlEyeLogger.GetLogger().log("Packet Received: Type: " + type + ", Sequence: " + sequence + ", Data: " + new String(data));
    }
}
