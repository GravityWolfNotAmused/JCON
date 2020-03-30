package BattleEye.Socket.Listeners.DefaultListeners;

import BattleEye.Command.BattleEyeCommandType;
import BattleEye.Socket.BattlEyeSocket;
import BattleEye.Socket.Listeners.BattlEyeQueueListener;

public class GenericPacketListener implements BattlEyeQueueListener {
    @Override
    public void OnCommandSent(byte type, byte sequence, byte[] data) {
        System.out.println("[BattlEye]:: Packet Sent: Type: " + type + ", Sequence: " + sequence + ", Data: " + new String(data));
        BattlEyeSocket.debugPrintBytes(data);
    }
}
