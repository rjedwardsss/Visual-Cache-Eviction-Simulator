package eviction.app;

import eviction.core.EvictionSimulator;
import eviction.model.Policy;
import eviction.model.SimStep;
import eviction.model.SimulationResult;
import eviction.model.TracePresets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public final class EvictionSimulatorApp {
  private EvictionSimulatorApp() {}

  public static void main(String[] args) {
    Policy policy = Policy.FIFO;
    int capacity = 3;
    List<Integer> keys = TracePresets.get("hot-cold");
    long seed = 42L;

    if (args.length >= 1 && !args[0].isBlank()) {
      policy = Policy.valueOf(args[0].trim().toUpperCase(Locale.ROOT));
    }
    if (args.length >= 2 && !args[1].isBlank()) {
      capacity = Integer.parseInt(args[1].trim());
    }
    if (args.length >= 3 && !args[2].isBlank()) {
      if (TracePresets.PRESETS.containsKey(args[2].trim())) {
        keys = TracePresets.get(args[2].trim());
      } else {
        keys = parseKeys(args[2]);
      }
    }

    if (args.length == 0) {
      try (Scanner scanner = new Scanner(System.in)) {
        System.out.println("Visual Cache & Eviction Simulator");
        System.out.println("Policies: FIFO, CLOCK, RANDOM, BELADY");
        System.out.print("Policy [FIFO]: ");
        String policyRaw = scanner.nextLine().trim();
        if (!policyRaw.isEmpty()) {
          policy = Policy.valueOf(policyRaw.toUpperCase(Locale.ROOT));
        }

        System.out.print("Capacity [3]: ");
        String capRaw = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
        if (!capRaw.isEmpty()) {
          capacity = Integer.parseInt(capRaw);
        }

        System.out.println("Presets: " + String.join(", ", TracePresets.PRESETS.keySet()));
        System.out.print("Preset or custom keys [hot-cold]: ");
        String traceRaw = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
        if (!traceRaw.isEmpty()) {
          if (TracePresets.PRESETS.containsKey(traceRaw)) {
            keys = TracePresets.get(traceRaw);
          } else {
            keys = parseKeys(traceRaw);
          }
        }
      }
    } else {
      System.out.println("Visual Cache & Eviction Simulator (batch)");
    }

    SimulationResult result = EvictionSimulator.run(policy, capacity, keys, seed);
    System.out.printf(
        Locale.ROOT,
        "hits=%d misses=%d hitRate=%.2f%%%n",
        result.hits(),
        result.misses(),
        result.hitRate() * 100.0);
    for (int i = 0; i < result.steps().size(); i++) {
      SimStep step = result.steps().get(i);
      System.out.printf(
          Locale.ROOT,
          "%2d. key=%d %s evicted=%s slots=%s%n",
          i + 1,
          step.key(),
          step.hit() ? "HIT " : "MISS",
          step.evicted() == null ? "-" : step.evicted(),
          formatSlots(step.slots()));
    }
  }

  private static List<Integer> parseKeys(String raw) {
    String[] parts = raw.trim().split("[\\s,]+");
    List<Integer> keys = new ArrayList<>();
    for (String part : parts) {
      if (!part.isEmpty()) {
        keys.add(Integer.parseInt(part));
      }
    }
    if (keys.isEmpty()) {
      throw new IllegalArgumentException("No keys provided");
    }
    return keys;
  }

  private static String formatSlots(List<Integer> slots) {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < slots.size(); i++) {
      if (i > 0) {
        sb.append(' ');
      }
      Integer v = slots.get(i);
      sb.append(v == null ? "_" : v);
    }
    return sb.append(']').toString();
  }
}
