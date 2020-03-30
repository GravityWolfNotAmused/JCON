package BattleEye.Threading;

import BattleEye.Socket.BattlEyeSocket;

import java.io.IOException;

public class JConReceivingThread extends JConRunnableBase
{
    public JConReceivingThread(BattlEyeSocket socket) {
        super(socket);
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                socket.receiveCallback();
                Thread.sleep(600);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}