package osu;

public class HitObjectFactory {
    static final int TYPE_INDEX = 3;
    static final byte HITCIRCLE_MASK = 0b00000001;
    static final byte SLIDER_MASK = 0b00000010;
    static final byte SPINNER_MASK = 0b00001000;

    static HitObject getHitObject(String hitObjectString) {
        byte hitObjectType = Byte.parseByte(hitObjectString.split(",")[TYPE_INDEX]);

        if ((hitObjectType & HITCIRCLE_MASK) != 0)
            return new HitCircle(hitObjectString);
        else if ((hitObjectType & SLIDER_MASK) != 0)
            return new Slider(hitObjectString);
        else if ((hitObjectType & SPINNER_MASK) != 0)
            return new Spinner(hitObjectString);
        else
            return null;
    }
}
