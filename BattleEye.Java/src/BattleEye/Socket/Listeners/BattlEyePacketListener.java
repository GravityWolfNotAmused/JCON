package BattleEye.Socket.Listeners;

import BattleEye.Command.BattleEyeCommandType;

public interface BattlEyePacketListener {
    void OnPacketReceived(byte type, byte sequenceNumber, String response);
}