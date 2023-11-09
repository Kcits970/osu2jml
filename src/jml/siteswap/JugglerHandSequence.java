package jml.siteswap;

public class JugglerHandSequence {
    public int juggler;
    String jugglerHandSequenceString;

    public JugglerHandSequence(int juggler, String jugglerHandSequenceString) {
        this.juggler = juggler;
        this.jugglerHandSequenceString = jugglerHandSequenceString.toLowerCase();
    }

    public String handAt(int nthBeat) {
        char handSequenceCharacter = jugglerHandSequenceString.charAt((nthBeat - 1) % jugglerHandSequenceString.length());
        return handSequenceCharacter == 'l' ? "left" : "right";
    }
}
