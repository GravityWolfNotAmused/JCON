package BattleEye;

public class NumberIncrementer {

    private int current = -1;

    public byte next() {
        byte ret = (byte) current;
        current++;
        if (current > (byte) 255)
            current = 0;
        return (byte) current;
    }
}
