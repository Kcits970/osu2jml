package jml;

import osu.Beatmap;
import jml.siteswap.VanillaSiteswap;
import osu.HitObject;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class JMLDocument {
    int paths;
    double delay;
    boolean rainbowRendering;

    List<Event> universalEvents;

    public JMLDocument(Beatmap beatmap, VanillaSiteswap siteswap, String handSequence, double filler, boolean rainbow) {
        Point2D.Double shiftUnit = new Point2D.Double(beatmap.hitObjectRadius() / 10, beatmap.hitObjectRadius() / 10);

        for (HitObject hitObject : beatmap.hitObjects)
            hitObject.shift(shiftUnit);

        List<List<Event>> conversions = HitObjectConversionFunctions.convertHitObjects(
                beatmap.hitObjects,
                siteswap,
                handSequence
        );

        paths = siteswap.getNumOfBalls();
        delay = conversions.getLast().getLast().t + filler;
        universalEvents = new ArrayList<>();
        rainbowRendering = rainbow;

        List<List<Event>> stabilizers = HitObjectConversionFunctions.getStablizers(conversions, delay);

        for (List<Event> conversion : conversions)
            universalEvents.addAll(conversion);

        for (List<Event> stabilizer : stabilizers)
            universalEvents.addAll(stabilizer);
    }

    @Override
    public String toString() {
        StringBuilder jmlBuilder = new StringBuilder();

        jmlBuilder.append(JMLTagFunctions.generateDefaultTags());
        jmlBuilder.append("<jml>\n");
        jmlBuilder.append("<pattern>\n");

        jmlBuilder.append(JMLTagFunctions.generatePropTag(paths, rainbowRendering)).append('\n');
        jmlBuilder.append(JMLTagFunctions.generateSetupTag(paths, rainbowRendering)).append('\n');
        jmlBuilder.append(JMLTagFunctions.generateSymmetryTag(paths, delay)).append('\n');

        for (Event e : universalEvents)
            jmlBuilder.append(e.toString()).append('\n');

        jmlBuilder.append("</pattern>\n");
        jmlBuilder.append("</jml>");

        return jmlBuilder.toString();
    }
}
