import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReceiverThread implements Runnable {
    private ConnectionFactory factory;
    private Connection connection;
    private String queue_name;
    private Map<Integer, Integer> threadMap;
    private Gson gson = new Gson();
    private JedisPool pool;
    private Integer VERTICAL_CONVERSION = 10;


    public ReceiverThread(ConnectionFactory fact, final Connection conn, String qname, Map<Integer, Integer> map, JedisPool jpool) {
        factory = fact;
        connection = conn;
        queue_name = qname;
        threadMap = map;
        pool = jpool;
    }

    public void run() {
        Jedis redis = null;
        try {
            final Channel channel = connection.createChannel();
            channel.queueDeclare(queue_name, false, false, false, null);
            // max one message per receiver
            channel.basicQos(1);
            System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");
            redis = pool.getResource();

            Jedis finalRedis = redis;
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                Skiers skiers = (Skiers) gson.fromJson(message, Skiers.class);
                Integer skierID = skiers.getSkierID();
                String dayID = skiers.getDayID();
                String seasonID = skiers.getSeasonID();
                Integer liftID = skiers.getLiftID() * VERTICAL_CONVERSION;
//                threadMap.put(skierID, liftID);
                System.out.println("line 51");
                finalRedis.hset(skierID.toString(), dayID, liftID.toString());
                finalRedis.hset(skierID.toString(), seasonID, dayID);
                String dayIDRedis = finalRedis.hget(skierID.toString(), dayID);
                System.out.println(" dayID:" + dayIDRedis);
            };
            // process messages
            channel.basicConsume(queue_name, false, deliverCallback, consumerTag -> {
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