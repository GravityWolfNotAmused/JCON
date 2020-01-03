package BattleEye.Socket.Listeners;

public interface BattlEyeCommandListener {
    void onCommandResponse(byte sequence, String response);
    void onCommandPacketSent(byte type, byte sequence, String command);
}
