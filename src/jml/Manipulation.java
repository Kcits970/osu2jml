package jml;

public class Manipulation {
    public final String type;
    public final int path;

    public Manipulation(String type, int path) {
        if (!"throw".equals(type) && !"catch".equals(type) && !"holding".equals(type))
            throw new RuntimeException(String.format("invalid manipulation type: '%s'", type));

        this.type = type;
        this.path = path;
    }

    @Override
    public String toString() {
        if (type.equals("throw"))
            //mod="g=0.0" causes Juggling Lab to crash, somehow...
            return String.format("<%s path=\"%d\" mod=\"g=0.1\"/>", type, path);

        return String.format("<%s path=\"%d\"/>", type, path);
    }
}