package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.*;
import com.opencsv.*;

public class App {

	public static double[] evaluateModel(String filePath) {
		List<String[]> allData;

		try {
			FileReader filereader = new FileReader(filePath);
			CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();
			allData = csvReader.readAll();
		} catch (Exception e) {
			System.out.println("Error reading: " + filePath);
			return null;
		}

		int TP = 0, TN = 0, FP = 0, FN = 0;
		double bce = 0;

		List<double[]> rocPoints = new ArrayList<>();

		for (String[] row : allData) {
			int y_true = Integer.parseInt(row[0]);
			double y_pred = Double.parseDouble(row[1]);

			// BCE using log base 2
			double epsilon = 1e-15;
			double y_clamped = Math.max(epsilon, Math.min(1 - epsilon, y_pred));
			bce += -(y_true * (Math.log(y_clamped) / Math.log(2))
					+ (1 - y_true) * (Math.log(1 - y_clamped) / Math.log(2)));

			// Convert to class
			int y_hat = (y_pred >= 0.5) ? 1 : 0;

			if (y_hat == 1 && y_true == 1) TP++;
			else if (y_hat == 0 && y_true == 0) TN++;
			else if (y_hat == 1 && y_true == 0) FP++;
			else if (y_hat == 0 && y_true == 1) FN++;

			rocPoints.add(new double[]{y_pred, y_true});
		}

		int total = TP + TN + FP + FN;

		double accuracy  = (double)(TP + TN) / total;
		double precision = (TP + FP == 0) ? 0 : (double) TP / (TP + FP);
		double recall    = (TP + FN == 0) ? 0 : (double) TP / (TP + FN);
		double f1        = (precision + recall == 0) ? 0 : 2 * precision * recall / (precision + recall);
		bce /= total;

		// AUC Calculation (trapezoidal rule)
		rocPoints.sort((a, b) -> Double.compare(b[0], a[0]));

		int P = 0, N = 0;
		for (double[] p : rocPoints) {
			if (p[1] == 1) P++;
			else N++;
		}

		double auc = 0;
		int tp = 0, fp = 0;
		double prevTPR = 0, prevFPR = 0;

		for (double[] p : rocPoints) {
			if (p[1] == 1) tp++;
			else fp++;

			double TPR = (double) tp / P;
			double FPR = (double) fp / N;

			auc += (FPR - prevFPR) * (TPR + prevTPR) / 2;

			prevTPR = TPR;
			prevFPR = FPR;
		}

		// OUTPUT
		System.out.println("for " + filePath);
		System.out.printf("\tBCE =%.7f%n", bce);
		System.out.println("\tConfusion matrix");
		System.out.println("\t\t\t\ty=1      y=0");
		System.out.println("\t\ty^=1\t" + TP + "\t" + FP);
		System.out.println("\t\ty^=0\t" + FN + "\t" + TN);
		System.out.printf("\tAccuracy =%.4f%n", accuracy);
		System.out.printf("\tPrecision =%.7f%n", precision);  // changed from 8
		System.out.printf("\tRecall =%.8f%n", recall);
		System.out.printf("\tf1 score =%.7f%n", f1);
		System.out.printf("\tauc roc =%.8f%n", auc);

		return new double[]{bce, accuracy, precision, recall, f1, auc};
	}

	public static void main(String[] args) {
		String[] models = {"model_1.csv", "model_2.csv", "model_3.csv"};
		String[] metricNames = {"BCE", "Accuracy", "Precision", "Recall", "F1 score", "AUC ROC"};
		// For BCE, lower is better; for all others, higher is better
		boolean[] lowerIsBetter = {true, false, false, false, false, false};

		double[][] results = new double[models.length][];

		for (int i = 0; i < models.length; i++) {
			results[i] = evaluateModel(models[i]);
		}

		System.out.println();

		// For each metric, find and print the best model
		for (int m = 0; m < metricNames.length; m++) {
			int bestIdx = 0;
			for (int i = 1; i < models.length; i++) {
				if (results[i] == null) continue;
				boolean isBetter = lowerIsBetter[m]
						? results[i][m] < results[bestIdx][m]
						: results[i][m] > results[bestIdx][m];
				if (isBetter) bestIdx = i;
			}
			System.out.println("According to " + metricNames[m] + ", The best model is " + models[bestIdx]);
		}
	}
}
