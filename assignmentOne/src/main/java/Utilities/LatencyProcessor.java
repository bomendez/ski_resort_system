package Utilities;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class LatencyProcessor {
    public static final String delimiter = ",";
    private static DescriptiveStatistics stats = new DescriptiveStatistics();
    private static List<Integer> latencyList = new ArrayList<>();
    private static int numRequests = 0;
    private static int totalLatency = 0;
    private static long throughput;

    private static String readFile(String path) {
        String content = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String[] tempArr;
            String line = " ";
            while ((line = reader.readLine()) != null) {
                tempArr = line.split(delimiter);
                numRequests = tempArr.length;
                System.out.println(numRequests);
                for (int i=0; i<tempArr.length; i++) {
                    if ((i % 4) == 2) {
                        String latencyDataPoint = tempArr[i].replaceAll("\"", "");
                        int latencyInt;
                        try {
                            latencyInt = (int) Integer.valueOf(latencyDataPoint);
                            stats.addValue(latencyInt);
                            totalLatency += latencyInt;
                        } catch (NumberFormatException e) {
                        }
                    }
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.trim();
    }

    public static void main(String[] args) {
        System.out.println("Digesting data. Please sit tight...");
        String file = readFile("/Users/bomendez/Documents/CS6650/assignment1/ClientPart2.csv");
        System.out.println("File read. Analyzing data...");
        System.out.println("numRequests: " + numRequests);
        System.out.println("Mean value: "+stats.getMean());
        System.out.println("Median value: "+stats.getPercentile(50));
        throughput = numRequests/totalLatency;
        System.out.println("Throughput: " + throughput);
        System.out.println("99th Percentile:  "+stats.getPercentile(99));
        System.out.println("Min Value: " + stats.getMin());
        System.out.println("Max Value: " + stats.getMax());
    }
}
