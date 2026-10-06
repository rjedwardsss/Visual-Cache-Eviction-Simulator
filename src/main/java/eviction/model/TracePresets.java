package eviction.model;

import java.util.List;
import java.util.Map;

public final class TracePresets {
  private TracePresets() {}

  public static final Map<String, List<Integer>> PRESETS =
      Map.of(
          "hot-cold", List.of(1, 2, 3, 1, 4, 1, 5),
          "scan", List.of(1, 2, 3, 4, 5, 6, 1, 2),
          "loop", List.of(1, 2, 3, 1, 2, 3, 1, 2, 4),
          "belady-trap", List.of(1, 2, 3, 4, 1, 2, 5, 1, 2));

  public static List<Integer> get(String id) {
    List<Integer> keys = PRESETS.get(id);
    if (keys == null) {
      throw new IllegalArgumentException("Unknown preset: " + id);
    }
    return keys;
  }
}
