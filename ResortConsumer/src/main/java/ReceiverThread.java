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
    private String QUEUE_NAME = "resort";
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
                String time = skiers.getTime();
                Integer resortID = skiers.getResortID();
                Integer liftID = skiers.getLiftID();

                // write to DB
                String resortDay = "ResortDay" + resortID.toString() + ":" + dayID;
                finalRedis.sadd(resortDay, skierID.toString());
                String liftDay = "LiftDay" + liftID.toString() + ":" + dayID;
                finalRedis.incr(liftDay);
                String dayHour = "DayHour" + dayID + ":" + time;
                finalRedis.incr(dayHour);
                // retrieve from DB
//                Set<String> dayList = finalRedis.smembers(resortDay);
//                System.out.println("dayList " + dayList);
//                String liftDayCount = finalRedis.get(liftDay);
//                System.out.println("liftDay Count " + liftDayCount);
//                String dayHourCount = finalRedis.get(dayHour);
//                System.out.println("dayHourCount " + dayHourCount);
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