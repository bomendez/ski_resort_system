import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecvMT {
    private static String EXCHANGE_NAME = "skier";
    private final static String QUEUE_NAME = "ski-rabbit";
    private final static int NUM_THREADS = 64;
    private final static String REDIS_IP = "54.189.41.117";

    private final static String rabbitMQIP = "54.188.119.38";
    private static Map<Integer, Integer> map = new ConcurrentHashMap(20000);


    public static void main(String[] argv) throws Exception {
        JedisPool pool = new JedisPool(new JedisPoolConfig(), REDIS_IP, 6379, Protocol.DEFAULT_TIMEOUT);
        System.out.println("Connected to Redis");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitMQIP);
//        factory.setUsername("guest");
//        factory.setPassword("guest");
        factory.setUsername("radmin");
        factory.setPassword("radmin");

        final Connection connection = factory.newConnection();

        // start threads and block to receive messages
        ExecutorService consumerPool = Executors.newFixedThreadPool(NUM_THREADS);

        for (int i = 0; i< NUM_THREADS; i++) {
            ReceiverThread receiverThread = new ReceiverThread(factory, connection, EXCHANGE_NAME, map, pool);
            consumerPool.execute(receiverThread);
        }
    }
}