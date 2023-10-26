package jml.siteswap;

import java.util.ArrayList;
import java.util.List;

public class SiteswapFunctions {
    public static int siteswapCharacterToBeat(char siteswapCharacter) {
        if (siteswapCharacter >= '0' && siteswapCharacter <= '9')
            return siteswapCharacter - '0';
        else if (siteswapCharacter >= 'a' && siteswapCharacter <= 'z')
            return siteswapCharacter - 'a' + 10;
        else
            return -1;
    }

    public static int siteswapGroupToBeat(String siteswapGroup) {
        return Integer.parseInt(siteswapGroup.replaceAll("\\{|\\}", ""));
    }

    public static int[] siteswapStringToBeatArray(String siteswapString) {
        List<Integer> beatList = new ArrayList<>();

        for (int i = 0; i < siteswapString.length(); i++) {
            char currentSiteswapCharacter = siteswapString.charAt(i);

            if (currentSiteswapCharacter != '{')
                beatList.add(siteswapCharacterToBeat(currentSiteswapCharacter));
            else {
                int closingBraceIndex = nextIndexOfCharIn(siteswapString, i, '}');
                beatList.add(siteswapGroupToBeat(siteswapString.substring(i, closingBraceIndex + 1)));
                i = closingBraceIndex;
            }
        }

        return beatList.stream().mapToInt(Integer::intValue).toArray();
    }

    private static int nextIndexOfCharIn(String s, int offset, char match) {
        for (int i = offset; i < s.length(); i++)
            if (s.charAt(i) == match)
                return i;

        return -1;
    }

    public static int findAverageBeat(String siteswapString) {
        int[] beats = siteswapStringToBeatArray(siteswapString);

        int averageBeat = 0;
        for (int beat : beats)
            averageBeat += beat;

        return averageBeat / beats.length;
    }
}
