package BattleEye.Socket.Listeners;

public interface BattlEyePacketListener {
    void onPacketReceived(byte type, int sequence, byte[] data);
}
