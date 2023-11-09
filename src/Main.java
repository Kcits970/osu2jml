import jml.JMLDocument;
import osu.Beatmap;
import jml.siteswap.VanillaSiteswap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

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

        System.out.println("Apply Rainbow Colors to Props? (Y/N):");
        System.out.print(">> ");
        boolean rainbow = scanner.nextLine().equalsIgnoreCase("y");

        JMLDocument convertedPattern = new JMLDocument(
                beatmap,
                new VanillaSiteswap(siteswapString),
                handSequenceString,
                filler,
                rainbow
        );

        BufferedWriter writer = new BufferedWriter(new FileWriter("output.jml"));
        writer.write(convertedPattern.toString());
        writer.flush();
    }
}
