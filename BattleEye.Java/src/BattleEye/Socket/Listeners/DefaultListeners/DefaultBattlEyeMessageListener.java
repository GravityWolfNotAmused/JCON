package BattleEye.Socket.Listeners.DefaultListeners;

import BattleEye.Socket.Listeners.BattlEyeMessageListener;

public class DefaultBattlEyeMessageListener extends DefaultListenerBase implements BattlEyeMessageListener {
    public DefaultBattlEyeMessageListener(boolean debug) {
        super(debug);
    }

    @Override
    public void onMessagePacketReceived(byte sequence, String response) {
        if (isDebug)
            System.out.println("Message Received: Sequence: " + sequence + ", Response String: " + response);
    }
}
