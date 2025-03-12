//package io.github.smagical.bot.spider.proxy;
//
//import io.github.smagical.bot.util.DbUtil;
//import org.apache.hc.client5.http.classic.methods.HttpGet;
//import org.apache.hc.client5.http.config.RequestConfig;
//import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
//import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
//import org.apache.hc.client5.http.impl.classic.HttpClients;
//import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
//import org.apache.hc.core5.http.*;
//import org.apache.hc.core5.http.protocol.HttpContext;
//import org.apache.hc.core5.util.Timeout;
//
//import java.io.IOException;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
//public interface  ProxySpider {
//
//
//    public  void get(CloseableHttpClient client);
//
//
//    final static String INSERT = "INSERT INTO ip(ip,port) VALUES (?,?)";
//    final static String EXIST = "SELECT * FROM ip WHERE ip=? and port=?";
//    final static String DEL = "DELETE FROM ip WHERE ip=? and port=?";
//    final static String SELECT_ONE = "SELECT * FROM ip limit ?";
//
//    public static boolean exist(String ip,Integer port) throws IOException {
//        try(PreparedStatement statement = DbUtil.getConnection()
//                .prepareStatement(EXIST);) {
//            statement.setString(1,ip);
//            statement.setInt(2,port);
//            return statement.executeQuery().next();
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//
//    }
//
//    public static void save(String ip,Integer port) throws IOException {
//
//        try(PreparedStatement statement = DbUtil.getConnection()
//                .prepareStatement(INSERT);) {
//            statement.setString(1,ip);
//            statement.setInt(2,port);
//            statement.execute();
//            ProxySpider.log("save ---" +ip+":"+port);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    public static void del(String ip,Integer port) throws IOException {
//        try(PreparedStatement statement = DbUtil.getConnection()
//                .prepareStatement(DEL);) {
//            statement.setString(1,ip);
//            statement.setInt(2,port);
//            statement.execute();
//            ProxySpider.log("del ---" +ip+":"+port);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    public static List<String> get(int size){
//        ArrayList<String> list = new ArrayList<>();
//        try(PreparedStatement statement = DbUtil.getConnection()
//                .prepareStatement(INSERT);) {
//            statement.setInt(1,size);
//            ResultSet set = statement.executeQuery();
//            while(set.next()){
//                list.add(set.getString("ip")+":"+set.getInt("port"));
//            }
//            return list;
//        } catch (SQLException e) {
//           return list;
//        }
//    }
//
//    static boolean test(String ip, int port) {
//        try(CloseableHttpClient client  = HttpClients
//                .custom()
//                .addRequestInterceptorFirst(
//                        new HttpRequestInterceptor() {
//                            @Override
//                            public void process(HttpRequest request, EntityDetails entity, HttpContext context) throws HttpException, IOException {
//                                request.addHeader("User-Agent","Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");
//                            }
//                        }
//                )
//                .setRoutePlanner(new DefaultProxyRoutePlanner(new HttpHost(ip,port)))
//                .build()
//        ) {
//          //  ProxySpider.log("test from  "+ip+":"+port);
//            HttpGet request = new HttpGet("http://httpbin.org");
//            CloseableHttpResponse response = null;
//            request.setConfig(
//                    RequestConfig
//                            .custom()
//                            .setResponseTimeout(Timeout.of(2, TimeUnit.SECONDS))
//                            .setConnectionRequestTimeout(Timeout.of(1, TimeUnit.SECONDS))
//                            .build()
//            );
//            response = client.execute(request);
//            if (response.getCode() != 200) {
//                return false;
//            }
//            return true;
//        } catch (IOException e) {
//            return false;
//        }
//
//    }
//
//    static void addProxy(String ip,Integer port) throws IOException {
//        if (!ProxySpider.exist(ip,port) && ProxySpider.test(ip,port)){
//            ProxySpider.save(ip,port);
//        };
//    }
//
//     static void log(String msg){
//        System.out.println(msg);
//    }
//}
