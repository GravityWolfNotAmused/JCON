package BattleEye.Command;

public enum BattleEyeCommandType {
    LOGIN((byte) 0x00),
    COMMAND((byte) 0x01),
    MESSAGE((byte) 0x02);

    private byte hexValue;

    BattleEyeCommandType(byte hex)
    {
        hexValue = hex;
    }

    public byte getHexValue()
    {
        return hexValue;
    }
}
