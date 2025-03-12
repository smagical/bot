package io.github.smagical.bot.spider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.*;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.htmlunit.BrowserVersion;
import org.htmlunit.NicelyResynchronizingAjaxController;
import org.htmlunit.WebClient;
import org.htmlunit.WebRequest;
import org.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.net.ProxySelector;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavMix {
    static ObjectMapper mapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();
    static CloseableHttpClient client = HttpClients
            .custom()
            .addRequestInterceptorFirst(
                    new HttpRequestInterceptor() {
                        @Override
                        public void process(HttpRequest request, EntityDetails entity, HttpContext context) throws HttpException, IOException {
                            request.addHeader("User-Agent","Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");
                        }
                    }
            )
            .setProxySelector(ProxySelector.getDefault())
            .build();
    //xvideo fc2ppv vr soaring popularity
    public static void main(String[] args) throws IOException, ScriptException {
        List<Card> cardList = solvePage(UrlBuilder.newBuilder("https://zh-cn.javmix.tv/video").build());
        for (Card card : cardList) {
            List<List<String>> list = solveDownload(card.page);
            card.embedList = list.get(0);
            card.downloadList = list.get(1);
        }
        System.out.println(cardList);

    }



    static List<List<String>> solveDownload(String url) throws IOException, ScriptException {
        HttpGet request = new HttpGet(url);
        request.addHeader("referer","https://zh-cn.javmix.tv/");
        String body = client.execute(request, new BasicHttpClientResponseHandler());
        Element page = Jsoup.parse(body)
                .body();
        String  js = page.getElementsByTag("script")
                .stream()
                .filter(
                        element ->
                                element.html().startsWith("eval(function")
                ).map(element -> element.html().replace("<script>","").replace("</script>",""))
                .findFirst()
                .orElse(null);
        js =  Util.solverJs(js);
        List<List<String>> result = Arrays.asList(
               Util.solverEmbed(js),Util.solverDl(js)
        );
        return result;
    }

    static List<Card> solvePage(UrlBuilder urlBuilder) throws IOException, ScriptException {
        HttpGet request = new HttpGet(urlBuilder.build());
        request.addHeader("referer","https://zh-cn.javmix.tv/");
        String body = client.execute(request, new BasicHttpClientResponseHandler());
        return solvePostList(body);
    }

    static List<Card> solvePostList(String body) throws ScriptException, IOException {
        Element postList = Jsoup.parse(body).body().getElementsByClass("post-list").first();
        Elements cards =  postList.getElementsByTag("a");
        List<Card> cardList = new ArrayList<Card>();
        for (Element card : cards) {
            String url = card.attr("href");
            String title = card.getElementsByTag("span").first().text();
            String image = card.getElementsByTag("img").first().attr("src");
            String uploadTime = card.getElementsByClass("post-list-time").first().text();
            Card card1 = new Card();
            card1.title = title;
            card1.image = image;
            card1.uploadTime = Util.solverDate(uploadTime);
            card1.page = url;
            card1.serialNumber = Util.getSerialNumberFormUrl(url);
            cardList.add(card1);
        }
        return cardList;
    }



    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    static class Card{
        String title;
        String image;
        String page;
        String uploadTime;
        String serialNumber;
        List<String> embedList;
        List<String> downloadList;
    }


    static class UrlBuilder {
        private String path;
        private int page = -1;
        private HashMap<String,String> params;

        private UrlBuilder() {
            params = new HashMap<>();
        }
        static UrlBuilder.Builder newBuilder(String path) {
            return new UrlBuilder.Builder(path);
        }
        String build() {
            StringBuilder url = new StringBuilder();
            url.append(path);
            url.append("/");
            if (page != -1){
                url.append("/page/");
                url.append(page);
            }
            if (params.size() > 0) {
                url.append("?");
                params.forEach((k,v) -> url.append(k).append("=").append(v).append("&"));
            }
            return url.toString().substring(0,url.length()-1);
        }
        static class Builder {
            private UrlBuilder urlBuilder = new UrlBuilder();
            private Builder(String path) {
                if (path.endsWith("/")) path = path.substring(0, path.length()-1);
                urlBuilder.path = path;
            }
            public Builder addParam(String key, String value) {
                urlBuilder.params.put(key, value);
                return this;
            }

            private Builder page(int page) {
                urlBuilder.page = page;
                return this;
            }
            public UrlBuilder build() {
                return urlBuilder;
            }
        }

    }

    private static class Util{
        static String getSerialNumberFormUrl(String url){
            if (url.endsWith("/")) url = url.substring(0, url.length()-1);
            int index = url.lastIndexOf("/");
            if(index == -1){
                return null;
            }
            return url.substring(index+1);
        }
        static String solverDate(String date){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy 年 MM 月 dd 日");
            try {
                return new SimpleDateFormat("yyyy/MM/dd").format(simpleDateFormat.parse(date));
            } catch (ParseException e) {
                return date;
            }
        }
        static String solverJs(String js) throws ScriptException {
            js = js.replace("eval(","var tmp = ");
            int index = js.indexOf("{");
            int cnt = 1;
            char[] s = js.toCharArray();
            for (int i = index+1; i < s.length; i++) {
                if (s[i]=='{') cnt++;
                else if (s[i]=='}') cnt--;
                if (cnt == 0){
                    js = js.substring(0,i+1) + ";tmp" + js.substring(i+1);
                    break;
                }
            }
            int last = js.lastIndexOf(")");
            js = js.substring(0,last) +   js.substring(last+1);
            ScriptEngineManager scriptEngine = new ScriptEngineManager();
            ScriptEngine engine = scriptEngine.getEngineByMimeType("text/javascript");
            return  engine.eval(js).toString();
        }
        static List<String> solverDl(String js){
            List<String> dl = new ArrayList<>();
            String reg = "DL\\.<a(.*?)>";
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(js);
            while (matcher.find()) {
                dl.add(matcher.group());
            }
            return dl.stream().map(
                    e->Jsoup.parse(e).getElementsByTag("a")
                            .attr("href")
            ).distinct().toList();
        }
        static List<String> solverEmbed(String js){
            List<String> dl = new ArrayList<>();
            String reg = "<iframe(.*?)>";
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(js);
            while (matcher.find()) {
                dl.add(matcher.group());
            }

            return dl.stream().map(
                    e->Jsoup.parse(e).getElementsByTag("iframe")
                            .attr("src")
            ).distinct().toList();
        }
        static String solverDownloadLink(String url) throws ScriptException, IOException {
            String res = null;
            if (((res = getDownloadStreamtape(url) )) != null) return res;
            return res;
        }

        private static String getDownloadVidhidepro(String url) throws IOException {
            if (url.contains("vidhidepro.com")){
                url = url.replace("vidhidepro.com/v","vidhidepro.com/d");
                HttpGet request = new HttpGet(url);
                request.addHeader("referer","https://vidhidepro.com");
                String body = client.execute(request, new BasicHttpClientResponseHandler());
                String downloadOne = Jsoup.parse(body)
                        .body()
                        .getElementsByTag("center")
                        .first()
                        .getElementsByTag("a")
                        .attr("href");
                downloadOne = "https://vidhidepro.com"+downloadOne;

                //todo 破解谷歌验证码
                WebClient webClient = new WebClient(BrowserVersion.FIREFOX);
                webClient.getOptions().setDownloadImages(false);
                webClient.getOptions().setJavaScriptEnabled(true);
                webClient.getOptions().setThrowExceptionOnScriptError(false);//当JS执行出错的时候是否抛出异常, 这里选择不需要
                webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);//当HTTP的状态非200时是否抛出异常, 这里选择不需要
                webClient.getOptions().setCssEnabled(false);//是否启用CSS, 因为不需要展现页面, 所以不需要启用
                webClient.getOptions().setJavaScriptEnabled(true); //很重要，启用JS
                webClient.getOptions().setDownloadImages(false);//不下载图片
                webClient.setAjaxController(new NicelyResynchronizingAjaxController());//很重要，设置支持AJAX
                WebRequest request2 = new WebRequest(new URL(downloadOne));
                request2.setAdditionalHeader("User-Agent","Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");
                HtmlPage page = webClient.getPage(request2);
                Document pageBody = Jsoup.parse(page.getBody().asXml());
                //webClient.close();

                String cap = pageBody.getElementsByTag("iframe")
                        .first()
                        .attr("src");
                List<BasicNameValuePair> list = pageBody
                        .body()
                        .getElementsByTag("center")
                        .first()
                        .getElementsByTag("form")
                        .first()
                        .getElementsByTag("input")
                        .stream()
                        .map(
                                e->{
                                    return new BasicNameValuePair(e.attr("name"),
                                            e.attr("value"));
                                }
                        ).toList();
                HttpGet capRequest = new HttpGet(cap);
                capRequest.addHeader("referer","https://vidhidepro.com");
                body = client.execute(capRequest, new BasicHttpClientResponseHandler());
                String recaptchaToken = Jsoup.parse(body)
                        .getElementById("recaptcha-token")
                        .attr("value");
                list = new ArrayList<>(list);
                list.add(new BasicNameValuePair("g-recaptcha-response", recaptchaToken));
                HttpPost request3 = new HttpPost(downloadOne);
                request3.addHeader("referer","https://vidhidepro.com");

                request3.setEntity(new UrlEncodedFormEntity(list));


                body = client.execute(request3, new BasicHttpClientResponseHandler());
                return Jsoup.parse(body)
                        .body()
                        .getElementsByClass("text-center")
                        .last()
                        .getElementsByTag("a")
                        .attr("href");

            }
            return null;

        }

        private static String getDownloadStreamtape(String url) throws IOException, ScriptException {
            if (url.contains("streamtape.com")){
                url = url.replace("streamtape.com/e","streamtape.com/v");
                HttpGet request = new HttpGet(url);
                request.addHeader("referer","https://streamtape.com/accpanel");
                String body = client.execute(request, new BasicHttpClientResponseHandler());
                String script = Jsoup.parse(body).body()
                        .getElementById("norobotlink")
                        .nextElementSibling()
                        .data();
                String links[] = script.split("\n");
                String norobotlink = null;
                for (String link : links) {
                    if (link.contains("norobotlink")){
                        norobotlink = link.split("=",2)[1];;
                    }
                }
                ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("javascript");
                norobotlink = "https:"+scriptEngine.eval(norobotlink).toString() +"&dl=1";
                HttpGet request2 = new HttpGet(norobotlink);
                request2.addHeader("referer","https://streamtape.com/accpanel");
                request2.setConfig(
                        RequestConfig.custom().setRedirectsEnabled(false).build()
                );
                String body2 = client.execute(request2, new HttpClientResponseHandler<String>(){

                    @Override
                    public String handleResponse(ClassicHttpResponse response) throws HttpException, IOException {
                        EntityUtils.consume(response.getEntity());
                        if (response.getCode() == HttpStatus.SC_MOVED_PERMANENTLY ||
                                response.getCode() == HttpStatus.SC_MOVED_TEMPORARILY){

                            return response.getHeader("location").getValue();
                        }

                        throw new HttpResponseException(response.getCode(), response.getReasonPhrase());
                    }
                });
                return body2;
            }
            return null;
            //streamtape.com/get_video?id=jVGm9rYkjRizeXQ&expires=1727847207&ip=FRSNKRSTKxSHDN&token=YVL1YAFVMvoF
        }
    }

}

