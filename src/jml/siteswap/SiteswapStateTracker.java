package jml.siteswap;

import java.util.*;

public class SiteswapStateTracker {
    VanillaSiteswap siteswap;
    JugglerHandSequence sequence;
    int currentSiteswapPosition;

    Stack<Ball> ballsOnTheGround;
    Set<Ball> ballsInTheAir;
    Ball lastThrownBall;

    public SiteswapStateTracker(VanillaSiteswap s, JugglerHandSequence seq) {
        siteswap = s;
        sequence = seq;
        currentSiteswapPosition = 0;
        setupBalls();
    }

    private void setupBalls() {
        ballsOnTheGround = new Stack<>();
        for (int i = siteswap.getNumOfBalls(); i > 0; i--)
            ballsOnTheGround.push(new Ball(i));

        ballsInTheAir = new HashSet<>();
    }
    
    private void throwAvailableBall() {
        int throwHeight = siteswap.beatAt(currentSiteswapPosition);
        Ball ballToThrow = (throwHeight == 0) ? null : ballsOnTheGround.pop();
        lastThrownBall = ballToThrow;

        if (ballToThrow != null) {
            ballToThrow.flyInTheAir(throwHeight);
            ballsInTheAir.add(ballToThrow);
        }
    }

    public void advanceState() {
        currentSiteswapPosition++;
        ballsInTheAir.forEach(ball -> ball.continueFlying());

        Optional<Ball> droppedBall = ballsInTheAir.stream().filter(ball -> ball.isDropped()).findFirst();
        if (droppedBall.isPresent()) {
            ballsOnTheGround.push(droppedBall.get());
            ballsInTheAir.remove(droppedBall.get());
        }

        throwAvailableBall();
    }

    public Ball getThrownBall() {
        return lastThrownBall;
    }

    public Juggler getAssignedJuggler() {
        return sequence.juggler;
    }

    public Hand getLastAssignedHand() {
        return sequence.handAt(currentSiteswapPosition);
    }
}
