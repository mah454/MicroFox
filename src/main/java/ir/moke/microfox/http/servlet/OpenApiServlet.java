package ir.moke.microfox.http.servlet;

import io.swagger.v3.oas.models.OpenAPI;
import ir.moke.kafir.utils.JsonUtils;
import ir.moke.microfox.http.OpenApiGenerator;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

@WebServlet(urlPatterns = {"/docs","/docs/*"})
public class OpenApiServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(OpenApiServlet.class);
    private String json;

    @Override
    public void init() {
        OpenAPI openAPI = OpenApiGenerator.generate();
        json = JsonUtils.toJson(openAPI);
        logger.info("Initialize OpenAPI");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getRequestURI();
        resp.setCharacterEncoding("UTF-8");
        if (pathInfo.equalsIgnoreCase("/docs/rapidoc-min.js")) {
            resp.setContentType("text/javascript");
            resp.getWriter().write(rapidocJS());
        } else if (pathInfo.endsWith("woff2")) {
            String[] split = pathInfo.split("/");
            String fontFile = split[split.length - 1];
            resp.setContentType("font/woff2");
            resp.getWriter().write(fontWOFF2(fontFile));
        } else if (pathInfo.equalsIgnoreCase("/docs/openapi.json")) {
            resp.setContentType("application/json");
            resp.getWriter().write(json);
        } else {
            resp.setContentType("text/html");
            resp.getWriter().write(indexHTML());
        }
    }

    private static String rapidocJS() {
        try (InputStream inputStream = OpenApiServlet.class.getClassLoader().getResourceAsStream("open-api/rapidoc-min.js")) {
            if (inputStream != null) {
                return new String(inputStream.readAllBytes());
            }
            return "";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String indexHTML() {
        try (InputStream inputStream = OpenApiServlet.class.getClassLoader().getResourceAsStream("open-api/index.html")) {
            if (inputStream != null) {
                return new String(inputStream.readAllBytes());
            }
            return "";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String fontWOFF2(String fontFile) {
        try (InputStream inputStream = OpenApiServlet.class.getClassLoader().getResourceAsStream("open-api/%s".formatted(fontFile))) {
            if (inputStream != null) {
                return new String(inputStream.readAllBytes());
            }
            return "";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
