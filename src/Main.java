import jml.EmptyThresholdSet;
import jml.JMLDocument;
import jml.siteswap.Juggler;
import jml.siteswap.JugglerHandSequence;
import osu.Beatmap;
import jml.siteswap.VanillaSiteswap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

public class Main {
    static final double HOLD_THRESHOLD = 0.2;
    static final double RESET_THRESHOLD = 0.6;

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

        System.out.println("Apply Rainbow Colors to Props? (Y/N):");
        System.out.print(">> ");
        boolean rainbow = scanner.nextLine().equalsIgnoreCase("y");

        JMLDocument convertedPattern = new JMLDocument(
                beatmap,
                new VanillaSiteswap(siteswapString),
                new JugglerHandSequence(new Juggler(1), handSequenceString),
                new EmptyThresholdSet(filler, HOLD_THRESHOLD, RESET_THRESHOLD),
                rainbow
        );

        BufferedWriter writer = new BufferedWriter(new FileWriter("output.jml"));
        writer.write(convertedPattern.toString());
        writer.flush();
    }
}
