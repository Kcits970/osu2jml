package jml.siteswap;

import java.util.*;
import java.util.stream.Collectors;

public class PropStateTracker {
    List<List<Integer>> siteswap;
    int currentSiteswapPosition;

    Stack<Integer> unusedProps;
    Map<Integer,Integer> propStatus;

    public PropStateTracker(List<List<Integer>> siteswap) {
        this.siteswap = siteswap;

        unusedProps = new Stack<>();
        for (int propID = SiteswapFunctions.averageBeat(siteswap); propID > 0; propID--)
            unusedProps.push(propID);

        propStatus = new HashMap<>();
    }

    public Set<Integer> advanceState() {
        List<Integer> currentSiteswapElement = siteswap.get(currentSiteswapPosition++ % siteswap.size());
        int numOfThrowingProps = (int) currentSiteswapElement.stream()
                .filter(number -> number != 0)
                .count();
        List<Integer> propsToThrow = propStatus.entrySet()
                .stream()
                .filter(entry -> entry.getValue() == 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        //Too many props to throw.
        if (propsToThrow.size() > numOfThrowingProps)
            throw new InvalidSiteswapException();

        //Not enough props to throw. (only if there are not enough unused props to compensate.)
        if (propsToThrow.size() < numOfThrowingProps) {
            if (unusedProps.size() < numOfThrowingProps - propsToThrow.size())
                throw new InvalidSiteswapException();

            while (propsToThrow.size() < numOfThrowingProps)
                propsToThrow.add(unusedProps.pop());
        }

        Collections.sort(propsToThrow);

        //'Throw' the props in the 'air'. (the value of the map represents the prop's current 'height'.)
        for (int i = 0; i < numOfThrowingProps; i++)
            propStatus.put(propsToThrow.get(i), currentSiteswapElement.get(i));

        //Let the props 'fall'. (we find every prop that is in the 'air', then decrease its 'height' by 1.)
        propStatus.entrySet()
                .stream()
                .filter(entry -> entry.getValue() != 0)
                .forEach(entry -> entry.setValue(entry.getValue() - 1));

        return new HashSet<>(propsToThrow);
    }
}
