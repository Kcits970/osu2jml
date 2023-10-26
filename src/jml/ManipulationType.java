package jml;

public enum ManipulationType {
    THROW ("throw"),
    CATCH ("catch"),
    HOLDING ("holding");

    private final String representation;

    ManipulationType(String representation) {
        this.representation = representation;
    }

    @Override
    public String toString() {return representation;}
}
