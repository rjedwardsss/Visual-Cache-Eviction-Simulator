package eviction.core;

import eviction.model.Policy;
import eviction.model.SimStep;
import eviction.model.SimulationResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

public final class EvictionSimulator {
  private EvictionSimulator() {}

  public static SimulationResult run(Policy policy, int capacity, List<Integer> keys, long seed) {
    if (capacity < 1) {
      throw new IllegalArgumentException("capacity must be >= 1");
    }
    return switch (policy) {
      case FIFO -> runFifo(capacity, keys);
      case CLOCK -> runClock(capacity, keys);
      case RANDOM -> runRandom(capacity, keys, seed);
      case BELADY -> runBelady(capacity, keys);
    };
  }

  private static SimulationResult runFifo(int capacity, List<Integer> keys) {
    Queue<Integer> order = new LinkedList<>();
    Set<Integer> present = new HashSet<>();
    List<SimStep> steps = new ArrayList<>();
    int hits = 0;
    int misses = 0;

    for (int key : keys) {
      Integer evicted = null;
      boolean hit = present.contains(key);
      if (hit) {
        hits++;
      } else {
        misses++;
        if (present.size() >= capacity) {
          evicted = order.remove();
          present.remove(evicted);
        }
        order.add(key);
        present.add(key);
      }
      steps.add(step(key, hit, evicted, new ArrayList<>(order), capacity, hits, misses, "FIFO"));
    }
    return finish(steps, hits, misses);
  }

  private static SimulationResult runClock(int capacity, List<Integer> keys) {
    List<Integer> slots = new ArrayList<>();
    Map<Integer, Boolean> ref = new HashMap<>();
    int hand = 0;
    List<SimStep> steps = new ArrayList<>();
    int hits = 0;
    int misses = 0;

    for (int key : keys) {
      Integer evicted = null;
      boolean hit = slots.contains(key);
      if (hit) {
        hits++;
        ref.put(key, true);
      } else {
        misses++;
        if (slots.size() < capacity) {
          slots.add(key);
          ref.put(key, true);
          hand = slots.size() % Math.max(capacity, 1);
        } else {
          while (true) {
            int victim = slots.get(hand);
            if (Boolean.TRUE.equals(ref.get(victim))) {
              ref.put(victim, false);
              hand = (hand + 1) % capacity;
            } else {
              evicted = victim;
              slots.set(hand, key);
              ref.remove(victim);
              ref.put(key, true);
              hand = (hand + 1) % capacity;
              break;
            }
          }
        }
      }
      steps.add(step(key, hit, evicted, new ArrayList<>(slots), capacity, hits, misses, "CLOCK"));
    }
    return finish(steps, hits, misses);
  }

  private static SimulationResult runRandom(int capacity, List<Integer> keys, long seed) {
    Random rng = new Random(seed);
    List<Integer> slots = new ArrayList<>();
    Set<Integer> present = new HashSet<>();
    List<SimStep> steps = new ArrayList<>();
    int hits = 0;
    int misses = 0;

    for (int key : keys) {
      Integer evicted = null;
      boolean hit = present.contains(key);
      if (hit) {
        hits++;
      } else {
        misses++;
        if (present.size() >= capacity) {
          int idx = rng.nextInt(slots.size());
          evicted = slots.remove(idx);
          present.remove(evicted);
        }
        slots.add(key);
        present.add(key);
      }
      steps.add(step(key, hit, evicted, new ArrayList<>(slots), capacity, hits, misses, "RANDOM"));
    }
    return finish(steps, hits, misses);
  }

  private static SimulationResult runBelady(int capacity, List<Integer> keys) {
    List<Integer> slots = new ArrayList<>();
    Set<Integer> present = new HashSet<>();
    List<SimStep> steps = new ArrayList<>();
    int hits = 0;
    int misses = 0;

    for (int i = 0; i < keys.size(); i++) {
      int key = keys.get(i);
      Integer evicted = null;
      boolean hit = present.contains(key);
      if (hit) {
        hits++;
      } else {
        misses++;
        if (present.size() >= capacity) {
          evicted = farthestFuture(slots, keys, i);
          slots.remove((Integer) evicted);
          present.remove(evicted);
        }
        slots.add(key);
        present.add(key);
      }
      steps.add(step(key, hit, evicted, new ArrayList<>(slots), capacity, hits, misses, "BELADY"));
    }
    return finish(steps, hits, misses);
  }

  private static int farthestFuture(List<Integer> slots, List<Integer> keys, int index) {
    int bestKey = slots.get(0);
    int bestDist = -1;
    for (int candidate : slots) {
      int dist = Integer.MAX_VALUE;
      for (int j = index + 1; j < keys.size(); j++) {
        if (keys.get(j).equals(candidate)) {
          dist = j - index;
          break;
        }
      }
      if (dist > bestDist) {
        bestDist = dist;
        bestKey = candidate;
      }
    }
    return bestKey;
  }

  private static SimStep step(
      int key,
      boolean hit,
      Integer evicted,
      List<Integer> slots,
      int capacity,
      int hits,
      int misses,
      String policy) {
    return new SimStep(
        key,
        hit,
        evicted,
        pad(slots, capacity),
        hits,
        misses,
        hit ? policy + " hit" : policy + " miss");
  }

  private static List<Integer> pad(List<Integer> slots, int capacity) {
    List<Integer> out = new ArrayList<>(slots);
    while (out.size() < capacity) {
      out.add(null);
    }
    return Collections.unmodifiableList(out);
  }

  private static SimulationResult finish(List<SimStep> steps, int hits, int misses) {
    int total = hits + misses;
    double rate = total == 0 ? 0.0 : (double) hits / total;
    return new SimulationResult(List.copyOf(steps), hits, misses, rate);
  }
}
