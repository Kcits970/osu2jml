package jml.siteswap;

public class JugglerHandSequence {
    public Juggler juggler;
    String jugglerHandSequenceString;

    public JugglerHandSequence(Juggler juggler, String jugglerHandSequenceString) {
        this.juggler = juggler;
        this.jugglerHandSequenceString = jugglerHandSequenceString.toLowerCase();
    }

    public Hand handAt(int nthBeat) {
        char handSequenceCharacter = jugglerHandSequenceString.charAt((nthBeat - 1) % jugglerHandSequenceString.length());
        return handSequenceCharacter == 'l' ? Hand.LEFT_HAND : Hand.RIGHT_HAND;
    }
}