/**
 *  eval(function(p, a, c, k, e, d) {
 *                             e = function(c) {
 *                                 return (c < a ? '' : e(parseInt(c / a))) + ((c = c % a) > 35 ? String.fromCharCode(c + 29) : c.toString(36))
 *                             }
 *                             ;
 *                             if (!''.replace(/^/, String)) {
 *                                 while (c--) {
 *                                     d[e(c)] = k[c] || e(c)
 *                                 }
 *                                 k = [function(e) {
 *                                     return d[e]
 *                                 }
 *                                 ];
 *                                 e = function() {
 *                                     return '\\w+'
 *                                 }
 *                                 ;
 *                                 c = 1
 *                             }
 *                             ;while (c--) {
 *                                 if (k[c]) {
 *                                     p = p.replace(new RegExp('\\b' + e(c) + '\\b','g'), k[c])
 *                                 }
 *                             }
 *                             return p
 *                         }('1k(1j()&&1i()){$(1(){$(\'#4\').P(\'3\',1(){$(\'#4\').6(m)});$(\'.m\').3(1(){$(\'#4\').6(m)});$(\'.r\').3(1(){$(\'#4\').6(r)});$(\'.o\').3(1(){$(\'#4\').6(o)});$(\'.n\').3(1(){$(\'#4\').6(n)});$(\'#4\').P(\'3\',1(){$(\'#7\').6(x)});$(\'.m\').3(1(){$(\'#7\').6(x)});$(\'.r\').3(1(){$(\'#7\').6(1b)});$(\'.o\').3(1(){$(\'#7\').6(16)});$(\'.n\').3(1(){$(\'#7\').6(V)});$(\'#A l\').3(1(){8 z=$(\'#A l\').z(y);$(\'#I H\').G(\'F\',\'1h\');$(\'#I H\').1g(z).G(\'F\',\'1f\');$(\'#A l\').B(\'u-w\');$(y).C(\'u-w\')});$(\'#D l\').3(1(){$(\'#D l\').B(\'u-w\');$(y).C(\'u-w\')});8 m=\'<a j="5://12.E/t/1c" h="g" 9="10"><14 p="5://18.1d.R.J/19/1e/O/N/M.L" /><i 9="2 2-K" b-c="k"></i></a>\';8 x=\'q.<a j="5://12.E/d/1c" h="g"><i 9="2 2-7" b-c="k"></i></a>\';8 r=\'<a j="5://1a.Q/e/17" h="g" 9="10"><14 p="5://18.1d.R.J/19/1e/O/N/M.L" /><i 9="2 2-K" b-c="k"></i></a>\';8 1b=\'q.<a j="5://1a.Q/f/17" h="g"><i 9="2 2-7" b-c="k"></i></a>\';8 o=\'<a j="5://15.s/v/S" h="g" 9="10"><14 p="5://18.1d.R.J/19/1e/O/N/M.L" /><i 9="2 2-K" b-c="k"></i></a>\';8 16=\'q.<a j="5://15.s/d/S" h="g"><i 9="2 2-7" b-c="k"></i></a>\';8 n=\'<a j="5://U.s/e/T" h="g" 9="10"><14 p="5://18.1d.R.J/19/1e/O/N/M.L" /><i 9="2 2-K" b-c="k"></i></a>\';8 V=\'q.<a j="5://U.s/v/T" h="g"><i 9="2 2-7" b-c="k"></i></a>\'})}1l{$(1(){$(\'#4\').P(\'3\',1(){$(\'#4\').6(m)});$(\'.m\').3(1(){$(\'#4\').6(m)});$(\'.r\').3(1(){$(\'#4\').6(r)});$(\'.o\').3(1(){$(\'#4\').6(o)});$(\'.n\').3(1(){$(\'#4\').6(n)});$(\'#4\').P(\'3\',1(){$(\'#7\').6(x)});$(\'.m\').3(1(){$(\'#7\').6(x)});$(\'.r\').3(1(){$(\'#7\').6(1b)});$(\'.o\').3(1(){$(\'#7\').6(16)});$(\'.n\').3(1(){$(\'#7\').6(V)});$(\'#A l\').3(1(){8 z=$(\'#A l\').z(y);$(\'#I H\').G(\'F\',\'1h\');$(\'#I H\').1g(z).G(\'F\',\'1f\');$(\'#A l\').B(\'u-w\');$(y).C(\'u-w\')});$(\'#D l\').3(1(){$(\'#D l\').B(\'u-w\');$(y).C(\'u-w\')});8 m=\'<4 13="11" p="5://12.E/t/1c" Z="0" Y="X" W></4>\';8 x=\'q.<a j="5://12.E/d/1c" h="g"><i 9="2 2-7" b-c="k"></i></a>\';8 r=\'<4 13="11" p="5://1a.Q/e/17" Z="0" Y="X" W></4>\';8 1b=\'q.<a j="5://1a.Q/f/17" h="g"><i 9="2 2-7" b-c="k"></i></a>\';8 o=\'<4 13="11" p="5://15.s/v/S" Z="0" Y="X" W></4>\';8 16=\'q.<a j="5://15.s/d/S" h="g"><i 9="2 2-7" b-c="k"></i></a>\';8 n=\'<4 13="11" p="5://U.s/e/T" Z="0" Y="X" W></4>\';8 V=\'q.<a j="5://U.s/v/T" h="g"><i 9="2 2-7" b-c="k"></i></a>\'})}', 62, 84, '|function|fa|click|iframe|https|html|download|var|class||aria|hidden||||_blank|target||href|true|span|e1s1|e1s4|e1s3|src|DL|e1s2|com||tab||select|e1s1d|this|index|episode|removeClass|addClass|e1|mov|display|css|div|server|jp|play|jpg|dvmm152pl|dvmm152|adult|one|to|co|s47zi62pueg4|wGjKx91R09tJ1q2|streamtape|e1s4d|allowfullscreen|no|scrolling|frameborder|codeX|embed|spankbang|id|img|vidhidepro|e1s3d|xxgr2mq6umt7|pics|mono|streamwish|e1s2d|66fa7db574159|dmm|movie|block|eq|none|ChromeCheck|UACheck|if|else'.split('|'), 0, {}))
 */

//https://streamtape.com/get_video?id=jVGm9rYkjRizeXQ&expires=1727848132&ip=FRSNKRSTKxSHDN&token=uL6iwwXcJECL
//https://streamtape.com/get_video?id=jVGm9rYkjRizeXQ&expires=1727848132&ip=FRSNKRSTKxSHDN&token=uL6iwwXcJEzZ&dl=1