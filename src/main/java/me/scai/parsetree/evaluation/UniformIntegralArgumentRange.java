package me.scai.parsetree.evaluation;

import java.util.ArrayList;
import java.util.List;

public class UniformIntegralArgumentRange implements ArgumentRange {
    /* Member variables */
    private double lowerBound;
    private double upperBound;
    private int numIntervals;

    private double interval;
    private ArrayList<Double> values;

    /* Constructor*/
    public UniformIntegralArgumentRange(double lowerBound, double upperBound, int numIntervals) {
        if (numIntervals <= 0) {
            throw new IllegalArgumentException("Negative or zero value in nIntervals");
        }

        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.numIntervals = numIntervals;

        values = new ArrayList<>();
        values.ensureCapacity(numIntervals);

        interval = (upperBound - lowerBound) / (double) numIntervals;
        double x0 = lowerBound;
        values.add(x0);

        for (int i = 1; i < this.numIntervals; ++i) {
            values.add(values.get(i - 1) + interval);
        }
    }

    /* Methods */

    /**
     * This returns the lower values of the trapezoids
     * @return
     */
    @Override
    public List<Double> getValues() {
        return values;
    }

    public int getNumInterval() {
        return numIntervals;
    }

    public double getInterval() {
        return interval;
    }

    public double getLowerBound() {
        return lowerBound;
    }
}
