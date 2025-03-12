//package io.github.smagical.bot.spider.proxy.spider;
//
//import io.github.smagical.bot.spider.proxy.ProxySpider;
//import org.apache.hc.client5.http.classic.methods.HttpGet;
//import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
//import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//
//public class ZdayeProxySpider implements ProxySpider {
//    private final  static String  url= "https://www.zdaye.com/free/";
//    @Override
//    public void get(CloseableHttpClient client) {
//        for (int i = 1; i < 15; i++) {
//
//            try {
//                HttpGet get = new HttpGet(url  +i);
//                String body = client.execute(get,new BasicHttpClientResponseHandler());
//                Elements elements = Jsoup.parse(body).getElementById("ipc").getElementsByTag("tr");
//                for (Element element : elements) {
//                    Elements elements2 = element.getElementsByTag("td");
//                    String ip = elements2.get(0).text();
//                    int port = Integer.parseInt(elements2.get(1).text());
//                    ProxySpider.addProxy(ip, port);
//                }
//            } catch (Exception e) {
//
//            }
//        }
//
//    }
//}
