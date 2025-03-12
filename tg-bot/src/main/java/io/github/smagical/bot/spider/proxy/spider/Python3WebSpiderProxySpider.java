//package io.github.smagical.bot.spider.proxy.spider;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.github.smagical.bot.spider.proxy.ProxySpider;
//import org.apache.hc.client5.http.classic.methods.HttpGet;
//import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
//import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
//
//import java.io.IOException;
//import java.util.HashSet;
//import java.util.Set;
//import java.util.concurrent.LinkedBlockingQueue;
//
//public class Python3WebSpiderProxySpider implements ProxySpider {
//
//
//    private static LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
//    public static void addLink(String link) {
//        queue.add(link);
//    }
//
//    @Override
//    public void get(CloseableHttpClient client) {
//        Set<String> set = new HashSet<>();
//        set.addAll(queue);
//        for (String link : set) {
//            if (link.endsWith("/")) link = link.substring(0, link.length() - 1);
//            HttpGet get = new HttpGet(link +"/all");
//            ObjectMapper mapper = new ObjectMapper();
//            try {
//                String body =  client.execute(get,new BasicHttpClientResponseHandler());
//                for (String url : body.split(" ")) {
//                    String[] urls = url.split(":");
//                    String ip = urls[0];
//                    int port = Integer.parseInt(urls[1]);
//                    ProxySpider.addProxy(ip,port);
//                }
//            } catch (IOException e) {
//
//            }
//        }
//    }
//}
