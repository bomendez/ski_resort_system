package Assignment2;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.AMQP.BasicProperties;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RecvMT {

    private final static String QUEUE_NAME = "skiers_queue";
    private final static int NUM_MESSAGES_PER_THREAD =32;


    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        final Connection connection = factory.newConnection();
        final BlockingQueue buffer = new LinkedBlockingQueue();

//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                try {
//
//                    final Channel channel = connection.createChannel();
//                    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
//                    // max one message per receiver
//                    channel.basicQos(1);
//                    System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");
//
//                    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
//                        String message = new String(delivery.getBody(), "UTF-8");
//                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
//                        System.out.println( "Callback thread ID = " + Thread.currentThread().getId() + " Received '" + message + "'");
//                    };
//                    // process messages
//                    channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
//                } catch (IOException ex) {
//                    Logger.getLogger(RecvMT.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        };
        // start threads and block to receive messages
        ExecutorService consumerPool = Executors.newFixedThreadPool(NUM_MESSAGES_PER_THREAD);
        ReceiverThread receiverThread = new ReceiverThread(factory, connection, QUEUE_NAME, buffer);
        for (int i=0; i<NUM_MESSAGES_PER_THREAD; i++) {
            consumerPool.execute(receiverThread);
        }

        consumerPool.shutdown();
        consumerPool.awaitTermination(10, TimeUnit.SECONDS);

        buffer.put("");
        System.out.println(" [*] Consumer Closed due to timeout");
    }
}