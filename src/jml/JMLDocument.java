package jml;

import jml.siteswap.SiteswapFunctions;
import jml.siteswap.SiteswapParser;
import osu.Beatmap;
import osu.HitObject;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class JMLDocument {
    int paths;
    double delay;
    boolean rainbowRendering;

    List<Event> universalEvents;

    public JMLDocument(Beatmap beatmap, String siteswap, String handSequence, double filler, boolean rainbow) {
        Point2D.Double shiftUnit = new Point2D.Double(beatmap.hitObjectRadius() / 10, beatmap.hitObjectRadius() / 10);

        for (HitObject hitObject : beatmap.hitObjects)
            hitObject.shift(shiftUnit);

        List<List<Event>> conversions = HitObjectConversionFunctions.convertHitObjects(
                beatmap.hitObjects,
                siteswap,
                handSequence,
                filler
        );

        universalEvents = new ArrayList<>();
        for (List<Event> conversion : conversions)
            universalEvents.addAll(conversion);

        universalEvents.sort(Comparator.comparingDouble(event -> event.t));

        paths = SiteswapFunctions.averageBeat(new SiteswapParser(siteswap).parse());
        delay = universalEvents.getLast().t + HitObjectConversionFunctions.FRAME_DISTANCE_SECONDS;
        rainbowRendering = rainbow;
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
