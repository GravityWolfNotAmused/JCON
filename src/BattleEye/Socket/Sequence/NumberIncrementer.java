package BattleEye.Socket.Sequence;

import java.util.concurrent.atomic.AtomicInteger;

public class NumberIncrementer {

    private AtomicInteger sequence;

    public NumberIncrementer()
    {
        sequence = new AtomicInteger(-1);
    }

    public int next() {
        int nextInt = sequence.incrementAndGet();

        if(nextInt > 255) {
            sequence.set(0);
            nextInt = sequence.get();
        }

        return nextInt;
    }
}
