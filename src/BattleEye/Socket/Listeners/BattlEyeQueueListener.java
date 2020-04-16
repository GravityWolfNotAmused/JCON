package BattleEye.Socket.Listeners;

public interface BattlEyeQueueListener {
    void onCommandSent(byte type, int sequence, byte[] data);
}