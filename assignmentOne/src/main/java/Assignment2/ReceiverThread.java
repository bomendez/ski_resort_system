package Assignment2;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReceiverThread implements Runnable {
    private ConnectionFactory factory;
    private Connection connection;
    private String queue_name;
    private BlockingQueue buffer;


    public ReceiverThread(ConnectionFactory fact, final Connection conn, String qname, BlockingQueue buf) {
        factory = fact;
        connection = conn;
        queue_name = qname;
        buffer = buf;
    }

    public void run() {
        boolean active = true;
        while (active) {
            try {
                String  s = (String) buffer.take();
                if (s.equals("")) {active = false;} else {
                    final Channel channel = connection.createChannel();
                    channel.queueDeclare(queue_name, false, false, false, null);
                    // max one message per receiver
                    channel.basicQos(1);
                    System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");

                    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                        String message = new String(delivery.getBody(), "UTF-8");
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                        System.out.println("Callback thread ID = " + Thread.currentThread().getId() + " Received '" + message + "'");
                    };
                    // process messages
                    channel.basicConsume(queue_name, false, deliverCallback, consumerTag -> {
                    });
                }
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(RecvMT.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
