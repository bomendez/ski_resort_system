package Assignment2;

import EndpointClasses.Skiers;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.AMQP.BasicProperties;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "SkierServlet", value = "/skiers/*")
public class SkierServlet extends HttpServlet {
    private String VERTICAL = "vertical";
    private String[] dayParams = new String[]{"seasons", "days", "skiers"};
    private Gson gson = new Gson();
    private Integer resortID;
    private String seasonID;
    private String dayID;
    private Integer skierID;

    private String requestQueueName = "ski-rabbits";
    private ObjectPool<Channel> pool;

    private boolean isUrlValid(String[] urlParts) {
        // Validate the request url path according to the API spec
        // urlPath  = "/1/seasons/2019/days/1/skiers/123"
        // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
        if (urlParts.length == 0) {return true;}

        if (urlParts.length == 3) {
            if (!urlParts[2].equals(VERTICAL)) {
                return false;
            }
            try {
                Integer.parseInt(urlParts[1]);
                skierID = Integer.valueOf(urlParts[1]);
            } catch (NumberFormatException ex) {
                return false;
            }
            return true;
        }

        if (urlParts.length == 8) {
            try {
                resortID = Integer.valueOf(urlParts[1]);
                seasonID = urlParts[3];
                dayID = urlParts[5];
                skierID = Integer.valueOf(urlParts[7]);
            } catch (NumberFormatException ex) {
                return false;
            }
            if (!(urlParts[2].equals(dayParams[0]) && urlParts[4].equals(dayParams[1]) &&
                    urlParts[6].equals(dayParams[2]))) {
                return false;
            }
            return true;
        }
        return false;
    }

    // reference from https://docstore.mik.ua/orelly/java-ent/servlet/ch03_03.htm
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        pool = new GenericObjectPool<Channel>(new ChannelFactory());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        String urlPath = req.getPathInfo();
        PrintWriter out = res.getWriter();
        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("missing parameters");
            return;
        }

        String[] urlParts = urlPath.split("/");
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)

        if (!isUrlValid(urlParts)) {
            out.write("improper url " + urlParts.length);
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            // success
            res.setStatus(HttpServletResponse.SC_OK);
            // do any sophisticated processing with urlParts which contains all the url params
            try {
                StringBuilder sb = new StringBuilder();
                String s;
                while ((s = req.getReader().readLine()) != null) {
                    sb.append(s);
                }
                Skiers skiers = (Skiers) gson.fromJson(sb.toString(), Skiers.class);
                out.print(gson.toJson(skiers));
                out.flush();
                System.out.println(skiers);
            } catch (Exception ex) {
                ex.printStackTrace();
                out.flush();
                System.out.println("Exception");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        String urlPath = request.getPathInfo();
        PrintWriter out = response.getWriter();
        // retrieve and parse body

        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.write("missing parameters");
            return;
        }

        String[] urlParts = urlPath.split("/");
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)

        if (!isUrlValid(urlParts)) {
            out.write("improper url");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            response.setStatus(HttpServletResponse.SC_CREATED);
            try {
                StringBuilder sb = new StringBuilder();
                String s;
                while ((s = request.getReader().readLine()) != null) {
                    sb.append(s);
                }

                Skiers skiers = (Skiers) gson.fromJson(sb.toString(), Skiers.class);
                skiers.setResortID(resortID);
                skiers.setSeasonID(seasonID);
                skiers.setDayID(dayID);
                skiers.setSkierID(skierID);
                out.print(gson.toJson(skiers));

                // send message to rabbitmq
                Channel channel = null;
                channel = pool.borrowObject();
                channel.queueDeclare(requestQueueName, false, false, false, null);
                String message = gson.toJson(skiers);
                System.out.println(message);
                System.out.println("server skierID " + skiers.getSkierID());
                channel.basicPublish("", requestQueueName, null, message.getBytes(StandardCharsets.UTF_8));
//                channel.close();
                pool.returnObject(channel);
                // end channel pool

                out.flush();
                System.out.println(skiers);
            } catch (IOException | TimeoutException ex) { // rpc client
                Logger.getLogger(SkierServlet.class.getName()).log(Level.SEVERE, null, ex); // end rpc client
            } catch (Exception ex) {
                ex.printStackTrace();
                out.flush();
                System.out.println("Exception");
            }
            System.out.println("created");
//            conn.close();
        }
    }


}
