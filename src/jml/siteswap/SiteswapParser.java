package jml.siteswap;

import java.util.*;

public class SiteswapParser {
    private String siteswapString;
    private int position;

    public static List<List<Integer>> parse(String siteswapString) {
        return new SiteswapParser(siteswapString).parse();
    }

    private SiteswapParser(String siteswapString) {
        this.siteswapString = siteswapString.replaceAll("\\s","").toLowerCase();
    }

    /*
    Siteswap grammar:

    Siteswap -> (Element)+
    Element -> Multiplex | AtomicNumber
    Multiplex -> '[' (AtomicNumber)+ ']'
    AtomicNumber -> [0-9] | [a-z] | '{' [0-9]+ '}'
    */

    private boolean reachedEnd() {
        return position == siteswapString.length();
    }

    private char currentChar() {
        return siteswapString.charAt(position);
    }

    private boolean requestDiscard(char charToDiscard) {
        if (!reachedEnd() && charToDiscard == currentChar()) {
            position++;
            return true;
        } else
            return false;
    }

    private void forceDiscard(char charToDiscard) {
        if (!reachedEnd() && charToDiscard == currentChar())
            position++;
        else
            throw new InvalidSiteswapException();
    }

    private boolean foresee(char charToForesee) {
        return siteswapString.substring(position).contains(String.valueOf(charToForesee));
    }

    private List<List<Integer>> parse() {
        List<List<Integer>> siteswap = new ArrayList<>();

        while (!reachedEnd())
            siteswap.add(nextElement());

        return siteswap;
    }

    private List<Integer> nextElement() {
        if ('[' == currentChar())
            return nextMultiplex();
        else
            return List.of(nextAtomicNumber());
    }

    private List<Integer> nextMultiplex() {
        List<Integer> multiplex = new ArrayList<>();

        forceDiscard('[');
        if (!foresee(']'))
            throw new InvalidSiteswapException();

        while (!requestDiscard(']'))
            multiplex.add(nextAtomicNumber());

        return multiplex;
    }

    private Integer nextAtomicNumber() {
        if (requestDiscard('{')) {
            int numberStartPos = position;
            while (currentChar() >= '0' && currentChar() <= '9')
                position++;
            forceDiscard('}');

            return Integer.parseInt(siteswapString.substring(numberStartPos, position));
        }

        if ('0' <= currentChar() && '9' >= currentChar()) {
            return siteswapString.charAt(position++) - '0';
        }

        if ('a' <= currentChar() && 'z' >= currentChar()) {
            return siteswapString.charAt(position++) - 'a' + 10;
        }

        System.out.println(currentChar());
        throw new InvalidSiteswapException();
    }
}