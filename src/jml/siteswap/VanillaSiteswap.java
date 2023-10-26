package jml.siteswap;

public class VanillaSiteswap {
    String siteswapString;
    int numOfBalls;
    int siteswapLength;
    int[] beatArray;

    public VanillaSiteswap(String siteswapString) {
        this.siteswapString = siteswapString;
        numOfBalls = SiteswapFunctions.findAverageBeat(siteswapString);
        beatArray = SiteswapFunctions.siteswapStringToBeatArray(siteswapString);
        siteswapLength = beatArray.length;
    }

    public int beatAt(int siteswapPosition) {
        return beatArray[(siteswapPosition - 1) % beatArray.length];
    }

    public int getNumOfBalls() {
        return numOfBalls;
    }
}
