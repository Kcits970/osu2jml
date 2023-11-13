package jml.siteswap;

import java.util.List;

public class SiteswapFunctions {
    public static int averageBeat(List<List<Integer>> siteswap) {
        int sumOfAllElements = 0;
        for (List<Integer> siteswapElement : siteswap)
            sumOfAllElements += siteswapElement.stream().mapToInt(Integer::intValue).sum();

        return sumOfAllElements / siteswap.size();
    }
}
