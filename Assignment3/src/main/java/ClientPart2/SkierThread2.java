package ClientPart2;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import Utilities.RequestLog;

public class SkierThread2 implements Runnable {
    public int threadId;
    public ApiClient apiClient;
    public Integer skierIdBegin;
    public Integer skierIdEnd;
    public Integer startTime;
    public Integer endTime;
    public Integer numThreads;
    public Integer numSkiers;
    public Integer numRuns;
    public double numCalls;
    public Integer numLifts;
    public Integer timeValue;
    public CountDownLatch completed;
    public Integer skierID;
    public Integer waitTime;
    public LiftRide body;
    public SkiersApi apiInstance;
    public RequestLog requestLog;
    public final Integer resortID = 30;
    public final String seasonID = "20";
    public final String dayID = "10";
    public int attempts = 0;
    public final int MAX_RETRY = 5;
    public int numSuccesses = 0;
    public int numFailures = 0;
    public int numRequests = 0;
    public CountDownLatch localGlobalLatch;

    public List<ApiPerformance> apiPerformanceList = new ArrayList<>();
    public List<String> RESORTS = new ArrayList<>();
    public List<String> SEASON = new ArrayList<>();

    public SkierThread2(Integer id, ApiClient client, Integer skierIdStart, Integer skierIdStop,
                       Integer start, Integer end, Integer threadCount, Integer skierCount, Integer runCount,
                       double callCount, Integer liftCount, CountDownLatch latch, RequestLog log, CountDownLatch globalLatch) {
        threadId = id;
        apiClient = client;
        skierIdBegin = skierIdStart;
        skierIdEnd = skierIdStop;
        startTime = start;
        endTime = end;
        numThreads = threadCount;
        numSkiers = skierCount;
        numRuns = runCount;
        numCalls = callCount;
        numLifts = ThreadLocalRandom.current().nextInt(liftCount);
        timeValue = ThreadLocalRandom.current().nextInt(start, end);
        completed = latch;
        requestLog = log;
        localGlobalLatch = globalLatch;
    }

    public void apiCall() {
        System.out.println("api call");
        boolean isSuccessful = false;
        ApiPerformance apiPerformance = new ApiPerformance();
        final long startTime = System.currentTimeMillis();
        while(attempts < MAX_RETRY) {
            try {
                ApiResponse response = apiInstance.writeNewLiftRideWithHttpInfo(body, resortID, seasonID, dayID, skierID);
                if (String.valueOf(response.getStatusCode()).startsWith("2")) {
                    isSuccessful = true;
                    break;
                } else {
                    attempts++;
                }
            } catch (ApiException e) {
                System.err.println("Exception when calling SkiersApi#writeNewLiftRide");
                e.printStackTrace();
            }
        }
        final long endTime = System.currentTimeMillis();
        final long latency = endTime - startTime;
        apiPerformance.setStartTime(startTime);
        apiPerformance.setLatency(latency);
        apiPerformance.setRequestType("POST");
        if (isSuccessful) {
            apiPerformance.setResponseCode(201);
            numSuccesses++;
        } else {
            apiPerformance.setResponseCode(500);
            numFailures++;
        }
        apiPerformanceList.add(apiPerformance);
        System.out.println(apiPerformance);
        System.out.println("Local list length: " + apiPerformanceList.size());
        attempts = 0;
    }

    public void run() {
        skierID = ThreadLocalRandom.current().nextInt(skierIdBegin, skierIdEnd);
        waitTime = Integer.valueOf((int) Math.floor(Math.random()*(endTime - startTime + 1) + startTime));
        body = new LiftRide();
        body.setLiftID(numLifts);
        body.setTime(timeValue);
        body.setWaitTime(waitTime);

        apiInstance = new SkiersApi(apiClient);
        for (int i=0; i < numCalls; i++) {
            numRequests++;
            apiCall();
        }
        requestLog.addNumSuccessfulRequests(numSuccesses);
        requestLog.addNumUnsuccessfulRequests(numFailures);
        requestLog.addRequestCount(numRequests);
        System.out.println("request added: " + numRequests);
        requestLog.joinApiPerformanceLists(apiPerformanceList);
        System.out.println(apiPerformanceList.size());
        completed.countDown();
        localGlobalLatch.countDown();
    }
}
