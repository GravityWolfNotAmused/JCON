package BattleEye.Socket;

import java.io.IOException;

public interface BattleSocket {
    boolean connect() throws IOException;
    void login() throws IOException, InterruptedException;
    void sendCommand(String cmd) throws IOException;
    void receiveCallback() throws IOException;
}
