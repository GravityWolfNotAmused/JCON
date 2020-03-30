package BattleEye.Socket.Listeners.DefaultListeners;

import BattleEye.Socket.Listeners.BattlEyePacketListener;

public class CommandPacketListener implements BattlEyePacketListener {
    @Override
    public void OnPacketReceived(byte type, byte sequence, byte[] data) {
        if (type == 0x01) {
            String response = new String(data);

            StringBuilder builder = new StringBuilder()
                    .append("[BattlEye]:: Sequence: " + sequence + ", ")
                    .append("Command Response Received: ")
                    .append(response + "\n");

            if(!response.equals("   "))
                System.out.println(builder.toString());
        }
    }
}
