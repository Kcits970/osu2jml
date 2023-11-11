import jml.*;
import jml.siteswap.*;
import osu.Beatmap;

import java.awt.Color;
import java.io.*;
import java.util.*;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Beatmap File Location: \n>> ");
        String beatmapLocation = scanner.nextLine();
        Beatmap beatmap = new Beatmap(new File(beatmapLocation));

        System.out.print("Siteswap: \n>> ");
        String siteswapString = scanner.nextLine();

        System.out.print("Hand Alternation Sequence: \n>> ");
        String handSequenceString = scanner.nextLine();

        System.out.print("Filler Duration: \n>> ");
        double filler = Double.parseDouble(scanner.nextLine());

        System.out.println("Mod Combinations (Multiple Selection):");
        System.out.println("1: Easy");
        System.out.println("2: Hard Rock");
        System.out.println("3: Half Time");
        System.out.println("4: Double Time");
        System.out.print(">> ");
        String selection = scanner.nextLine();

        for (char c : selection.toCharArray()) {
            if (c == '1')
                beatmap = beatmap.easy();
            else if (c == '2')
                beatmap = beatmap.hardRock();
            else if (c == '3')
                beatmap = beatmap.halfTime();
            else if (c == '4')
                beatmap = beatmap.doubleTime();
            else
                throw new Exception();
        }

        beatmap.applyStackLayers();

        System.out.println("Apply Rainbow Colors to Props? (Y/N):");
        System.out.print(">> ");
        boolean rainbow = scanner.nextLine().equalsIgnoreCase("y");

        List<Event> convertedHitObjects = HitObjectConversionFunctions.convertHitObjects(
                beatmap.hitObjects,
                siteswapString,
                handSequenceString,
                filler
        );

        int numOfPaths = SiteswapFunctions.averageBeat(new SiteswapParser(siteswapString).parse());
        JMLDocument testJML = new JMLDocument();
        testJML.setPaths(numOfPaths);
        testJML.setJugglers(1);
        testJML.setDelay(convertedHitObjects.getLast().t + HitObjectConversionFunctions.FRAME_DISTANCE_SECONDS);
        testJML.addEvents(convertedHitObjects);
        testJML.setPPerm(
                String.join(
                        "",
                        IntStream.rangeClosed(1, numOfPaths)
                        .mapToObj(i -> String.format("(%d)", i))
                        .toList()
                )
        );

        for (int i = 1; i <= numOfPaths; i++) {
            if (rainbow)
                testJML.assignProp(i, Color.getHSBColor((1.0f / numOfPaths * i), 1.0f, 1.0f), 10);
            else
                testJML.assignProp(i, Color.WHITE, 10);
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter("output.jml"));
        writer.write(testJML.toString());
        writer.flush();
    }
}
