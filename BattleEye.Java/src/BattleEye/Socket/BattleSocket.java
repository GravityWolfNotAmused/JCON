package BattleEye.Socket;

import BattleEye.Command.BattleEyeCommandType;

import java.io.IOException;

public interface BattleSocket {
    boolean connect() throws IOException;
    void login() throws IOException, InterruptedException;
    void sendCommand(String cmd, BattleEyeCommandType type) throws IOException;
    void receiveCallback() throws IOException;
}
