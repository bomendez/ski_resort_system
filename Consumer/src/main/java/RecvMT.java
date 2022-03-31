import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.Map;
import java.util.concurrent.*;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

public class RecvMT {
    private static String EXCHANGE_NAME = "skier";
    private final static String QUEUE_NAME = "ski-rabbit";
    private final static int NUM_THREADS = 8;
    private final static String REDIS_IP = "35.160.13.77";

    private final static String rabbitMQIP = "localhost";
    private static Map<Integer, Integer> map = new ConcurrentHashMap(20000);


    public static void main(String[] argv) throws Exception {
        JedisPool pool = new JedisPool(new JedisPoolConfig(), REDIS_IP, 6379, Protocol.DEFAULT_TIMEOUT);
        System.out.println("Connected to Redis");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitMQIP);
        factory.setUsername("guest");
        factory.setPassword("guest");

        final Connection connection = factory.newConnection();

        // start threads and block to receive messages
        ExecutorService consumerPool = Executors.newFixedThreadPool(NUM_THREADS);

        for (int i = 0; i< NUM_THREADS; i++) {
            ReceiverThread receiverThread = new ReceiverThread(factory, connection, EXCHANGE_NAME, map, pool);
//            ReceiverThread receiverThread = new ReceiverThread(factory, connection, QUEUE_NAME, map, pool);
            consumerPool.execute(receiverThread);
        }
    }
}