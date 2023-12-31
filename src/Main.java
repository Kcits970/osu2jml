import jml.*;
import jml.siteswap.*;
import osu.Beatmap;
import osu.BeatmapFunctions;

import java.awt.Color;
import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

public class Main {
    //List of recognized options: 's'ite's'wap, beatmap 'm'odifier, 'h'and sequence, 'f'iller duration, prop 'c'olor, prop color 's'aturation, 'i'nvert color.
    static final List<String> recognizedArguments = List.of("-ss", "-m", "-h", "-f", "-c", "-s", "-i");
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

        //Checking mandatory arguments. (The first 2 arguments should specify input and output file paths.)
        if (args.length < 2)
            throw new RuntimeException("missing input source/output destination argument");

        //Parsing optional arguments. (Each argument takes exactly one parameter. (only if it exists))
        Map<String,String> optionalArguments = new HashMap<>();

        for (int i = 2; i < args.length; i++) {
            String argument;
            String parameter;

            if (args[i].contains("=")) {
                argument = args[i].substring(0, args[i].indexOf('='));
                parameter = args[i].substring(args[i].indexOf('=') + 1);
            } else {
                if (i+1 >= args.length)
                    throw new RuntimeException(String.format("no parameters provided for argument '%s'", args[i]));
                argument = args[i];
                parameter = args[++i];
            }

            if (recognizedArguments.contains(argument))
                optionalArguments.put(argument, parameter);
            else
                throw new RuntimeException(String.format("'%s' is not a recognized argument", args[i]));
        }

        //Required variables for beatmap conversion. (The parameters for '-m', '-ss', '-h', 'i' get validated here.)
        Beatmap beatmap = new Beatmap(new File(args[0]));
        beatmap.applyModifier(optionalArguments.getOrDefault("-m", ""));
        beatmap.applyStackLayers();
        File outputFile = new File(args[1]);
        List<List<Integer>> siteswap = SiteswapParser.parse(optionalArguments.getOrDefault("-ss", "3"));
        HandSequence handSequence = new HandSequence(optionalArguments.getOrDefault("-h", "LR"));
        double filler = Double.parseDouble(optionalArguments.getOrDefault("-f", "1.0"));
        Color propColor = colorMap.get(optionalArguments.getOrDefault("-c", "white"));
        float saturation = (float) Double.parseDouble(optionalArguments.getOrDefault("-s", "1.0"));

        String invertString = optionalArguments.getOrDefault("-i", "false");
        if (!"true".equalsIgnoreCase(invertString) && !"false".equalsIgnoreCase(invertString))
            throw new RuntimeException("the '-i' option only accepts \"true\" or \"false\"");

        boolean invertColors = "true".equalsIgnoreCase(invertString);

        if (outputFile.exists() && outputFile.isFile()) {
            System.out.printf("specified output location \"%s\" already exists, continue? (Y/N)\n", outputFile.getAbsolutePath());
            System.out.print(">> ");

            String input = scanner.nextLine();

            if ("N".equalsIgnoreCase(input))
                return;
            else if (!"Y".equalsIgnoreCase(input))
                throw new RuntimeException(String.format("'%s' is not a recognized command here", input));
        }

        //Validation of remaining optional parameters.
        if (filler < 0)
            throw new RuntimeException("filler duration cannot be negative");

        if (propColor == null)
            throw new RuntimeException(String.format("'%s' is not a recognized color", optionalArguments.get("-c")));

        if (saturation < 0.0f || saturation > 1.0f)
            throw new RuntimeException("saturation values must be within [0,1]");

        if (propColor != rainbowColor) {
            float[] hsb = Color.RGBtoHSB(propColor.getRed(), propColor.getGreen(), propColor.getBlue(), null);
            propColor = Color.getHSBColor(hsb[0], hsb[1] == 0.0f ? 0.0f : saturation, hsb[2]);
        }

        //Construction of the full JML.
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

        Function<Color,Color> colorInverter = color -> new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue());

        double propDiameter = 10 * BeatmapFunctions.hitObjectRadius(beatmap.circleSize) / BeatmapFunctions.hitObjectRadius(5.5);
        for (int i = 1; i <= numOfPaths; i++) {
            Color colorToAssign = propColor == rainbowColor ?
                    Color.getHSBColor((1.0f / numOfPaths * i), saturation, 1.0f) :
                    propColor;
            if (invertColors)
                colorToAssign = colorInverter.apply(colorToAssign);


            testJML.assignPathToProp(i, new Prop(colorToAssign, propDiameter));
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        writer.write(testJML.toString());
        writer.flush();

        System.out.printf("successfully flushed to output file \"%s\"\n", outputFile.getAbsolutePath());
    }
}
