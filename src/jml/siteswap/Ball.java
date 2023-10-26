package jml.siteswap;

public class Ball {
    final int ballNumber;
    int beatsLeftToReachGround;

    public Ball(int ballNumber) {
        this.ballNumber = ballNumber;
    }

    public void flyInTheAir(int beats) {
        beatsLeftToReachGround = beats;
    }

    public void continueFlying() {
        beatsLeftToReachGround--;
    }

    public boolean isDropped() {
        return beatsLeftToReachGround == 0;
    }

    public int getBallNumber() {
        return ballNumber;
    }

    @Override
    public String toString() {
        return "Prop " + ballNumber;
    }
}
