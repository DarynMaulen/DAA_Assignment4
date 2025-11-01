package metrics;

import java.util.concurrent.ConcurrentHashMap;

// SimpleMetrics class: Implementation of the Metrics interface using a concurrent map for counters and basic timer logic.
public class SimpleMetrics implements Metrics {
    // Map to store counters and metrics.
    private final ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<>();
    // Start time marker in nanoseconds.
    private long start = 0L;
    // Last measured elapsed time.
    private long lastElapsed = 0L;

    @Override
    public void startTimer() {
        start = System.nanoTime();
    }

    @Override
    public void stopTimer() {
        lastElapsed = System.nanoTime() - start;
    }

    @Override
    public long getElapsedNanos() {
        return lastElapsed;
    }

    @Override
    public void inc(String counter) {
        map.merge(counter, 1L, Long::sum);
    }

    @Override
    public void incBy(String counter, long delta) {
        map.merge(counter, delta, Long::sum);
    }

    @Override
    public void putLong(String key, long value) {
        map.put(key, value);
    }

    @Override
    public long get(String key) {
        return map.getOrDefault(key, 0L);
    }
}
