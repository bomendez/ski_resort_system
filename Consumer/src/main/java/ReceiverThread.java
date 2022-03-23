import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import redis.clients.jedis.Jedis;

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
    private Jedis jedis;


    public ReceiverThread(ConnectionFactory fact, final Connection conn, String qname, Map<Integer, Integer> map, Jedis mjedis) {
        factory = fact;
        connection = conn;
        queue_name = qname;
        threadMap = map;
        jedis = mjedis;
    }

    public void run() {
        try {
            final Channel channel = connection.createChannel();
            channel.queueDeclare(queue_name, false, false, false, null);
            // max one message per receiver
            channel.basicQos(1);
            System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                Skiers skiers = (Skiers) gson.fromJson(message, Skiers.class);
                Integer skierID = skiers.getSkierID();
                Integer liftID = skiers.getLiftID();
                System.out.println("skierID " + skierID + "liftID " + liftID);
//                threadMap.put(skierID, liftID);
                jedis.set(skierID.toString(), liftID.toString());
                String value = jedis.get(skierID.toString());
            };
            // process messages
            channel.basicConsume(queue_name, false, deliverCallback, consumerTag -> {
            });

        } catch (IOException ex) {
            Logger.getLogger(RecvMT.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}