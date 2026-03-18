package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.List;
import com.opencsv.*;

public class App
{
	public static void main(String[] args)
	{
		String filePath = "model.csv";
		FileReader filereader;
		List<String[]> allData;
		try {
			filereader = new FileReader(filePath);
			CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();
			allData = csvReader.readAll();
		} catch(Exception e){
			System.out.println("Error reading the CSV file");
			return;
		}

		int numClasses = 5;
		double crossEntropy = 0.0;
		int[][] confusionMatrix = new int[numClasses][numClasses]; // rows = predicted, cols = actual
		int totalSamples = allData.size();

		for (String[] row : allData) {
			int y_true = Integer.parseInt(row[0]); // actual class (1-5)
			float[] y_pred = new float[numClasses];
			for(int i = 0; i < numClasses; i++){
				y_pred[i] = Float.parseFloat(row[i + 1]);
			}

			// --- Cross-Entropy ---
			// y_true ranges 1..5, array index 0..4
			int trueIndex = y_true - 1;
			// To avoid log(0), clip prediction to [1e-15, 1]
			double p = Math.max(1e-15, y_pred[trueIndex]);
			crossEntropy -= Math.log(p);

			// --- Confusion Matrix ---
			// Predicted class = index of max probability
			int predictedIndex = 0;
			float maxProb = y_pred[0];
			for(int i = 1; i < numClasses; i++){
				if(y_pred[i] > maxProb){
					maxProb = y_pred[i];
					predictedIndex = i;
				}
			}
			confusionMatrix[predictedIndex][trueIndex]++;
		}

		// Compute average CE
		crossEntropy /= totalSamples;

		// --- Print Results ---
		System.out.printf("CE = %.7f\n", crossEntropy);
		System.out.println("Confusion matrix");
		System.out.print("\t\t");
		for(int i = 1; i <= numClasses; i++){
			System.out.print("y=" + i + "\t");
		}
		System.out.println();
		for(int i = 0; i < numClasses; i++){
			System.out.print("y^=" + (i+1) + "\t");
			for(int j = 0; j < numClasses; j++){
				System.out.print(confusionMatrix[i][j] + "\t");
			}
			System.out.println();
		}
	}
}
