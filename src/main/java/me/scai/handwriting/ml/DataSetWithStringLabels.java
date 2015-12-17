package me.scai.handwriting.ml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataSetWithStringLabels {
    /* Members */
    private List<float []> X;
    private List<String> y;

    private List<String> labelNames;

    /* Constructor */
    public DataSetWithStringLabels() {
        X = new ArrayList<>();
        y = new ArrayList<>();
    }

    /* Methods */
    public void addAll(DataSetWithStringLabels that) {
        this.X.addAll(that.X);
        this.y.addAll(that.y);
    }

    public void addSample(float[] tX, String ty) {
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

    public List<String> getY() {
        return y;
    }

    public Set<String> getYSet() {
        Set<String> ySet = new HashSet<>();

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
