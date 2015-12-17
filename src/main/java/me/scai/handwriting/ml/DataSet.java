package me.scai.handwriting.ml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataSet {
    /* Members */
    private List<float []> X;
    private List<Integer> y;

    private List<String> labelNames;

    /* Constructor */
    public DataSet() {
        X = new ArrayList<>();
        y = new ArrayList<>();
    }

    /* Methods */
    public void addAll(DataSet that) {
        this.X.addAll(that.X);
        this.y.addAll(that.y);
    }

    public void addSample(float[] tX, int ty) {
        X.add(tX);
        y.add(ty);
    }

    public int numSamples() {
        assert(y.size() == X.size());
        return y.size();
    }

    public boolean isEmpty() {
        return numSamples() == 0;
    }

    public List<float []> getX() {
        return X;
    }

    public List<Integer> getY() {
        return y;
    }

    public Set<Integer> getYSet() {
        Set<Integer> ySet = new HashSet<>();

        ySet.addAll(y);

        return ySet;
    }

    public int numUniqueYs() {
        return getYSet().size();
    }

    public List<String> getLabelNames() {
        return labelNames;
    }

    public void setLabelNames(List<String> labelNames) {
        this.labelNames = labelNames;
    }
}
