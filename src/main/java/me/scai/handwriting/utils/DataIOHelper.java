package me.scai.handwriting.utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class DataIOHelper {
    public static void printFloatDataToCsvFile(List<float[]> data, File f) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(f);
            for (float[] dataRow : data) {
                for (int i = 0; i < dataRow.length; ++i) {
                    pw.printf("%.9f", dataRow[i]);
                    if (i < dataRow.length - 1) {
                        pw.print(",");
                    }
                }
                pw.print("\n");
            }

        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            pw.close();
        }
    }

    public static void printLabelsDataToOneHotCsvFile(List<Integer> labels, int nLabels, File f) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(f);
            for (int label : labels) {
                for (int i = 0; i < nLabels; ++i) {
                    pw.print(i == label ? "1" : "0");

                    if (i < nLabels - 1) {
                        pw.print(",");
                    }
                }

                pw.print("\n");
            }

        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            pw.close();
        }
    }
}
