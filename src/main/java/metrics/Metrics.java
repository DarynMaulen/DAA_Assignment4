package metrics;

import java.util.Map;

// Interface Metrics: Defines methods for timing execution and collecting counters long-value metrics.
public interface Metrics {
    void startTimer();
    void stopTimer();
    long getElapsedNanos();
    void inc(String counter);
    void incBy(String counter, long delta);
    void putLong(String key, long value);
    long get(String key);
}
