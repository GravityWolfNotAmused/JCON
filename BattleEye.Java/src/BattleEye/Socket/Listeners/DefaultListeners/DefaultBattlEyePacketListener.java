package BattleEye.Socket.Listeners.DefaultListeners;

import BattleEye.Command.BattleEyeCommandType;
import BattleEye.Socket.Listeners.BattlEyePacketListener;

public class DefaultBattlEyePacketListener extends DefaultListenerBase implements BattlEyePacketListener {
    public DefaultBattlEyePacketListener(boolean debug) {
        super(debug);
    }

    @Override
    public void OnPacketReceived(byte type, byte sequenceNumber, String response) {
        if (isDebug) {
            if (type == 0x00) {
                if (sequenceNumber == 0x00)
                    System.err.println("[BattlEye] Login Requested: Failed");

                if (sequenceNumber == 0x01)
                    System.out.println("[BattlEye] Login Requested: Success");
            }

            if(type != 0x00 && response != null)
                System.out.println("Packet Received: Type: " + type + ", Sequence Number: " + sequenceNumber + " Response: " + response);
        }
    }
}