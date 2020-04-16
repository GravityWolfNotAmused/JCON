package BattleEye.Socket.Listeners.DefaultListeners;

import BattleEye.Socket.Listeners.BattlEyePacketListener;

public class CommandPacketListener implements BattlEyePacketListener {
    @Override
    public void onPacketReceived(byte type, int sequence, byte[] data) {
        if (type == 0x01) {
            String response = new String(data);

            StringBuilder builder = new StringBuilder()
                    .append("[BattlEye]:: Sequence: " + sequence + ", ")
                    .append("Command Response Received: ");

            if(response != null && !response.equals(" ") && !response.isEmpty())
                builder.append(response);
            else
                builder.append("Empty");

            if(!response.equals("   "))
                System.out.println(builder.toString());
        }
    }
}
