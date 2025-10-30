package metrics;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleMetrics implements Metrics {
    private final ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<>();
    private long start = 0L;
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

    @Override
    public Map<String, Long> allCounters() {
        return Collections.unmodifiableMap(map);
    }
}
