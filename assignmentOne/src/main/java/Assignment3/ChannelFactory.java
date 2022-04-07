package Assignment3;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ChannelFactory extends BasePooledObjectFactory<Channel>{
    private Connection conn;
    private int NUM_CHANNELS = 4;

    @Override
    public Channel create() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
//        factory.setHost("localhost");
//        factory.setUsername("guest");
//        factory.setPassword("guest");
        factory.setHost("54.188.119.38");
        factory.setUsername("radmin");
        factory.setPassword("radmin");
        try {
            conn = factory.newConnection();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        Channel channel = conn.createChannel();
        return channel;
    }

    @Override
    public PooledObject<Channel> wrap(Channel channel) {
        return new DefaultPooledObject<Channel>(channel);
    }
}
