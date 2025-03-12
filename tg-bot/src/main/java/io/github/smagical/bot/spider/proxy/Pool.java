//package io.github.smagical.bot.spider.proxy;
//
//
//import io.github.smagical.bot.spider.proxy.spider.Jhao104ProxyPoolProxySpider;
//import io.github.smagical.bot.spider.proxy.spider.KuaiDaiLiProxySpider;
//import io.github.smagical.bot.spider.proxy.spider.Python3WebSpiderProxySpider;
//import io.github.smagical.bot.spider.proxy.spider.ZdayeProxySpider;
//import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
//import org.apache.hc.client5.http.impl.classic.HttpClients;
//import org.apache.hc.core5.http.*;
//import org.apache.hc.core5.http.protocol.HttpContext;
//
//import java.io.IOException;
//import java.net.ProxySelector;
//import java.time.Duration;
//import java.util.List;
//import java.util.concurrent.*;
//
//public class Pool {
//    private final static ExecutorService service =
//            new ThreadPoolExecutor(
//                    Runtime.getRuntime().availableProcessors()/2,
//                    Runtime.getRuntime().availableProcessors(),
//                    60,
//                    TimeUnit.SECONDS,
//                    new ArrayBlockingQueue<Runnable>(100),
//                    Executors.defaultThreadFactory(),
//                    new ThreadPoolExecutor.CallerRunsPolicy()
//
//            );
//    private final static ArrayBlockingQueue<ProxySpider> queue = new ArrayBlockingQueue<>(1000);
//    private final static Thread thread;
//    static {
//        Runtime.getRuntime().addShutdownHook(new Thread() {
//            @Override
//            public void run() {
//                Pool.service.shutdown();
//            }
//        });
//        thread = new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                int cnt = 0;
//                while (true) {
//                    if (cnt == 0){
//                        for (ProxySpider spider : queue) {
//                            spider(spider);
//                        }
//                    }
//                    testDb();
//                    cnt++;
//                    cnt %= 2 * 30;
//                    try {
//                        Thread.sleep(Duration.ofSeconds(30).toMillis());
//                    } catch (InterruptedException e) {
//                    }
//                }
//            }
//        });
//        thread.start();
//
//
//        Pool.add(new KuaiDaiLiProxySpider());
//        Pool.add(new ZdayeProxySpider());
//        Jhao104ProxyPoolProxySpider.addLink("https://proxy.953959.xyz/");
//        Jhao104ProxyPoolProxySpider.addLink("http://47.109.72.18:5010/");
//        Pool.add(new Jhao104ProxyPoolProxySpider());
//        Python3WebSpiderProxySpider.addLink("https://proxypool.scrape.center");
//        Pool.add(new Python3WebSpiderProxySpider());
//
//    }
//
//    public static void add(final ProxySpider proxy) {
//        queue.add(proxy);
//        spider(proxy);
//    }
//
//    public static String get(){
//        List<String> list = ProxySpider.get(1);
//        return list.isEmpty() ?null:list.get(0);
//    }
//
//    public static boolean del(String ip){
//        try {
//            String[] ips = ip.split(":");
//            ProxySpider.del(ips[0],Integer.parseInt(ips[1]));
//            return true;
//        } catch (IOException e) {
//            return false;
//        }
//    }
//
//    public static boolean del(String ip,int port){
//        try {
//             ProxySpider.del(ip,port);
//             return true;
//        } catch (IOException e) {
//            return false;
//        }
//    }
//
//    private static void spider(ProxySpider proxySpider){
//        service.submit(
//                ()->{
//                    try (
//                            CloseableHttpClient client =
//                                    HttpClients.custom()
//                                            .setProxySelector(ProxySelector.getDefault())
//                                            .addRequestInterceptorFirst(
//                                                    new HttpRequestInterceptor() {
//                                                        @Override
//                                                        public void process(HttpRequest request, EntityDetails entity, HttpContext context) throws HttpException, IOException {
//                                                            request.addHeader("User-Agent","Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");
//                                                        }
//                                                    }
//                                            )
//                                            .build();
//                            ){
//                        proxySpider.get(client);
//                    } catch (IOException e) {
//
//                    }
//                }
//        );
//    }
//
//    private static void testDb(){
//        List<String> ips = ProxySpider.get(Integer.MAX_VALUE);
//        int size = ips.size();
//        for (int i = 0; i < size; i+=10) {
//            List<String> ips2 = ips.subList(i, Math.min(i+10,size));
//            service.submit(
//                    ()->{
//                        for (String ip : ips2) {
//                            String[] ipAndPort = ip.split(":");
//                            String ipAddr = ipAndPort[0];
//                            try {
//                                int port = Integer.parseInt(ipAndPort[1]);
//                                try {
//                                    if (!ProxySpider.test(ipAddr,port))
//                                        ProxySpider.del(ip,port);
//                                }catch (Exception e){
//                                    try {
//                                        ProxySpider.del(ip,port);
//                                    } catch (IOException ex) {}
//                                }
//                            }catch (Exception e){}
//                        }
//                    }
//            );
//        }
//    }
//
//}
