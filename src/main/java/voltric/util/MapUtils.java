package voltric.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TODO: Temporary until Apache Commons or similar is added for better collections
 */
public class MapUtils {

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByDescendingValue(Map<K, V> map) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByAscendingValue(Map<K, V> map) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(/*Collections.reverseOrder()*/))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
}
