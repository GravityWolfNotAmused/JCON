package BattleEye.Socket.Listeners;

public interface BattlEyePacketListener {
    void OnPacketReceived(byte type, byte sequence, byte[] data);
}
