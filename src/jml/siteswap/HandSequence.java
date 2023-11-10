package jml.siteswap;

import java.util.Iterator;
import java.util.regex.Pattern;

public class HandSequence {
    private String sequence;

    public HandSequence(String sequence) {
        if (sequence.isEmpty() || Pattern.compile("[^lrLR]").matcher(sequence).find())
            throw new RuntimeException(String.format("invalid hand sequence specification: '%s'", sequence));

        this.sequence = sequence.toLowerCase();
    }

    public Iterator<String> iterator() {
        return new Iterator<>() {
            int index;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public String next() {
                if (index == sequence.length())
                    index = 0;

                if (sequence.charAt(index++) == 'l')
                    return "left";
                else
                    return "right";
            }
        };
    }
}
