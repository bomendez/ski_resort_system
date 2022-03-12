package Assignment2;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.Map;
import java.util.concurrent.*;

public class RecvMT {

    private final static String QUEUE_NAME = "ski-rabbit";
    private final static int NUM_THREADS = 8;
    private static Map<Integer, Integer> map = new ConcurrentHashMap(20000);

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("guest");
        factory.setPassword("guest");
//        factory.setHost("54.68.165.222");
//        factory.setUsername("radmin");
//        factory.setPassword("radmin");

        final Connection connection = factory.newConnection();

        // start threads and block to receive messages
        ExecutorService consumerPool = Executors.newFixedThreadPool(NUM_THREADS);

        for (int i = 0; i< NUM_THREADS; i++) {
            ReceiverThread receiverThread = new ReceiverThread(factory, connection, QUEUE_NAME, map);
            consumerPool.execute(receiverThread);
        }
    }
}