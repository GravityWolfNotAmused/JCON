package BattleEye.Socket.Listeners.DefaultListeners;

import BattleEye.Logger.BattlEyeLogger;
import BattleEye.Socket.Listeners.BattlEyePacketListener;

public class LoginPacketListener implements BattlEyePacketListener {
    @Override
    public void onPacketReceived(byte type, int sequence, byte[] data) {
        if (type == 0x00) {
            if (sequence == 0x01)
                BattlEyeLogger.GetLogger().log("Login Request: Success");

            if (sequence == 0x00)
                BattlEyeLogger.GetLogger().error("Login Request: Failed");
        }
    }
}
