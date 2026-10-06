# Visual Cache & Eviction Simulator

A **Java console** simulator for classic **cache eviction policies** with step-by-step slot traces: **FIFO**, **CLOCK**, **RANDOM**, and **Belady's optimal**. Fully associative cache with fixed capacity. No external libraries.

Interactive TypeScript demo: [rjedwards.dev/projects/cache-eviction](https://rjedwards.dev/projects/cache-eviction).

This is separate from the LRU/LFU [Cache Simulator](https://github.com/rjedwardsss/Cache-Simulator).

## Policies

| Policy | Evicts |
|--------|--------|
| **FIFO** | Oldest inserted key |
| **CLOCK** | Second-chance / reference-bit hand |
| **RANDOM** | Uniform random resident (seeded) |
| **BELADY** | Key whose next use is farthest in the future |

## Requirements

- JDK 17+

## Compile & run

```bash
javac -d out -sourcepath src/main/java src/main/java/eviction/app/EvictionSimulatorApp.java
java -cp out eviction.app.EvictionSimulatorApp
```

Batch mode (no prompts):

```bash
java -cp out eviction.app.EvictionSimulatorApp FIFO 3 hot-cold
```

## Layout

```
src/main/java/eviction/
  model/   Policy, steps, presets
  core/    EvictionSimulator
  app/     Console UI
```
