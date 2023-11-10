package jml.siteswap;

import java.util.*;

public class SiteswapParser {
    Queue<Character> tokens;

    public SiteswapParser(String siteswapString) {
        tokens = new LinkedList<>();
        for (char c : siteswapString.replaceAll("\\s", "").toLowerCase().toCharArray())
            tokens.add(c);
    }

    /*
    Siteswap grammar:

    Siteswap -> (Beat)+
    Beat -> Multiplex | AtomicBeat
    Multiplex -> '[' (AtomicBeat)+ ']'
    AtomicBeat -> [0-9] | [a-z] | '{' [0-9]+ '}'
    */

    public List<List<Integer>> parse() {
        //This method can only be used once.
        //Once the tokens are all consumed, the method will merely return an empty list.
        List<List<Integer>> siteswap = new ArrayList<>();

        while (!tokens.isEmpty())
            siteswap.add(nextBeat());

        return siteswap;
    }

    private List<Integer> nextBeat() {
        if ('[' == tokens.peek())
            return nextMultiplex();
        else
            return List.of(nextAtomicBeat());
    }

    private List<Integer> nextMultiplex() {
        List<Integer> multiplex = new ArrayList<>();

        forceDiscard('[');
        while (!requestDiscard(']'))
            multiplex.add(nextAtomicBeat());

        return multiplex;
    }

    private Integer nextAtomicBeat() {
        if (requestDiscard('{')) {
            int atomicBeat = nextGreedyNumber();
            forceDiscard('}');
            return atomicBeat;
        }

        if ('0' <= tokens.peek() && '9' >= tokens.peek())
            return tokens.poll() - '0';

        if ('a' <= tokens.peek() && 'z' >= tokens.peek())
            return tokens.poll() - 'a' + 10;

        throw new RuntimeException(String.format("unknown character %c", tokens.peek()));
    }

    private Integer nextGreedyNumber() {
        StringBuilder numberBuilder = new StringBuilder();
        while ('0' <= tokens.peek() && '9' >= tokens.peek())
            numberBuilder.append(tokens.poll());

        return Integer.parseInt(numberBuilder.toString());
    }

    private boolean requestDiscard(char character) {
        if (character == tokens.peek()) {
            tokens.poll();
            return true;
        } else
            return false;
    }

    private void forceDiscard(char character) {
        if (character == tokens.peek())
            tokens.poll();
        else
            throw new RuntimeException(String.format("%c expected", character));
    }

    public static boolean validate(String siteswapString) {
        return false;
    }
}
