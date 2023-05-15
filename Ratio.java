import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Ratio {

    public static Map<String, Double> calculateCallsSuccessRatioByMonth(String filePath) {
        Map<String, Integer> callsCountByMonth = new HashMap<>();
        Map<String, Integer> successCountByMonth = new HashMap<>();
        Map<String, Double> successRatioByMonth = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 3) {
                    String month = data[0].trim();
                    int year = Integer.parseInt(data[1].trim());
                    boolean isSuccess = Boolean.parseBoolean(data[2].trim());

                    String monthYearKey = month + "-" + year;

                    // Update penalty count by month
                    callsCountByMonth.put(monthYearKey, callsCountByMonth.getOrDefault(monthYearKey, 0) + 1);

                    // Update success count by month if penalty is successful
                    if (isSuccess) {
                        successCountByMonth.put(monthYearKey, successCountByMonth.getOrDefault(monthYearKey, 0) + 1);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Calculate success ratio by month
        for (String monthYearKey : callsCountByMonth.keySet()) {
            int penaltyCount = callsCountByMonth.getOrDefault(monthYearKey, 0);
            int successCount = successCountByMonth.getOrDefault(monthYearKey, 0);
            double successRatio = successCount / (double) penaltyCount;

            successRatioByMonth.put(monthYearKey, successRatio);
        }

        return successRatioByMonth;
    }

    public static Map<String, String> calculateTendencies(Map<String, Double> successRatioByMonth) {
        Map<String, String> tendenciesByMonth = new HashMap<>();

        String previousMonthYearKey1 = null;
        String previousMonthYearKey2 = null;
        for (Map.Entry<String, Double> entry : successRatioByMonth.entrySet()) {
            String monthYearKey = entry.getKey();
            double successRatio = entry.getValue();

            String tendency;
            if (previousMonthYearKey1 != null && previousMonthYearKey2 != null) {
                double previousSuccessRatio1 = successRatioByMonth.get(previousMonthYearKey1);
                double previousSuccessRatio2 = successRatioByMonth.get(previousMonthYearKey1);

                if ((successRatio > previousSuccessRatio1 && successRatio > previousSuccessRatio2)
                 || (successRatio > previousSuccessRatio1 && successRatio == previousSuccessRatio2) ||
                 (successRatio > previousSuccessRatio2 && successRatio == previousSuccessRatio1)) {
                    tendency = "+";
                } else if (successRatio < previousSuccessRatio1 || successRatio < previousSuccessRatio2) {
                    tendency = "-";
                } else {
                    tendency = "0";
                }
            } else {
                tendency = "0";
            }

            tendenciesByMonth.put(monthYearKey, tendency);

            previousMonthYearKey2 = previousMonthYearKey1;
            previousMonthYearKey1 = monthYearKey;
        }

        return tendenciesByMonth;
    }
    
    public static Map<String, Double> sortByDate(Map<String, Double> data) {
        List<Map.Entry<String, Double>> entries = new ArrayList<>(data.entrySet());

        // Sort the entries using a custom comparator
        Collections.sort(entries, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> entry1, Map.Entry<String, Double> entry2) {
                String[] dateParts1 = entry1.getKey().split("-");
                String[] dateParts2 = entry2.getKey().split("-");

                // Compare year
                int yearComparison = Integer.compare(Integer.parseInt(dateParts1[0]), Integer.parseInt(dateParts2[0]));
                if (yearComparison != 0) {
                    return yearComparison;
                }

                // Compare month
                int monthComparison = Integer.compare(Integer.parseInt(dateParts1[1]), Integer.parseInt(dateParts2[1]));
                if (monthComparison != 0) {
                    return monthComparison;
                }

                // Dates are equal
                return 0;
            }
        });

        // Create a new LinkedHashMap to preserve the sorted order
        Map<String, Double> sortedData = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : entries) {
            sortedData.put(entry.getKey(), entry.getValue());
        }

        return sortedData;
    }


    public static void main(String[] args) throws IOException {
        String filePath = "log.txt"; // Specify the path to the penalty data file

        // Calculate penalty success ratio by month
        Map<String, Double> successRatioByMonth = calculateCallsSuccessRatioByMonth(filePath);
        successRatioByMonth = sortByDate(successRatioByMonth);
        Map<String,String> tendenciesByMonth = calculateTendencies(successRatioByMonth);

        new FileWriter("ratio.txt", false).close();

        String linea = "";
        BufferedWriter writer = new BufferedWriter(new FileWriter("ratio.txt"));

        // Print the results
        for (Map.Entry<String, Double> entry : successRatioByMonth.entrySet()) {
            String monthYearKey = entry.getKey();
            double successRatio = entry.getValue();
            String tendency = tendenciesByMonth.get(monthYearKey);

            linea = "Año-Mes: " + monthYearKey + ", Ratio: " + successRatio + ", Tendencia: " + tendency + "\n";
            writer.write(linea);
    
        }

        writer.close();
    }
}
