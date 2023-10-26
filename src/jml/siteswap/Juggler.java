package jml.siteswap;

public class Juggler {
    final int ID;

    public Juggler(int jugglerID) {
        ID = jugglerID;
    }

    @Override
    public String toString() {
        return String.valueOf(ID);
    }
}
