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
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReceiverThread implements Runnable {
    private ConnectionFactory factory;
    private Connection connection;
    private String EXCHANGE_NAME;
    private String QUEUE_NAME = "skier";
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
//            channel.queueDeclare(EXCHANGE_NAME, false, false, false, null);
//            channel.basicQos(1);


            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
            String queueName = "skier";
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
//                threadMap.put(skierID, liftID);
                String skierSeasonID = skierID.toString() + ":" + seasonID.toString();
                finalRedis.lpush(skierSeasonID, dayID);
                System.out.println("pushed to redis");
                List<String> dayList = finalRedis.lrange(skierSeasonID, 0, -1);
                System.out.println("dayList " + dayList);
                System.out.println("dayList length: " + dayList.size());
                finalRedis.lpush(skierID.toString(), liftID.toString());
                List<String> liftIDList = finalRedis.lrange(skierID.toString(), 0, -1);
                Integer verticalTotal = liftID * 10;
                String skierDayID = skierID.toString() + ":" + dayID.toString();
                finalRedis.lpush(skierDayID, verticalTotal.toString());
                List<String> verticalList = finalRedis.lrange(skierDayID, 0, -1);
                System.out.println("skier: " + skierID.toString() + verticalList);

            };
            // process messages
//            channel.basicConsume(queueName, false, deliverCallback, consumerTag -> {
//            });
            channel.basicConsume(EXCHANGE_NAME, false, deliverCallback, consumerTag -> {
            });

        } catch (IOException ex) {
            System.out.println("Logger Exception");
            Logger.getLogger(RecvMT.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JedisConnectionException e) {
            System.out.println("JedisConnectionException");
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