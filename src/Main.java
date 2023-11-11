import jml.*;
import jml.siteswap.*;
import osu.Beatmap;

import java.awt.Color;
import java.io.*;
import java.util.*;
import java.util.stream.IntStream;

public class Main {
    //List of recognized options: 's'ite's'wap, beatmap 'm'odifier, 'h'and sequence, 'f'iller duration, prop 'c'olor, prop color 's'aturation.
    static final List<String> recognizedArguments = List.of("-ss", "-m", "-h", "-f", "-c", "-s");
    static final Color rainbowColor = new Color(255,255,255,0);
    static final Map<String,Color> colorMap = Map.of(
            "rainbow", rainbowColor,
            "white", Color.WHITE,
            "red", Color.RED,
            "green", Color.GREEN,
            "blue", Color.BLUE,
            "yellow", Color.YELLOW,
            "cyan", Color.CYAN,
            "magenta", Color.MAGENTA,
            "black", Color.BLACK
    );

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        //Parsing mandatory arguments. (The first 2 arguments should specify input and output file paths.)
        if (args.length < 2)
            throw new RuntimeException("missing input source/output destination argument");

        Beatmap beatmap = new Beatmap(new File(args[0]));
        File outputFile = new File(args[1]);

        if (outputFile.exists() && outputFile.isFile()) {
            System.out.printf("specified output location \"%s\" already exists, continue? (Y/N)\n", outputFile.getAbsolutePath());
            System.out.print(">> ");

            String input = scanner.nextLine();

            if ("N".equalsIgnoreCase(input))
                return;
            else if (!"Y".equalsIgnoreCase(input))
                throw new RuntimeException(String.format("'%s' is not a recognized command here", input));
        }

        //Parsing optional arguments. (Each argument takes exactly one parameter. (only if it exists))
        Map<String,String> optionalArguments = new HashMap<>();

        for (int i = 2; i < args.length; i++) {
            if (recognizedArguments.contains(args[i]))
                if (i+1 < args.length)
                    optionalArguments.put(args[i], args[++i]);
                else
                    throw new RuntimeException(String.format("no parameters provided for argument '%s'", args[i]));
            else
                throw new RuntimeException(String.format("'%s' is not a recognized argument", args[i]));
        }

        String siteswapString = optionalArguments.getOrDefault("-ss", "3");
        String modifierString = optionalArguments.getOrDefault("-m", "");
        String handSequenceString = optionalArguments.getOrDefault("-h", "LR");
        String fillerString = optionalArguments.getOrDefault("-f", "1.0");
        String colorString = optionalArguments.getOrDefault("-c", "white");
        String saturationString = optionalArguments.getOrDefault("-s", "1.0");

        List<List<Integer>> siteswap = new SiteswapParser(siteswapString).parse();
        HandSequence handSequence = new HandSequence(handSequenceString);
        double filler = Double.parseDouble(fillerString);

        if (!colorMap.containsKey(colorString.toLowerCase()))
            throw new RuntimeException(String.format("'%s' is not a recognized color", colorString));

        Color propColor = colorMap.get(colorString.toLowerCase());
        float saturation = (float) Double.parseDouble(saturationString);

        if (saturation < 0.0f || saturation > 1.0f)
            throw new RuntimeException("saturation values must be within [0,1]");

        //Get the beatmap ready for conversion!!
        beatmap.applyModifier(modifierString);
        beatmap.applyStackLayers();

        List<Event> convertedHitObjects = ConversionFunctions.convertHitObjects(
                beatmap.hitObjects,
                siteswap,
                handSequence,
                filler
        );

        int numOfPaths = SiteswapFunctions.averageBeat(siteswap);
        JMLDocument testJML = new JMLDocument();
        testJML.setPaths(numOfPaths);
        testJML.setJugglers(1);
        testJML.setDelay(convertedHitObjects.getLast().t + ConversionFunctions.FRAME_DISTANCE_SECONDS);
        testJML.addEvents(convertedHitObjects);
        testJML.setPPerm(
                String.join(
                        "",
                        IntStream.rangeClosed(1, numOfPaths)
                                .mapToObj(i -> String.format("(%d)", i))
                                .toList()
                )
        );

        double propDiameter = 10 * Beatmap.hitObjectRadius(beatmap.circleSize) / Beatmap.hitObjectRadius(6);
        for (int i = 1; i <= numOfPaths; i++) {
            if (propColor == rainbowColor)
                testJML.assignProp(i, Color.getHSBColor((1.0f / numOfPaths * i), 1.0f, 1.0f), propDiameter);
            else
                testJML.assignProp(i, Color.WHITE, propDiameter);
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        writer.write(testJML.toString());
        writer.flush();

        System.out.printf("successfully flushed to output file \"%s\"\n", outputFile.getAbsolutePath());
    }
}
