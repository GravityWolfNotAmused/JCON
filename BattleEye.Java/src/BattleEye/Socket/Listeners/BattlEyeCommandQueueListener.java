package BattleEye.Socket.Listeners;

import BattleEye.Socket.BattlEyeCommand;

public interface BattlEyeCommandQueueListener {
    void onCommandAdded(BattlEyeCommand command);
    void onCommandRemoved(BattlEyeCommand command);
}
