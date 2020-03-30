package BattleEye;

import java.util.concurrent.atomic.AtomicInteger;

public class NumberIncrementer {

    private AtomicInteger sequence;

    public NumberIncrementer()
    {
        sequence = new AtomicInteger(-1);
    }

    public byte next() {
        int next = sequence.incrementAndGet();

        if(next > 255)
            sequence.set(0);

        return (byte) sequence.get();
    }
}
