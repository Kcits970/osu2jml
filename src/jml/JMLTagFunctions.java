package jml;

import java.awt.*;

public class JMLTagFunctions {
    public static String generateDefaultTags() {
        return "<?xml version=\"1.0\"?>" + "<!DOCTYPE jml SYSTEM \"file://jml.dtd\">";
    }

    public static String generatePropTag(int paths, boolean rainbow) {
        if (!rainbow)
            return "<prop type=\"ball\" mod=\"color=black\"/>";

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < paths; i++) {
            Color propColor = Color.getHSBColor((1.0f / paths * i), 0.5f, 1.0f);
            builder.append(String.format("<prop type=\"ball\" mod=\"color=%s,%s,%s\"/>", 255 - propColor.getRed(), 255 - propColor.getGreen(), 255 - propColor.getBlue()));
        }

        return builder.toString();
    }

    public static String generateSetupTag(int paths, boolean rainbow) {
        return String.format("<setup jugglers=\"1\" paths=\"%d\" props=\"%s\"/>",
                paths,
                generatePropsAttributeValue(paths, rainbow)
        );
    }

    public static String generateSymmetryTag(int paths, double delay) {
        return String.format("<symmetry type=\"delay\" pperm=\"%s\" delay=\"%.4f\"/>",
                generatePPermAttributeValue(paths),
                delay);
    }

    public static String generatePropsAttributeValue(int paths, boolean rainbow) {
        StringBuilder builder = new StringBuilder();

        for (int i = 1; i <= paths; i++) {
            builder.append(rainbow ? i : 1);

            if (i != paths)
                builder.append(",");
        }

        return builder.toString();
    }

    public static String generatePPermAttributeValue(int paths) {
        StringBuilder builder = new StringBuilder();

        for (int i = 1; i <= paths; i++)
            builder.append("(").append(i).append(")");

        return builder.toString();
    }
}
