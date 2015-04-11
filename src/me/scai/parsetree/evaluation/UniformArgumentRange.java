package me.scai.parsetree.evaluation;

import java.util.ArrayList;
import java.util.List;

class UniformArgumentRange implements ArgumentRange {
	/* Member variables */
	double minVal;
	double maxVal;
	double interval;

	/* Constructor */
	public UniformArgumentRange(double tMinVal, double tInterval, double tMaxVal) {
		this.minVal = tMinVal;
		this.interval = tInterval;
		this.maxVal = tMaxVal;
	}

	/* Methods */
	@Override
	public List<Double> getValues() {
		ArrayList<Double> vs = new ArrayList<Double>();

		/* Edge cases */
		if ((maxVal - minVal) * interval < 0.0) {
			return vs;
		}
		if (interval == 0.0 && maxVal - minVal != 0.0) {
			return vs;
		}

		int estimLength = (int) ((maxVal - minVal) / interval) + 1;
		vs.ensureCapacity(estimLength);

		double v = minVal;
		while ((interval > 0.0) && v <= maxVal || (interval < 0.0)
				&& v >= maxVal) {
			vs.add(v);
		}

		return vs;
	}
}
