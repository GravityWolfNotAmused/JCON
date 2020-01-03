package BattleEye.Socket.Listeners.DefaultListeners;

import BattleEye.Socket.Listeners.BattlEyeCommandListener;

public class DefaultBattlEyeCommandListener extends DefaultListenerBase implements BattlEyeCommandListener {
    public DefaultBattlEyeCommandListener(boolean debug) {
        super(debug);
    }

    @Override
    public void onCommandResponse(byte sequence, String response) {
        StringBuilder builder = new StringBuilder()
                .append("[BattlEye]:: Command Response Received, Sequence: ")
                .append(sequence)
                .append(", Response: ")
                .append(response);
        if (isDebug && (response != null || response.equals("")))
            System.out.println(builder);
    }

    @Override
    public void onCommandPacketSent(byte type, byte sequence, String command) {
        StringBuilder builder = new StringBuilder()
                .append("[BattlEye]:: Command Sent, Sequence: ")
                .append(sequence)
                .append(", Packet Type: " + type)
                .append(", Command: ")
                .append(command);
        if (isDebug && command != null)
            System.out.println(builder.toString());
    }
}
