package BattleEye.Socket;

import BattleEye.Command.BattleEyeCommandType;
import BattleEye.Socket.Listeners.BattlEyeCommandListener;
import BattleEye.Socket.Listeners.BattlEyeCommandQueueListener;
import BattleEye.Socket.Listeners.BattlEyeMessageListener;
import BattleEye.Socket.Listeners.BattlEyePacketListener;

import java.util.ArrayList;

public class BattlEyeListenerManager
{
    private ArrayList<BattlEyePacketListener> packetListeners;
    private ArrayList<BattlEyeMessageListener> messageListeners;
    private ArrayList<BattlEyeCommandListener> commandListeners;
    private ArrayList<BattlEyeCommandQueueListener> commandQueueListeners;

    public BattlEyeListenerManager() {
        packetListeners = new ArrayList<>();
        messageListeners = new ArrayList<>();
        commandListeners = new ArrayList<>();
        commandQueueListeners = new ArrayList<>();
    }

    void addPacketListener(BattlEyePacketListener listener)
    {
        packetListeners.add(listener);
    }
    void addCommandListener(BattlEyeCommandListener listener)
    {
        commandListeners.add(listener);
    }
    void addMessageListener(BattlEyeMessageListener listener)
    {
        messageListeners.add(listener);
    }
    void addCommandQueueListener(BattlEyeCommandQueueListener listener){commandQueueListeners.add(listener);}

    void sendCommandAddedToQueueEvent(BattlEyeCommand command)
    {
        for(BattlEyeCommandQueueListener listener : commandQueueListeners)
        {
            listener.onCommandAdded(command);
        }
    }

    void sendCommandRemovedToQueueEvent(BattlEyeCommand command)
    {
        for(BattlEyeCommandQueueListener listener : commandQueueListeners)
        {
            listener.onCommandRemoved(command);
        }
    }

    void sendPacketEvent(byte[] data, String response){
        for (BattlEyePacketListener listeners : packetListeners) {
            if(listeners != null) {
                if (data.length > 1) {
                    listeners.OnPacketReceived(data[0], data[1], response);
                }
            }
        }
    }

    void sendCommandEvent(byte sequence, String response)
    {
        for(BattlEyeCommandListener commandListener : commandListeners)
        {
            commandListener.onCommandResponse(sequence, response);
        }
    }

    void sendMessageEvent(byte sequence, String response)
    {
        for(BattlEyeMessageListener listener : messageListeners)
        {
            listener.onMessagePacketReceived(sequence, response);
        }
    }

    void sendCommandSentEvent(byte[] data, String response) {
        if(data.length == 0) return;

        for(BattlEyeCommandListener listener : commandListeners)
        {
            if(data[0] == (byte) 2){
                if(data.length < 2)
                    listener.onCommandPacketSent(data[0], data[1], response);

                if(data.length >= 2)
                    listener.onCommandPacketSent(data[0], data[1], "Empty Command Packet.");
            }

            if(data[0] == 0)
                listener.onCommandPacketSent(data[0], (byte) -1, "#login");
        }
    }
}
