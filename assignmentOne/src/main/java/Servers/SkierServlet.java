package Servers;

import EndpointClasses.Skiers;
import EndpointClasses.Status;
import com.google.gson.Gson;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

@WebServlet(name = "Servers.SkierServlet", value = "/skiers/*")
public class SkierServlet extends HttpServlet {
    private String VERTICAL = "vertical";
    private String[] dayParams = new String[]{"seasons", "days", "skiers"};
    private Gson gson = new Gson();

    private boolean isUrlValid(String[] urlParts) {
        // Validate the request url path according to the API spec
        // urlPath  = "/1/seasons/2019/day/1/skier/123"
        // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
        if (urlParts.length == 0) {return true;}

        if (urlParts.length == 3) {
            if (!urlParts[2].equals(VERTICAL)) {
                return false;
            }
            try {
                Integer.parseInt(urlParts[1]);
            } catch (NumberFormatException ex) {
                return false;
            }
            return true;
        }

        if (urlParts.length == 8) {
            for (int i=0; i < urlParts.length; i++) {
                if (i % 2 == 1) {
                    try {
                        System.out.println(urlParts[i] + " int " + Integer.parseInt(urlParts[i]));
                        Integer.parseInt(urlParts[i]);
                    } catch (NumberFormatException ex) {
                        return false;
                    }
                }
            }
            if (!(urlParts[2].equals(dayParams[0]) && urlParts[4].equals(dayParams[1]) &&
                    urlParts[6].equals(dayParams[2]))) {
                return false;
            }
            return true;
        }
        return false;
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
            // do any sophisticated processing with urlParts which contains all the url params
            try {
                StringBuilder sb = new StringBuilder();
                String s;
                while ((s = req.getReader().readLine()) != null) {
                    sb.append(s);
                }
                Skiers skiers = (Skiers) gson.fromJson(sb.toString(), Skiers.class);
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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        String urlPath = request.getPathInfo();
        System.out.println(urlPath);
        // retrieve and parse body
        BufferedReader buffer = request.getReader();
        final String body = buffer.lines().collect(Collectors.joining());
        System.out.println(body);

        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("missing parameters");
            return;
        }

        String[] urlParts = urlPath.split("/");
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)

        if (!isUrlValid(urlParts)) {
            response.getWriter().write("improper url");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            System.out.println("else reached");
            response.setStatus(HttpServletResponse.SC_CREATED);
            System.out.println("else finished");
        }
    }


}
