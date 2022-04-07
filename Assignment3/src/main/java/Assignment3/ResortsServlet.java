package Assignment3;

import EndpointClasses.Resorts;
import EndpointClasses.Status;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
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

@WebServlet(name = "Servers.ResortsServlet", value = "/resorts/*")
public class ResortsServlet extends HttpServlet {
    private String SEASONS = "seasons";
    private String DAY = "day";
    private String SKIERS = "skiers";
    private Gson gson = new Gson();
    private Integer resortID;
    private String seasonID;
    private String dayID;
    private Integer year;

    private String requestQueueName = "resort-rabbit";
    private ObjectPool<Channel> pool;

    private boolean isUrlValid(String[] urlParts) {
        if (urlParts.length == 0) {return true;}
        if (urlParts.length == 3) {
            try {
                resortID = Integer.valueOf(urlParts[1]);
            } catch (NumberFormatException ex) {
                return false;
            }
            if (!urlParts[2].equals(SEASONS)) {
                return false;
            }
            return true;
        }
        if (urlParts.length == 7) {
            if (!(urlParts[2].equals(SEASONS) && urlParts[4].equals(DAY) && urlParts[6].equals(SKIERS))) {
                return false;
            }
            try {
                resortID = Integer.valueOf(urlParts[1]);
                seasonID = urlParts[3];
                dayID = urlParts[5];
            } catch (NumberFormatException ex) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        pool = new GenericObjectPool<Channel>(new ChannelFactory());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        String urlPath = req.getPathInfo();

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
            res.getWriter().write("improper url " + urlParts.length);
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            // success
            res.setStatus(HttpServletResponse.SC_OK);
        }
        // do any sophisticated processing with urlParts which contains all the url params
        try {
            StringBuilder sb = new StringBuilder();
            String s;
            while ((s = req.getReader().readLine()) != null) {
                sb.append(s);
            }
            Resorts resorts = (Resorts) gson.fromJson(sb.toString(), Resorts.class);
            Status status = new Status();
            status.setSuccess(true);
            status.setDescription("success");
            res.getOutputStream().print(gson.toJson(status));
            res.getOutputStream().flush();
            System.out.println("200 OK");
        } catch (Exception ex) {
            ex.printStackTrace();
            Status status = new Status();
            status.setSuccess(false);
            status.setDescription(ex.getMessage());
            res.getOutputStream().print(gson.toJson(status));
            res.getOutputStream().flush();
            System.out.println("Exception");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        String urlPath = request.getPathInfo();
        PrintWriter out = response.getWriter();

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
            response.getWriter().write("improper url");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            response.setStatus(HttpServletResponse.SC_CREATED);
            // do any sophisticated processing with urlParts which contains all the url params
            try {
                StringBuilder sb = new StringBuilder();
                String s;
                while ((s = request.getReader().readLine()) != null) {
                    sb.append(s);
                }

                Resorts resorts = (Resorts) gson.fromJson(sb.toString(), Resorts.class);
                resorts.setYear(year);
                out.print(gson.toJson(resorts));

                // send message to rabbitmq
                Channel channel = null;
                channel = pool.borrowObject();
                channel.queueDeclare(requestQueueName, false, false, false, null);
                String message = gson.toJson(resorts);
                System.out.println(message);
                channel.basicPublish("", requestQueueName, null, message.getBytes(StandardCharsets.UTF_8));
                pool.returnObject(channel);

                out.flush();
                System.out.println(resorts);

            } catch (IOException | TimeoutException ex) { // rpc client
                Logger.getLogger(ResortsServlet.class.getName()).log(Level.SEVERE, null, ex); // end rpc client
            } catch (Exception ex) {
                ex.printStackTrace();
                out.flush();
                System.out.println("Resorts Server Exception");
            }
        }
    }
}
