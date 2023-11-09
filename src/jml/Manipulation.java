package jml;

public class Manipulation {
    private static final double GRAVITY = 0.1; //for some reason, setting gravity=0 causes juggling lab to crash.
    String type;
    int path;

    public Manipulation(String type, int path) {
        if (!"throw".equals(type) && !"catch".equals(type) && !"holding".equals(type))
            throw new RuntimeException("Manipulation type must be either throw, catch, or holding (case sensitive)");

        this.type = type;
        this.path = path;
    }

    @Override
    public String toString() {
        if (type.equals("throw"))
            return String.format("<%s path=\"%d\" type=\"toss\" mod=\"g=%.2f\"/>", type, path, GRAVITY);

        return String.format("<%s path=\"%d\"/>", type, path);
    }
}