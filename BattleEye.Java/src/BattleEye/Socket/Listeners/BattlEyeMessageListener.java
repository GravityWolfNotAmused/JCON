package BattleEye.Socket.Listeners;

public interface BattlEyeMessageListener {
    void onMessagePacketReceived(byte sequence, String response);
}
