import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.Map;
import java.util.concurrent.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

public class RecvMT {

    private final static String QUEUE_NAME = "ski-rabbit";
    private final static int NUM_THREADS = 8;
    private static Map<Integer, Integer> map = new ConcurrentHashMap(20000);


    public static void main(String[] argv) throws Exception {
        JedisPool pool = new JedisPool(new JedisPoolConfig(), "54.213.252.120", 6379, Protocol.DEFAULT_TIMEOUT, "p@ss$12E45");

//        Jedis jedis = new Jedis("54.213.252.120", 6379);
//        jedis.auth("p@ss$12E45");
        System.out.println("Connected to Redis");

        ConnectionFactory factory = new ConnectionFactory();
//        factory.setHost("localhost");
//        factory.setUsername("guest");
//        factory.setPassword("guest");
        factory.setHost("54.68.165.222");
        factory.setUsername("radmin");
        factory.setPassword("radmin");

        final Connection connection = factory.newConnection();

        // start threads and block to receive messages
        ExecutorService consumerPool = Executors.newFixedThreadPool(NUM_THREADS);

        for (int i = 0; i< NUM_THREADS; i++) {
            ReceiverThread receiverThread = new ReceiverThread(factory, connection, QUEUE_NAME, map, pool);
            consumerPool.execute(receiverThread);
        }
    }
}