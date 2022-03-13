import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

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


    public ReceiverThread(ConnectionFactory fact, final Connection conn, String qname, Map<Integer, Integer> map) {
        factory = fact;
        connection = conn;
        queue_name = qname;
        threadMap = map;
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
                threadMap.put(skierID, liftID);
            };
            // process messages
            channel.basicConsume(queue_name, false, deliverCallback, consumerTag -> {
            });

        } catch (IOException ex) {
            Logger.getLogger(RecvMT.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}