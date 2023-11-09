package jml.siteswap;

import java.util.*;

public class SiteswapStateTracker {
    VanillaSiteswap siteswap;
    JugglerHandSequence sequence;
    int currentSiteswapPosition;

    Map<Integer,Integer> propStatus;
    int lastThrownProp;

    public SiteswapStateTracker(VanillaSiteswap s, JugglerHandSequence seq) {
        siteswap = s;
        sequence = seq;
        propStatus = new HashMap<>();
        setupProps();
    }

    private void setupProps() {
        for (int i = 1; i <= siteswap.getNumOfBalls(); i++)
            propStatus.put(i, 0);
    }

    public void advanceState() {
        int currentSiteswapElement = siteswap.beatAt(currentSiteswapPosition++);
        int droppedProp = propStatus.entrySet()
                .stream()
                .filter(entry -> entry.getValue() == 0)
                .mapToInt(Map.Entry::getKey)
                .sorted()
                .findFirst()
                .orElse(0);

        if (droppedProp != 0)
            propStatus.put(droppedProp, currentSiteswapElement);
        lastThrownProp = droppedProp;

        propStatus.entrySet()
                .stream()
                .filter(entry -> entry.getValue() != 0)
                .forEach(entry -> entry.setValue(entry.getValue() - 1));
    }

    public int getThrownBall() {
        return lastThrownProp;
    }

    public String getLastAssignedHand() {
        return sequence.handAt(currentSiteswapPosition);
    }
}
