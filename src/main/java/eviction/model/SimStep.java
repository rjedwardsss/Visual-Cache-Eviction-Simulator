package eviction.model;

import java.util.List;

public record SimStep(
    int key,
    boolean hit,
    Integer evicted,
    List<Integer> slots,
    int hits,
    int misses,
    String note
) {}
