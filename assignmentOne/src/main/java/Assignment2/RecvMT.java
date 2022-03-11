package Assignment2;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.AMQP.BasicProperties;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RecvMT {

    private final static String QUEUE_NAME = "skiers_queue";
    private final static int NUM_MESSAGES_PER_THREAD =4;
    private static Map<Integer, Integer> map = new ConcurrentHashMap(20000);

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        final Connection connection = factory.newConnection();

        // start threads and block to receive messages
        ExecutorService consumerPool = Executors.newFixedThreadPool(NUM_MESSAGES_PER_THREAD);

        for (int i=0; i<NUM_MESSAGES_PER_THREAD; i++) {
            ReceiverThread receiverThread = new ReceiverThread(factory, connection, QUEUE_NAME, map);
            consumerPool.execute(receiverThread);
        }

//        consumerPool.shutdown();
//        consumerPool.awaitTermination(10, TimeUnit.SECONDS);
//
//        buffer.put("");
//        System.out.println(" [*] Consumer Closed due to timeout");
    }
}