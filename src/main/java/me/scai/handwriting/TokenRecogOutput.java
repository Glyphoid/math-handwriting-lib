package me.scai.handwriting;

import java.util.List;

public class TokenRecogOutput {
    private String winner;
    private float maxP;

    private List<String> candidateNames;
    private List<Float> candidatePs;

    public TokenRecogOutput(String winner, float maxP, List<String> candidateNames, List<Float> candiatePs) {
        this.winner = winner;
        this.maxP = maxP;
        this.candidateNames = candidateNames;
        this.candidatePs = candiatePs;
    }

    public String getWinner() {
        return winner;
    }

    public float getMaxP() {
        return maxP;
    }

    public List<String> getCandidateNames() {
        return candidateNames;
    }

    public List<Float> getCandidatePs() {
        return candidatePs;
    }

    public double[] getCandidatePsAsDoubleArray() {
        double[] ps = new double[candidatePs.size()];

        for (int i = 0; i < candidatePs.size(); ++i) {
            ps[i] = (double) candidatePs.get(i);
        }

        return ps;
    }

    public float[] getCandidatePsAsFloatArray() {
        float[] ps = new float[candidatePs.size()];

        for (int i = 0; i < candidatePs.size(); ++i) {
            ps[i] = (float) candidatePs.get(i);
        }

        return ps;
    }
}
