package BattleEye.Command;

import BattleEye.Socket.BattlEyePacketType;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.CRC32;

public class BattlEyeCommand {
    private String command;
    private byte[] commandBytes;
    private int sequence;
    private byte[] packetBytes;

    public BattlEyeCommand(String cmd) {
        if(cmd != null && cmd.length() > 0) {
            command = cmd;
            commandBytes = cmd.getBytes(StandardCharsets.UTF_8);
        }

        packetBytes = new byte[0];
    }

    public BattlEyeCommand generatePacket(BattlEyePacketType type) {
        int headerAndSequence = 8 + ( sequence >= 0 ? 1 : 0);

        if(commandBytes != null)
            headerAndSequence += commandBytes.length;

        ByteBuffer buffer = ByteBuffer.allocate(headerAndSequence);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.put((byte) 'B');
        buffer.put((byte) 'E');
        buffer.position(6);
        buffer.put((byte) 0xFF);
        buffer.put(type.getHexValue());

        if (sequence >= 0 && type != BattlEyePacketType.LOGIN)
            buffer.put((byte) sequence);

        if (command != null && !command.isEmpty())
            buffer.put(commandBytes);

        CRC32 crc = new CRC32();
        crc.update(buffer.array(), 6, buffer.position() - 6);
        int hash = (int) crc.getValue();

        buffer.putInt(2, hash);
        packetBytes = buffer.array();
        buffer.clear();
        return this;
    }

    public String getCommandString() {
        return command;
    }

    public byte[] getPacketBytes()
    {
        return packetBytes;
    }

    public int getSequence() {
        return sequence;
    }

    public BattlEyeCommand setSequence(int seq) {
        sequence = seq;
        return this;
    }

    @Override
    public String toString() {
        return "BattlEyeCommand{" +
                "command='" + command + '\'' +
                ", commandBytes=" + Arrays.toString(commandBytes) +
                ", sequence=" + sequence +
                ", packetBytes=" + Arrays.toString(packetBytes) +
                '}';
    }
}
