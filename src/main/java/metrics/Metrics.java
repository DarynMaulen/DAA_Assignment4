package metrics;

import java.util.Map;

public interface Metrics {
    void startTimer();
    void stopTimer();
    long getElapsedNanos();
    void inc(String counter);
    void incBy(String counter, long delta);
    void putLong(String key, long value);
    long get(String key);
    Map<String, Long> allCounters();
}
