package ClientPart1;

import Utilities.RequestLog;
import io.swagger.client.ApiClient;

import java.util.concurrent.CountDownLatch;

import static java.lang.Math.floor;

public class MainClient {
    public static int numThreads;
    public static int numSkiers;
    public static int numLifts = 40;
    public static int numRuns = 10;
    public static String ipAddress;
    public final static int startDayPhase1 = 0;
    public final static int endDayPhase1 = 90;
    public final static int startDayPhase2 = 91;
    public final static int endDayPhase2 = 360;
    public final static int startDayPhase3 = 361;
    public final static int endDayPhase3 = 420;
    public final static int THROUGHPUT = 5;
    public static long throughputFinal;

    /**
     * sets numThreads to threads / 4
     * @param threads the number of threads
     *               must be less than 1024
     */
    private static void setNumThreads(int threads) {
        if (threads < 0 || threads > 1024) {throw new IllegalArgumentException("Max threads exceeded 1024");}
        numThreads = threads;
    }


    private static void setNumSkiers(int skiers) {
        if (skiers < 0 || skiers > 100000) {throw new IllegalArgumentException("Max skiers exceeded 100000");}
        numSkiers = skiers;
    }

    private static void setSkiLifts(int skiLifts) {
        if (skiLifts < 5 || skiLifts > 60) {throw new IllegalArgumentException("Max ski lifts not between 5 and 60");}
        numLifts = skiLifts;
    }

    private static void setNumRuns(int runs) {
        if (runs < 0 || runs > 20) {throw new IllegalArgumentException("Max runs exceeded 20");}
        numRuns = runs;
    }

    private static void setIpAddress(String ip) {
        ipAddress = ip;
    }

    public static void processArgs(String[] args) {
        if (args.length < 1) {throw new IllegalArgumentException("no arguments provided");}
        if (args.length > 6) {throw new IllegalArgumentException("too many arguments provided");}
        if (args.length > 3) {
            try {
                setSkiLifts(Integer.parseInt(args[2]));
                setNumRuns(Integer.parseInt(args[3]));
                setIpAddress(args[4]);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        } else {
            try {
                setIpAddress(args[3]);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        try {
            setNumThreads(Integer.parseInt(args[0]));
            setNumSkiers(Integer.parseInt(args[1]));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * accepts arguments in two formats
     * @param args
     * format 1, three arguments:  numThreads numSkiers ipAddress
     * format 2, five arguments:   numThreads numSkiers numLifts numRuns ipAddress
     */
    public static void main(String[] args) throws InterruptedException{
        processArgs(args);
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(ipAddress);

        RequestLog requestLog = new RequestLog();

        /**
         * Phase 1 startup
         */
        System.out.println("Phase 1 Startup");
        final long startPhase1 = System.currentTimeMillis();
        int numThreadsPhase1 = (int) Math.floor(numThreads/4);
        CountDownLatch phase1Latch = new CountDownLatch((int) Math.floor(numThreadsPhase1 * 0.20));
        double numCallsPhase1 = floor((numRuns*0.2)*(numSkiers/numThreadsPhase1));
        int skierIdStartPhase1 = 0;
        int skierIdStopPhase1 = numSkiers/numThreadsPhase1;

        for (int i = 0; i < numThreadsPhase1; i++) {
            SkierThread skierThreadPhase1 = new SkierThread(i, apiClient, skierIdStartPhase1,
                    skierIdStopPhase1, startDayPhase1, endDayPhase1, numThreadsPhase1, numSkiers, numRuns,
                    numCallsPhase1, numLifts, phase1Latch, requestLog);
            new Thread(skierThreadPhase1).start();
            skierIdStartPhase1 = skierIdStopPhase1 + 1;
            skierIdStopPhase1 += numSkiers/numThreadsPhase1;
        }

        phase1Latch.await();


        /**
         * Phase 2 peak
         */
        System.out.println("Phase 2 Peak");
        CountDownLatch phase2Latch = new CountDownLatch((int) Math.floor(numThreads * 0.20));
        int skierIdStartPhase2 = 0;
        int skierIdStopPhase2 = numSkiers/numThreads;
        double numCallsPhase2 = floor((numRuns*0.6)*(numSkiers/numThreads));

        for (int i = 0; i < numThreads; i++) {
            SkierThread skierThreadPhase2 = new SkierThread(i, apiClient, skierIdStartPhase2,
                    skierIdStopPhase2, startDayPhase2, endDayPhase2, numThreads, numSkiers, numRuns,
                    numCallsPhase2, numLifts, phase2Latch, requestLog);
            new Thread(skierThreadPhase2).start();
            skierIdStartPhase2 = skierIdStopPhase2 + 1;
            skierIdStopPhase2 += numSkiers/numThreads;
        }

        phase2Latch.await();


        /**
         * Phase 3 cooldown
         */
        System.out.println("Phase 3 Cooldown");
        int numThreadsPhase3 = (int) Math.floor(numThreads*0.1);
        CountDownLatch phase3Latch = new CountDownLatch(numThreadsPhase3);
        double numCallsPhase3 = floor(numRuns*0.1);
        int skierIdStartPhase3 = 0;
        int skierIdStopPhase3 = numSkiers/numThreadsPhase3;

        for (int i = 0; i < numThreadsPhase3; i++) {
            SkierThread skierThreadPhase3 = new SkierThread(i, apiClient, skierIdStartPhase3,
                    skierIdStopPhase3, startDayPhase3, endDayPhase3, numThreadsPhase3, numSkiers, numRuns,
                    numCallsPhase3, numLifts, phase3Latch, requestLog);
            new Thread(skierThreadPhase3).start();
            skierIdStartPhase3 = skierIdStopPhase3 + 1;
            skierIdStopPhase3 += numSkiers/numThreadsPhase3;
        }

        final long endPhase3 = System.currentTimeMillis();
        phase3Latch.await();

        /**
         * Statistics
         */
        final long totalRunTime = endPhase3 - startPhase1;

        System.out.println("numThreads " + numThreads);
        System.out.printf("Total Run Time: %s milliseconds%n", totalRunTime);
        System.out.println("Total Number of Requests: " + requestLog.getNumRequests());
        System.out.println("Number of Successful Requests: " + requestLog.getNumSuccessfulRequests());
        System.out.println("Number of Unsuccessful Requests: " + requestLog.getNumUnsuccessfulRequests());
        throughputFinal = requestLog.getNumRequests()/(totalRunTime / 1000);
        System.out.println("Throughput: " + throughputFinal + " requests/second");
    }

}
