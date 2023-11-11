package osu;

import math.GeometryFunctions;

import java.io.*;
import java.util.*;
import java.util.function.Function;

import static java.lang.Double.parseDouble;

public class Beatmap {
    public List<HitObject> hitObjects;

    public double stackLeniency;
    public double approachRate;
    public double circleSize;

    public Beatmap(File f) throws Exception {
        Map<String,List<String>> rawBeatmapData = readRawBeatmapData(f);

        stackLeniency = parseDouble(findValue(rawBeatmapData, "[General]", "StackLeniency"));
        approachRate = parseDouble(findValue(rawBeatmapData, "[Difficulty]", "ApproachRate"));
        circleSize = parseDouble(findValue(rawBeatmapData, "[Difficulty]", "CircleSize"));
        hitObjects = new ArrayList<>();

        double sliderMultiplier = parseDouble(findValue(rawBeatmapData, "[Difficulty]", "SliderMultiplier"));

        //Reading the timing points from the raw beatmap data.
        List<TimingPoint> rawTimingPoints = rawBeatmapData.get("[TimingPoints]").stream().map(TimingPoint::new).toList();
        List<TimingPoint> timingPoints = new ArrayList<>();

        TimingPoint currentParent = null;
        for (TimingPoint rawTimingPoint : rawTimingPoints) {
            if (!rawTimingPoint.inherited)
                currentParent = rawTimingPoint;

            if (rawTimingPoint.inherited)
                timingPoints.add(new TimingPoint(rawTimingPoint, currentParent));
            else
                timingPoints.add(new TimingPoint(rawTimingPoint, null));
        }

        //Reading the hit objects from the raw beatmap data.
        List<HitObject> rawHitObjects = rawBeatmapData.get("[HitObjects]").stream().map(HitObjectFactory::getHitObject).toList();

        Function<HitObject,TimingPoint> timingPointFinder = hitObject ->
                timingPoints.stream()
                        .filter(timingPoint -> timingPoint.time <= hitObject.time)
                        .reduce((first,second) -> second)
                        .get();

        for (HitObject rawHitObject : rawHitObjects) {
            if (rawHitObject instanceof Slider)
                hitObjects.add(new Slider((Slider) rawHitObject, sliderMultiplier, timingPointFinder.apply(rawHitObject)));
            else
                hitObjects.add(rawHitObject);
        }
    }

    private static Map<String,List<String>> readRawBeatmapData(File f) throws Exception {
        Map<String,List<String>> sections = new HashMap<>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
        reader.readLine(); //skip the first line: "osu file format v14"

        String currentSectionName = null;
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            line = line.trim();

            if (line.startsWith("//") || line.isBlank())
                continue;
            else if (line.startsWith("[")) {
                currentSectionName = line;
                sections.putIfAbsent(currentSectionName, new ArrayList<>());
            }
            else
                sections.get(currentSectionName).add(line);
        }
        reader.close();

