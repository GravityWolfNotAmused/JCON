package BattleEye.Socket.Listeners;

public interface BattlEyeQueueListener {
    void OnCommandSent(byte type, byte sequence, byte[] data);
}
