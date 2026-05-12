// src/Metrics.java
import java.util.*;

public class Metrics {
    public static long bfComparisons = 0;
    public static long lshCandidatePairs = 0;
    public static long lshEmittedPairs = 0;

    public static void reset() {
        bfComparisons = 0;
        lshCandidatePairs = 0;
        lshEmittedPairs = 0;
    }
}