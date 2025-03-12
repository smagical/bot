//package io.github.smagical.bot.spider.proxy.spider;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.json.JsonMapper;
//import com.fasterxml.jackson.databind.node.ArrayNode;
//import io.github.smagical.bot.spider.proxy.ProxySpider;
//import org.apache.hc.client5.http.classic.methods.HttpGet;
//import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
//import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
//
//
//public class KuaiDaiLiProxySpider implements ProxySpider {
//
//
//    @Override
//    public void get(CloseableHttpClient client) {
//        for (int i = 1; i < 15; i++) {
//            final String url ="https://www.kuaidaili.com/free/fps/"+i;
//            try {
//                HttpGet httpGet = new HttpGet(url);
//                String body = client.execute(httpGet,new BasicHttpClientResponseHandler());
//                String fps = body.substring(body.indexOf("fpsList"));
//                int st = fps.indexOf("[");
//                int en = fps.indexOf("]");
//                fps = fps.substring(st,en+1);
//                ObjectMapper objectMapper = JsonMapper.builder().build();
//                ArrayNode node = (ArrayNode)objectMapper.readTree(fps);
//                for (JsonNode jsonNode : node) {
//                    try {
//                        String ip = jsonNode.get("ip").asText();
//                        int port = jsonNode.get("port").asInt();
//                        ProxySpider.addProxy(ip,port);
//                    } catch (Exception e) {}
//
//                }
//            }catch (Exception e){}
//
//        }
//    }
//}
