package jml.siteswap;

public enum Hand {
    LEFT_HAND ("left"),
    RIGHT_HAND ("right");

    private final String representation;

    Hand(String representation) {
        this.representation = representation;
    }

    @Override
    public String toString() {return representation;}

    public static Hand getOppositeHand(Hand hand) {
        if (hand == LEFT_HAND)
            return RIGHT_HAND;
        else
            return LEFT_HAND;
    }
}
