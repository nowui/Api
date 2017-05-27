package com.shanghaichuangshi.api;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ChuangShiFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String project = request.getHeader("Project");

        String url = request.getRequestURI();

        if (url.endsWith(".txt")) {
            chain.doFilter(request, response);

            return;
        }

        String path = "http://" + request.getServerName().replace("api.", "api." + project + ".") + url ;

        if (project.equals("MINA_RENT")) {
            path = "http://114.215.47.235:8091" + url;
        } else if (project.equals("MINA_DIANCAN")) {
            path = "http://114.215.47.235:8092" + url;
        }

        if (request.getMethod().equals("POST")) {
            HttpPost httpPost = new HttpPost(path);
            httpPost.addHeader("Accept", "application/json");
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.addHeader("Token", request.getHeader("Token"));
            httpPost.addHeader("Platform", request.getHeader("Platform"));
            httpPost.addHeader("Version", request.getHeader("Version"));

            String parameter = readData(request);

            StringEntity stringEntity = new StringEntity(parameter);
            stringEntity.setContentType("application/json");
            stringEntity.setContentEncoding("UTF-8");

            httpPost.setEntity(stringEntity);
            HttpResponse httpResponse = new DefaultHttpClient().execute(httpPost);

            HttpEntity httpEntity = httpResponse.getEntity();
            String content = EntityUtils.toString(httpEntity);

            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=utf-8");

            write(response, content);
        } else if (request.getMethod().equals("GET")) {
            path = path + "?" + request.getQueryString();

            HttpGet httpGet = new HttpGet(path);
            HttpResponse httpResponse = new DefaultHttpClient().execute(httpGet);

            if(httpResponse.getStatusLine().getStatusCode() == 200) {
                HttpEntity httpEntity = httpResponse.getEntity();
                String content = EntityUtils.toString(httpEntity);

                response.setCharacterEncoding("UTF-8");
                response.setContentType("text/html; charset=utf-8");
                PrintWriter out = null;

                write(response, content);
            } else {
                httpGet.abort();
            }
        }
    }

    @Override
    public void destroy() {

    }

    private static void write(HttpServletResponse response, String content) {
        PrintWriter out = null;
        try {
            out = response.getWriter();
            out.append(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private static String readData(HttpServletRequest request) {
        BufferedReader br = null;

        try {
            StringBuilder result = new StringBuilder();

            String line;
            for (br = request.getReader(); (line = br.readLine()) != null; result.append(line)) {
                if (result.length() > 0) {
                    result.append("\n");
                }
            }

            line = result.toString();
            return line;
        } catch (IOException var12) {
            throw new RuntimeException(var12);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException var11) {

                }
            }

        }
    }
}
