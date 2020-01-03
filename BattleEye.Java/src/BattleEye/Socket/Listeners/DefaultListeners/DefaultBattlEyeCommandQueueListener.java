package BattleEye.Socket.Listeners.DefaultListeners;

import BattleEye.Socket.BattlEyeCommand;
import BattleEye.Socket.Listeners.BattlEyeCommandQueueListener;

public class DefaultBattlEyeCommandQueueListener extends DefaultListenerBase implements BattlEyeCommandQueueListener {
    public DefaultBattlEyeCommandQueueListener(boolean debug) {
        super(debug);
    }

    @Override
    public void onCommandAdded(BattlEyeCommand command) {
        if (isDebug)
            if (!(command.getSequence() == (byte) 0 && command.getCommandString() == null))
                System.out.println("[BattlEye]:: Sequence: " + command.getSequence() + ", Command String: " + command.getCommandString() + " added to queue.");
    }

    @Override
    public void onCommandRemoved(BattlEyeCommand command) {
        if (isDebug)
            if (!(command.getSequence() == (byte) 0 && command.getCommandString() == null))
                System.out.println("[BattlEye]:: Sequence: " + command.getSequence() + ", Command String: " + command.getCommandString() + " removed to queue.");
    }
}
