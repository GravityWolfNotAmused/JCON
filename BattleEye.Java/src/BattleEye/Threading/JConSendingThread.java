package BattleEye.Threading;

import BattleEye.Socket.BattlEyeSocket;

import java.io.IOException;

public class JConSendingThread extends JConRunnableBase {
    public JConSendingThread(BattlEyeSocket socket) {
        super(socket);
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                socket.sendNextPacket();
                Thread.sleep(500);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}