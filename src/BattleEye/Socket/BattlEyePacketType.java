package BattleEye.Socket;

public enum BattlEyePacketType {
    LOGIN((byte) 0x00),
    COMMAND((byte) 0x01),
    MESSAGE((byte) 0x02);

    private byte hexValue;

    BattlEyePacketType(byte hex)
    {
        hexValue = hex;
    }

    public byte getHexValue()
    {
        return hexValue;
    }
}