        return sections;
    }

    private static String findValue(Map<String,List<String>> rawBeatmapData, String sectionName, String attributeName) {
        if (!rawBeatmapData.containsKey(sectionName))
            throw new RuntimeException(String.format("Unable to find section name '%s'", sectionName));

        Optional<String> possibleValue = rawBeatmapData.get(sectionName)
                .stream()
                .filter(string -> string.startsWith(attributeName))
                .map(string -> string.substring(string.indexOf(':') + 1))
                .findAny();

        if (possibleValue.isEmpty())
            throw new RuntimeException(String.format("Unable to find '%s' in '%s'", attributeName, sectionName));

        return possibleValue.get();
    }

    public void applyStackLayers() {
        /*
        This method shifts all the hit objects based on their stack layers.
        It effectively sets the beatmap ready for "rendering".
        Call to this method should be done AFTER applying the beatmap modifiers.
        */

        //The value of 'stackOffset' is taken from https://gist.github.com/peppy/1167470. (Line 6)
        double stackOffset = -hitObjectRadius(circleSize)/10;
        Map<HitObject,Integer> stackLayerMap = calculateStackLayers(hitObjects, approachRate, stackLeniency);

        for (HitObject hitObject : hitObjects)
            hitObject.translate(stackLayerMap.get(hitObject) * stackOffset, stackLayerMap.get(hitObject) * stackOffset);
    }

    public static double preempt(double approachRate) {
        if (approachRate < 5)
            return 1200 + (double) 600 * (5 - approachRate) / 5;
        else if (approachRate > 5)
            return 1200 - (double) 750 * (approachRate - 5) / 5;
        else
            return 1200;
    }

    public static double hitObjectRadius(double circleSize) {
        return 54.5 - 4.48 * circleSize;
    }

    public static Map<HitObject,Integer> calculateStackLayers(List<HitObject> hitObjects, double approachRate, double stackLeniency) {
        //DO NOT ALTER ANY CODE IN THIS METHOD!! IT WORKS, SO IT'S PROBABLY BEST TO NOT TOUCH ANYTHING!
        //The algorithm here is entirely taken from https://gist.github.com/peppy/1167470.
        //Planning to de-nest some logic here if possible, but it works, so I'm not going to really mess with it.

        Map<HitObject,Integer> stackLayerMap = new HashMap<>();
        for (HitObject hitObject : hitObjects)
            stackLayerMap.put(hitObject, 0);

        final int STACK_LENIENCE = 3;

        for (int i = hitObjects.size() - 1; i > 0; i--) {
            int n = i;

            HitObject objectI = hitObjects.get(i);

            if (stackLayerMap.get(objectI) != 0 || objectI instanceof Spinner) continue;

            if (objectI instanceof HitCircle) {
                while (--n >= 0) {
                    HitObject objectN = hitObjects.get(n);

                    if (objectN instanceof Spinner) continue;

                    HitObject spanN = objectN instanceof Slider ? (Slider) objectN : null;

                    if (objectI.time - (preempt(approachRate) * stackLeniency) > objectN.endTime())
                        break;

                    if (spanN != null && GeometryFunctions.distance(spanN.endPosition(), objectI.position()) < STACK_LENIENCE) {
                        int offset = stackLayerMap.get(objectI) - stackLayerMap.get(objectN) + 1;
                        for (int j = n + 1; j <= i; j++) {
                            if (GeometryFunctions.distance(spanN.endPosition(), hitObjects.get(j).position()) < STACK_LENIENCE)
                                stackLayerMap.put(hitObjects.get(j), stackLayerMap.get(hitObjects.get(j)) - offset);
                        }

                        break;
                    }

                    if (GeometryFunctions.distance(objectN.position(), objectI.position()) < STACK_LENIENCE) {
                        stackLayerMap.put(objectN, stackLayerMap.get(objectI) + 1);
                        objectI = objectN;
                    }
                }
            }
            else if (objectI instanceof Slider) {
                while (--n >= 0) {
                    HitObject objectN = hitObjects.get(n);

                    if (objectN instanceof Spinner) continue;

                    HitObject spanN = objectN instanceof Slider ? (Slider) objectN : null;

                    if (objectI.time - (preempt(approachRate) * stackLeniency) > objectN.time)
                        break;

                    if (GeometryFunctions.distance((spanN != null ? spanN.endPosition() : objectN.position()), objectI.position()) < STACK_LENIENCE) {
                        stackLayerMap.put(objectN, stackLayerMap.get(objectI) + 1);
                        objectI = objectN;
                    }
                }
            }
        }

        return stackLayerMap;
    }

    public void easy() {
        approachRate *= 0.5;
        circleSize *= 0.5;
    }

    public void hardRock() {
        approachRate = Math.min(10, approachRate * 1.4);
        circleSize *= Math.min(10, circleSize * 1.3);

        for (HitObject hitObject : hitObjects)
            hitObject.flip();
    }

    public void halfTime() {
        for (HitObject hitObject : hitObjects)
            hitObject.adjustSpeed(0.75);
    }

    public void doubleTime() {
        for (HitObject hitObject : hitObjects)
            hitObject.adjustSpeed(1.5);
    }

    public void applyModifier(String modifier) {
        List<String> modifierList = new ArrayList<>();
        for (int i = 0; i < modifier.length(); i+=2)
            modifierList.add(modifier.substring(i, Math.min(modifier.length(), i+2)));

        for (String subModifier : modifierList) {
            if ("ez".equalsIgnoreCase(subModifier))
                easy();
            else if ("hr".equalsIgnoreCase(subModifier))
                hardRock();
            else if ("ht".equalsIgnoreCase(subModifier))
                halfTime();
            else if ("dt".equalsIgnoreCase(subModifier))
                doubleTime();
            else
                throw new RuntimeException(String.format("'%s' is not a recognized combination of beatmap modifiers", modifier));
        }
    }
}