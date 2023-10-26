package jml;

import jml.siteswap.Ball;

public class Manipulation {
    private static final double GRAVITY = 0.1; //for some reason, setting gravity=0 causes juggling lab to crash.
    ManipulationType type;
    int path;

    public Manipulation(ManipulationType type, int path) {
        this.type = type;
        this.path = path;
    }

    public Manipulation(ManipulationType type, Ball ball) {
        this(type, ball.getBallNumber());
    }

    @Override
    public String toString() {
        if (type == ManipulationType.THROW)
            return String.format("<%s path=\"%d\" type=\"toss\" mod=\"g=%.2f\"/>", type, path, GRAVITY);

        return String.format("<%s path=\"%d\"/>", type, path);
    }
}