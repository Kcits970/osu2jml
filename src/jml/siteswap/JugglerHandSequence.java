package jml.siteswap;

public class JugglerHandSequence {
    public Juggler juggler;
    String jugglerHandSequenceString;

    public JugglerHandSequence(Juggler juggler, String jugglerHandSequenceString) {
        this.juggler = juggler;
        this.jugglerHandSequenceString = jugglerHandSequenceString;
    }

    public Hand handAt(int nthBeat) {
        char handSequenceCharacter = jugglerHandSequenceString.charAt((nthBeat - 1) % jugglerHandSequenceString.length());
        return handSequenceCharacter == 'L' ? Hand.LEFT_HAND : Hand.RIGHT_HAND;
    }
}
