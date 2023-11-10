package jml.siteswap;

import java.util.List;

public class SiteswapFunctions {
    public static int averageBeat(List<List<Integer>> siteswap) {
        return siteswap.stream()
                .mapToInt(list -> list.stream().mapToInt(Integer::intValue).sum())
                .sum() / siteswap.size();
    }
}
