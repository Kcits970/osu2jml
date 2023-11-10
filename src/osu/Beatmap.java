package osu;

import math.GeometryFunctions;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Beatmap {
    private static final List<String> SECTION_NAMES;
    static {
        String[] sectionsArray = {
                "[General]",
                "[Editor]",
                "[Metadata]",
                "[Difficulty]",
                "[Events]",
                "[TimingPoints]",
                "[Colours]",
                "[HitObjects]"
        };
        SECTION_NAMES = List.of(sectionsArray);
    }

    public File originalFile;
    public Map<String,List<String>> sections;
    public List<TimingPoint> timingPoints;
    public List<HitObject> hitObjects;

    public double stackLeniency;
    public double approachRate;
    public double circleSize;
    public double sliderMultiplier;

    public Beatmap(File f) throws IOException {
        originalFile = f;
        sections = new HashMap<>();
        timingPoints = new ArrayList<>();
        hitObjects = new ArrayList<>();

        setupSections();
        setupGeneralParameters();
        setupDifficultyParameters();
        setupTimingPoints();
        setupHitObjects();
        calculateStackLayers();
    }

    void setupSections() throws IOException {
        for (String s : SECTION_NAMES)
            sections.put(s, new ArrayList<>());

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(originalFile)));
        reader.readLine(); //skip the first line: "osu file format v14"

        String currentSectionName = null;
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            line = line.trim();

            if (line.startsWith("//") || line.isBlank())
                continue;
            else if (line.startsWith("["))
                currentSectionName = line;
            else
                sections.get(currentSectionName).add(line);
        }

        reader.close();
    }

    void setupGeneralParameters() {
        List<String> generalStrings = sections.get("[General]");

        for (String s : generalStrings) {
            String generalTag = s.split(":")[0];
            String generalValue = s.split(":")[1];

            if (generalTag.startsWith("StackLeniency"))
                stackLeniency = Double.parseDouble(generalValue);
        }
    }

    void setupDifficultyParameters() {
        List<String> difficultyStrings = sections.get("[Difficulty]");

        for (String s : difficultyStrings) {
            String difficultyTag = s.split(":")[0];
            String difficultyValue = s.split(":")[1];

            if (difficultyTag.startsWith("CircleSize"))
                circleSize = Double.parseDouble(difficultyValue);
            else if (difficultyTag.startsWith("ApproachRate"))
                approachRate = Double.parseDouble(difficultyValue);
            else if (difficultyTag.startsWith("SliderMultiplier"))
                sliderMultiplier = Double.parseDouble(difficultyValue);
        }
    }

    void setupTimingPoints() {
        List<String> timingPointStrings = sections.get("[TimingPoints]");

        TimingPoint lastUninheritedTimingPoint = null;
        for (String s : timingPointStrings) {
            TimingPoint currentTimingPoint = new TimingPoint(s);
            timingPoints.add(currentTimingPoint);

            if (currentTimingPoint.uninherited)
                lastUninheritedTimingPoint = currentTimingPoint;
            else
                currentTimingPoint.inheritFields(lastUninheritedTimingPoint);
        }
    }

    void setupHitObjects() {
        List<String> hitObjectStrings = sections.get("[HitObjects]");

        for (String s : hitObjectStrings)
            hitObjects.add(HitObjectFactory.getHitObject(s));

        for (int i = 0; i < hitObjects.size(); i++) {
            HitObject currentHitObject = hitObjects.get(i);
            if (currentHitObject instanceof Slider) {
                hitObjects.set(i, new Slider((Slider) currentHitObject, sliderMultiplier, BeatmapFunctions.findFurthestTimingPointUntil(timingPoints, currentHitObject.time)));
            }
        }
    }

    void calculateStackLayers() {
        /*
        The code here is entirely taken from https://gist.github.com/peppy/1167470.
        Some of the syntax has been modified from C# to Java, but the algorithm is entirely the same.
        */

        final int STACK_LENIENCE = 3;

        for (int i = hitObjects.size() - 1; i > 0; i--) {
            int n = i;

            HitObject objectI = hitObjects.get(i);

            if (objectI.stackLayer != 0 || objectI instanceof Spinner) continue;

            if (objectI instanceof HitCircle) {
                while (--n >= 0) {
                    HitObject objectN = hitObjects.get(n);

                    if (objectN instanceof Spinner) continue;

                    HitObject spanN = objectN instanceof Slider ? (Slider) objectN : null;

                    if (objectI.time - (preempt() * stackLeniency) > objectN.endTime())
                        break;

                    if (spanN != null && GeometryFunctions.distance(spanN.endPosition(), objectI.position()) < STACK_LENIENCE) {
                        int offset = objectI.stackLayer - objectN.stackLayer + 1;
                        for (int j = n + 1; j <= i; j++) {
                            if (GeometryFunctions.distance(spanN.endPosition(), hitObjects.get(j).position()) < STACK_LENIENCE)
                                hitObjects.get(j).stackLayer -= offset;
                        }

                        break;
                    }

                    if (GeometryFunctions.distance(objectN.position(), objectI.position()) < STACK_LENIENCE) {
                        objectN.stackLayer = objectI.stackLayer + 1;
                        objectI = objectN;
                    }
                }
            }
            else if (objectI instanceof Slider) {
                while (--n >= 0) {
                    HitObject objectN = hitObjects.get(n);

                    if (objectN instanceof Spinner) continue;

                    HitObject spanN = objectN instanceof Slider ? (Slider) objectN : null;

                    if (objectI.time - (preempt() * stackLeniency) > objectN.time)
                        break;

                    if (GeometryFunctions.distance((spanN != null ? spanN.endPosition() : objectN.position()), objectI.position()) < STACK_LENIENCE) {
                        objectN.stackLayer = objectI.stackLayer + 1;
                        objectI = objectN;
                    }
                }
            }
        }
    }

    void resetAllStackLayers() {
        for (HitObject hitObject : hitObjects)
            hitObject.resetStackProperties();
    }

    void recalculateStackLayers() {
        resetAllStackLayers();
        calculateStackLayers();
    }

    public double preempt() {
        if (approachRate < 5)
            return 1200 + (double) 600 * (5 - approachRate) / 5;
        else if (approachRate > 5)
            return 1200 - (double) 750 * (approachRate - 5) / 5;
        else
            return 1200;
    }

    public double hitObjectRadius() {
        return 54.5 - 4.48 * circleSize;
    }

    public Beatmap easy() {
        approachRate *= 0.5;
        circleSize *= 0.5;
        recalculateStackLayers();

        return this;
    }

    public Beatmap hardRock() {
        approachRate *= 1.4;
        circleSize *= 1.3;

        if (approachRate > 10)
            approachRate = 10;

        if (circleSize > 10)
            circleSize = 10;

        for (HitObject hitObject : hitObjects)
            hitObject.flip();

        recalculateStackLayers();

        return this;
    }

    public Beatmap halfTime() {
        for (HitObject hitObject : hitObjects)
            hitObject.adjustSpeed(1 / 0.75);

        return this;
    }

    public Beatmap doubleTime() {
        for (HitObject hitObject : hitObjects)
            hitObject.adjustSpeed(1 / 1.5);

        return this;
    }
}
