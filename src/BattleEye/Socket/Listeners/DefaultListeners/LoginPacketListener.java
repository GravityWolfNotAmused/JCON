package BattleEye.Socket.Listeners.DefaultListeners;

import BattleEye.Socket.Listeners.BattlEyePacketListener;

public class LoginPacketListener implements BattlEyePacketListener {
    @Override
    public void OnPacketReceived(byte type, byte sequence, byte[] data) {
        if (type == 0x00) {
            if (sequence == 0x01)
                System.out.println("[BattlEye]:: Login Request: Success");

            if (sequence == 0x00)
                System.out.println("[BattlEye]:: Login Request: Failed");
        }
    }
}
