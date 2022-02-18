package Servers;

import EndpointClasses.Skiers;
import EndpointClasses.Status;
import com.google.gson.Gson;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "StatisticsServlet", value = "/statistics/*")
public class StatisticsServlet extends HttpServlet {
    private Gson gson = new Gson();

    private boolean isUrlValid(String[] urlParts) {
        // Validate the request url path according to the API spec
        // urlPath  = "/1/seasons/2019/day/1/skier/123"
        // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
        if (urlParts.length != 0) {
            return false;
        }
        return true;
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
            try {
                StringBuilder sb = new StringBuilder();
                String s;
                while ((s = req.getReader().readLine()) != null) {
                    sb.append(s);
                }
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
    }


}
