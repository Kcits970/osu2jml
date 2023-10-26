package list;

import java.util.List;

public class Lists {
    public static <E> boolean isIndexWithinBounds(List<E> list, int index) {
        return index >= 0 && index < list.size();
    }

    public static <E> E lastElement(List<E> list) {
        return isIndexWithinBounds(list, list.size()-1) ? list.get(list.size()-1) : null;
    }

    public static <E> E nextElement(List<E> list, int index) {
        return isIndexWithinBounds(list, index+1) ? list.get(index+1) : null;
    }

    public static <E> E previousElement(List<E> list, int index) {
        return isIndexWithinBounds(list, index-1) ? list.get(index-1) : null;
    }
}
