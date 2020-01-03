package BattleEye.Socket;

import BattleEye.Command.BattleEyeCommandType;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

public class BattlEyeCommand {
    private String command;
    private byte[] commandBytes;
    private byte sequence;
    private byte[] packetBytes;

    BattlEyeCommand(String cmd) {
        if(cmd != null && cmd.length() > 0) {
            command = cmd;
            commandBytes = cmd.getBytes(StandardCharsets.UTF_8);
        }
        packetBytes = new byte[0];
    }

    public void generatePacket(BattleEyeCommandType type) {
        int bufferSize = 7;

        if (commandBytes != null)
            bufferSize += commandBytes.length;

        if (sequence != -1)
            bufferSize += 2;

        if (sequence == -1)
            bufferSize += 1;

        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.put((byte) 'B');
        buffer.put((byte) 'E');
        buffer.position(6);
        buffer.put((byte) 0xFF);
        buffer.put(type.getHexValue());

        if (sequence >= (byte) 0)
            buffer.put(sequence);

        if (commandBytes != null && commandBytes.length > 0)
            buffer.put(commandBytes);

        CRC32 crc = new CRC32();
        crc.update(buffer.array(), 6, buffer.position() - 6);
        int hash = (int) crc.getValue();

        buffer.putInt(2, hash);
        packetBytes = buffer.array();
        buffer.clear();
    }

    public String getCommandString() {
        return command;
    }

    public byte[] getPacketBytes()
    {
        return packetBytes;
    }

    public byte getSequence() {
        return sequence;
    }

    public void setSequence(byte seq) {
        sequence = seq;
    }
}
