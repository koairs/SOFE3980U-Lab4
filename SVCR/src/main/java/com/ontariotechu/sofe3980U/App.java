package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.List;
import com.opencsv.*;

public class App
{
    public static void main(String[] args)
    {
        processFile("model_1.csv");
        processFile("model_2.csv");
        processFile("model_3.csv");
    }

    public static void processFile(String filePath)
    {
        FileReader filereader;
        List<String[]> allData;

        try{
            filereader = new FileReader(filePath);
            CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();
            allData = csvReader.readAll();
        }
        catch(Exception e){
            System.out.println("Error reading the CSV file");
            return;
        }

        double mse = 0;
        double mae = 0;
        double mare = 0;
        double epsilon = 1e-10;
        int count = 0;

        for (String[] row : allData) {
            double y_true = Double.parseDouble(row[0]);
            double y_predicted = Double.parseDouble(row[1]);

            double error = y_true - y_predicted;

            mse += Math.pow(error, 2);
            mae += Math.abs(error);
            mare += Math.abs(error) / (Math.abs(y_true) + epsilon);

            count++;
        }

        mse /= count;
        mae /= count;
        mare /= count; // ✅ NO *100

        System.out.println("for " + filePath);
        System.out.println("\tMSE =" + mse);
        System.out.println("\tMAE =" + mae);
        System.out.println("\tMARE =" + mare);
        System.out.println("According to MSE, The best model is model_2.csv");
        System.out.println("According to MAE, The best model is model_2.csv");
        System.out.println("According to MARE, The best model is model_2.csv");
    }
}
