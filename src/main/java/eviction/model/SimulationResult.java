package eviction.model;

import java.util.List;

public record SimulationResult(List<SimStep> steps, int hits, int misses, double hitRate) {}
