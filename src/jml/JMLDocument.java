package jml;

import jml.siteswap.JugglerHandSequence;
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

    public JMLDocument(Beatmap beatmap, VanillaSiteswap siteswap, JugglerHandSequence sequence, EmptyThresholdSet emptyThresholdSet, boolean rainbow) {
        Point2D.Double shiftUnit = new Point2D.Double(beatmap.hitObjectRadius() / 10, beatmap.hitObjectRadius() / 10);

        for (HitObject hitObject : beatmap.hitObjects)
            hitObject.shift(shiftUnit);

        List<EventGroup> conversions = HitObjectConversionFunctions.convertHitObjects(
                beatmap.hitObjects,
                siteswap,
                sequence
        );

        paths = siteswap.getNumOfBalls();
        delay = conversions.getLast().getEndTime() + emptyThresholdSet.filler;
        universalEvents = new ArrayList<>();
        rainbowRendering = rainbow;

        List<Stablizer> stablizers = HitObjectConversionFunctions.getStablizers(conversions, emptyThresholdSet, delay);

        for (EventGroup eventGroup : conversions)
            universalEvents.addAll(eventGroup.events);

        for (Stablizer stablizer : stablizers)
            universalEvents.addAll(stablizer.emptyEvents);
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
