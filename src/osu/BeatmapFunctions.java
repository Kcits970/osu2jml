package osu;

import math.GeometryFunctions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeatmapFunctions {
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
}
