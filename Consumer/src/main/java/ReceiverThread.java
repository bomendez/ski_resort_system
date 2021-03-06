import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReceiverThread implements Runnable {
    private ConnectionFactory factory;
    private Connection connection;
    private String EXCHANGE_NAME;
    private String QUEUE_NAME = "skiers";
    private Map<Integer, Integer> threadMap;
    private Gson gson = new Gson();
    private JedisPool pool;
    private Integer VERTICAL_CONVERSION = 10;


    public ReceiverThread(ConnectionFactory fact, final Connection conn, String eExch, Map<Integer, Integer> map, JedisPool jpool) {
        factory = fact;
        connection = conn;
        EXCHANGE_NAME = eExch;
        threadMap = map;
        pool = jpool;
    }

    public void run() {
        Jedis redis = null;
        try {
            final Channel channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
//            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");
            System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");
            redis = pool.getResource();

            Jedis finalRedis = redis;
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println("message" + message);
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                System.out.println("after ack");
                Skiers skiers = (Skiers) gson.fromJson(message, Skiers.class);
                System.out.println("gson parsed " +skiers.getSkierID().toString());
                Integer skierID = skiers.getSkierID();
                String dayID = skiers.getDayID();
                String seasonID = skiers.getSeasonID();
                Integer liftID = skiers.getLiftID() * VERTICAL_CONVERSION;

                // write to DB
                String skierSeasonID = "Skier" + skierID.toString() + ":" + seasonID.toString();
                finalRedis.sadd(skierSeasonID, dayID);
                String skierVertDayID = "SkierVert" + skierID.toString() + ":" + dayID;
                Integer verticalTotal = liftID * 10;
                finalRedis.incrBy(skierVertDayID, verticalTotal);
                String skierLift = "SkierLift" + skierID.toString();
                finalRedis.lpush(skierLift, liftID.toString());
                // retrieve from DB
//                Set<String> daySet = finalRedis.smembers(skierSeasonID);
//                System.out.println("daySett " + daySet);
//                String skierVert = finalRedis.get(skierVertDayID);
//                System.out.println("vertCount " + skierVert);
//                List<String> liftList = finalRedis.lrange(skierLift, 0, -1);
//                System.out.println("skier: " + skierID.toString() + liftList);

            };
            // process messages
            channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {
            });

        } catch (IOException ex) {
            Logger.getLogger(RecvMT.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JedisConnectionException e) {
            if (redis != null)
            {
                pool.returnBrokenResource(redis);
                redis = null;
            }
            throw e;
        } finally
        {
            if (redis != null)
            {
                pool.returnResource(redis);
            }
        }
    }
}