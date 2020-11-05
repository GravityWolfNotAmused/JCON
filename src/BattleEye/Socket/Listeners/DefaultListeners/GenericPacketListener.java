package BattleEye.Socket.Listeners.DefaultListeners;

import BattleEye.Logger.BattlEyeLogger;
import BattleEye.Socket.Listeners.BattlEyeQueueListener;

public class GenericPacketListener implements BattlEyeQueueListener {
    @Override
    public void onCommandSent(byte type, int sequence, byte[] data) {
        BattlEyeLogger.GetLogger().log("Packet Sent: Type: " + type + ", Sequence: " + sequence + ", Data: " + new String(data));
    }
}
