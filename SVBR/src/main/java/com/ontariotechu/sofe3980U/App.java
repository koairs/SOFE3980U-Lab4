package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.*;
import com.opencsv.*;

public class App {

    public static void main(String[] args) throws Exception {
        String[] models = {"model_1.csv", "model_2.csv", "model_3.csv"};

        float bestBCE = Float.MAX_VALUE, bestAcc = -1, bestPrec = -1;
        float bestRec = -1, bestF1 = -1, bestAUC = -1;
        String bestBCEModel="", bestAccModel="", bestPrecModel="";
        String bestRecModel="", bestF1Model="", bestAUCModel="";

        for (String file : models) {
            CSVReader csv = new CSVReaderBuilder(new FileReader(file)).withSkipLines(1).build();
            List<String[]> data = csv.readAll();

            int n = data.size();
            int[] yTrue = new int[n];
            float[] yPred = new float[n];
            for (int i = 0; i < n; i++) {
                yTrue[i] = Integer.parseInt(data.get(i)[0].trim());
                yPred[i] = Float.parseFloat(data.get(i)[1].trim());
            }

            // BCE (flipped formula matching reference implementation)
            float bce = 0f;
            for (int i = 0; i < n; i++) {
                float p = Math.max(Math.min(yPred[i], 1 - 1e-15f), 1e-15f);
                bce += (float)(yTrue[i] * Math.log(1 - p) + (1 - yTrue[i]) * Math.log(p));
            }
            bce = -bce / n;

            // Confusion matrix at threshold = 0.5
            int TP = 0, TN = 0, FP = 0, FN = 0;
            for (int i = 0; i < n; i++) {
                int pred = yPred[i] >= 0.5f ? 1 : 0;
                if      (yTrue[i]==1 && pred==1) TP++;
                else if (yTrue[i]==0 && pred==0) TN++;
                else if (yTrue[i]==0 && pred==1) FP++;
                else                              FN++;
            }

            float accuracy  = (float)(TP + TN) / n;
            float precision = (float) TP / (TP + FP);
            float recall    = (float) TP / (TP + FN);
            float f1        = 2f * accuracy * recall / (accuracy + recall);
            float auc       = computeAUC(yTrue, yPred, n);

            // Print report
            System.out.println("for " + file);
            System.out.printf("\tBCE =%s%n", bce);
            System.out.println("\tConfusion matrix");
            System.out.println("\t\t\t\ty=1\t y=0");
            System.out.printf("\t\ty^=1\t%d\t%d%n", TP, FP);
            System.out.printf("\t\ty^=0\t%d\t%d%n", FN, TN);
            System.out.printf("\tAccuracy =%s%n", accuracy);
            System.out.printf("\tPrecision =%s%n", precision);
            System.out.printf("\tRecall =%s%n", recall);
            System.out.printf("\tf1 score =%s%n", f1);
            System.out.printf("\tauc roc =%s%n", auc);

            // Track best per metric (lower BCE is better, higher for rest)
            if (bce < bestBCE)   { bestBCE = bce;   bestBCEModel = file; }
            if (accuracy > bestAcc)  { bestAcc = accuracy;  bestAccModel = file; }
            if (precision > bestPrec){ bestPrec = precision; bestPrecModel = file; }
            if (recall > bestRec)    { bestRec = recall;     bestRecModel = file; }
            if (f1 > bestF1)         { bestF1 = f1;          bestF1Model = file; }
            if (auc > bestAUC)       { bestAUC = auc;        bestAUCModel = file; }
        }

        System.out.println("According to BCE, The best model is " + bestBCEModel);
        System.out.println("According to Accuracy, The best model is " + bestAccModel);
        System.out.println("According to Precision, The best model is " + bestPrecModel);
        System.out.println("According to Recall, The best model is " + bestRecModel);
        System.out.println("According to F1 score, The best model is " + bestF1Model);
        System.out.println("According to AUC ROC, The best model is " + bestAUCModel);
    }

    static float computeAUC(int[] yTrue, float[] yPred, int n) {
        // Collect unique thresholds sorted descending
        List<Float> thresholds = new ArrayList<>();
        for (float p : yPred)
            if (!thresholds.contains(p)) thresholds.add(p);
        thresholds.sort(Collections.reverseOrder());

        int totalPos = 0, totalNeg = 0;
        for (int y : yTrue) { if (y == 1) totalPos++; else totalNeg++; }

        float auc = 0f, prevFPR = 0f, prevTPR = 0f;
        for (float t : thresholds) {
            int tp = 0, fp = 0;
            for (int i = 0; i < n; i++) {
                if (yPred[i] >= t) { if (yTrue[i]==1) tp++; else fp++; }
            }
            float tpr = (float) tp / totalPos;
            float fpr = (float) fp / totalNeg;
            auc += (fpr - prevFPR) * (tpr + prevTPR) / 2f;
            prevFPR = fpr; prevTPR = tpr;
        }
        auc += (1f - prevFPR) * (1f + prevTPR) / 2f;
        return Math.abs(auc);
    }
}
