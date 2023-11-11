package jml.siteswap;

import java.util.*;

public class PropStateTracker {
    List<List<Integer>> siteswap;
    int currentSiteswapPosition;

    Map<Integer,Integer> propStatus;

    public PropStateTracker(List<List<Integer>> siteswap) {
        this.siteswap = siteswap;
        propStatus = new HashMap<>();
        for (int i = 1; i <= SiteswapFunctions.averageBeat(siteswap); i++)
            propStatus.put(i, 0);
    }

    public Set<Integer> advanceState() {
        List<Integer> currentSiteswapElement = siteswap.get(currentSiteswapPosition++ % siteswap.size());
        List<Integer> propsToThrow = propStatus.entrySet()
                .stream()
                .filter(entry -> entry.getValue() == 0)
                .mapToInt(Map.Entry::getKey)
                .sorted()
                .limit(currentSiteswapElement.size())
                .boxed()
                .toList();

        for (int i = 0; i < currentSiteswapElement.size(); i++)
            propStatus.put(propsToThrow.get(i), currentSiteswapElement.get(i));

        propStatus.entrySet()
                .stream()
                .filter(entry -> entry.getValue() != 0)
                .forEach(entry -> entry.setValue(entry.getValue() - 1));

        return new HashSet<>(propsToThrow);
    }
}
