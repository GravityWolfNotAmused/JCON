package BattleEye.Threading;

import BattleEye.Socket.BattlEyeSocket;

public abstract class JConRunnableBase extends Thread
{
    protected BattlEyeSocket socket;
    public JConRunnableBase(BattlEyeSocket socket)
    {
        this.socket = socket;
    }

    public abstract void run();
}