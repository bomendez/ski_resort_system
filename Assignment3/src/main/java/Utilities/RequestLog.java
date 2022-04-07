package Utilities;

import ClientPart2.ApiPerformance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RequestLog {
    public static int numSuccessfulRequests;
    public static int numUnsuccessfulRequests;
    public static int numRequests;
    public static List<ApiPerformance> apiPerformanceList = Collections.synchronizedList(new ArrayList<>());

    public RequestLog() {}

    public static synchronized int getNumSuccessfulRequests() {return numSuccessfulRequests;}

    public static synchronized int getNumUnsuccessfulRequests() {return numUnsuccessfulRequests;}

    public static synchronized int getNumRequests() {return numRequests;}

    public static List<ApiPerformance> getApiPerformanceList() {return apiPerformanceList;}

    public static synchronized void logSuccess() {numSuccessfulRequests++;}

    public static synchronized void addNumSuccessfulRequests(int requests) {numSuccessfulRequests += requests;}

    public static synchronized void logFailure() {numUnsuccessfulRequests++;}

    public static synchronized void addNumUnsuccessfulRequests(int requests) {numUnsuccessfulRequests += requests;}

    public static synchronized void incRequestCount() {numRequests++;}

    public static synchronized void addRequestCount(int requests) {numRequests += requests;}

    public static synchronized void addApiPerformance(ApiPerformance thread) {
        apiPerformanceList.add(thread);
    }

    public static synchronized void joinApiPerformanceLists(List<ApiPerformance> incomingApiList) {
        apiPerformanceList.addAll(incomingApiList);
    }
}
