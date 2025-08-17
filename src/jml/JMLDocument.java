package jml;

import java.util.*;
import java.util.stream.IntStream;

public class JMLDocument {
    int jugglers, paths;
    Map<Integer,Prop> pathAssignments;
    double delay;
    String pperm;

    List<Event> events;

    public JMLDocument() {
        pathAssignments = new HashMap<>();
        events = new ArrayList<>();
    }

    public void setJugglers(int jugglers) {
        this.jugglers = jugglers;
    }

    public void setPaths(int paths) {
        this.paths = paths;
    }

    public void setPPerm(String pperm) {
        this.pperm = pperm;
    }

    public void setDelay(double delay) {
        this.delay = delay;
    }

    public void assignPathToProp(int path, Prop prop) {
        pathAssignments.put(path, prop);
    }

    public void addEvents(Collection<? extends Event> eventsToAdd) {
        events.addAll(eventsToAdd);
    }

    @Override
    public String toString() {
        StringBuilder jmlBuilder = new StringBuilder();

        jmlBuilder.append("<?xml version=\"1.0\"?>\n");
        jmlBuilder.append("<!DOCTYPE jml SYSTEM \"file://jml.dtd\">\n");
        jmlBuilder.append("<jml>\n");
        jmlBuilder.append("<pattern>\n");

        for (int i = 1; i <= paths; i++)
            jmlBuilder.append(
                    String.format("<prop type=\"ball\" mod=\"color=%d,%d,%d;diam=%.4f\"/>\n",
                            pathAssignments.get(i).color.getRed(),
                            pathAssignments.get(i).color.getGreen(),
                            pathAssignments.get(i).color.getBlue(),
                            pathAssignments.get(i).diameter
                    )
            );

        jmlBuilder.append(String.format("<setup jugglers=\"%d\" paths=\"%d\" props=\"%s\"/>\n",
                jugglers,
                paths,
                String.join(",", IntStream.rangeClosed(1, paths).mapToObj(String::valueOf).toList())
        ));

        jmlBuilder.append(String.format("<symmetry type=\"delay\" pperm=\"%s\" delay=\"%.4f\"/>\n", pperm, delay));

        for (Event e : events)
            jmlBuilder.append(e.toString()).append('\n');

        jmlBuilder.append("</pattern>\n");
        jmlBuilder.append("</jml>");

        return jmlBuilder.toString();
    }
}
