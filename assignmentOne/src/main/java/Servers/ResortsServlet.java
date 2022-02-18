package Servers;

import EndpointClasses.Resorts;
import EndpointClasses.Skiers;
import EndpointClasses.Status;
import com.google.gson.Gson;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

@WebServlet(name = "Servers.ResortsServlet", value = "/resorts/*")
public class ResortsServlet extends HttpServlet {
    private String SEASONS = "seasons";
    private String[] seasonParams = new String[] {"seasons", "day", "skiers"};
    private Gson gson = new Gson();

    private boolean isUrlValid(String[] urlParts) {
        if (urlParts.length == 0) {return true;}
        if (urlParts.length == 3) {
            try {
                Integer.parseInt(urlParts[1]);
            } catch (NumberFormatException ex) {
                return false;
            }
            if (!urlParts[2].equals(SEASONS)) {
                return false;
            }
            return true;
        }
        if (urlParts.length == 7) {
            if (!(urlParts[2].equals(seasonParams[0]) && urlParts[4].equals(seasonParams[1]) && urlParts[6].equals(seasonParams[2]))) {
                return false;
            }
            try {
                Integer.parseInt(urlParts[1]);
                Integer.parseInt(urlParts[3]);
                Integer.parseInt(urlParts[5]);
            } catch (NumberFormatException ex) {
                return false;
            }
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
            response.setStatus(HttpServletResponse.SC_CREATED);
            // do any sophisticated processing with urlParts which contains all the url params
            // TODO: process url params in `urlParts`
            try {
                StringBuilder sb = new StringBuilder();
                String s;
                while ((s = request.getReader().readLine()) != null) {
                    sb.append(s);
                }
                Resorts resorts = (Resorts) gson.fromJson(sb.toString(), Resorts.class);
//                resorts.setResortID(request.getParameter());
                Status status = new Status();
                status.setSuccess(true);
                status.setDescription("success");
                response.getOutputStream().print(gson.toJson(status));
                response.getOutputStream().flush();
            } catch (Exception ex) {
                ex.printStackTrace();
                Status status = new Status();
                status.setSuccess(false);
                status.setDescription(ex.getMessage());
                response.getOutputStream().print(gson.toJson(status));
                response.getOutputStream().flush();
            }
//            response.getWriter().write(body);
        }
    }
}
