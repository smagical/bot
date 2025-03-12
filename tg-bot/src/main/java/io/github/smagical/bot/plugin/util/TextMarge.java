package io.github.smagical.bot.plugin.util;

import java.io.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TextMarge {
    public static void main(String[] args) throws InterruptedException, IOException {
//        solve(100000,"test_3.txt");
//        BufferedReader reader = new BufferedReader(new FileReader("E:\\Code\\JAVA\\bot\\tg\\src\\main\\resources\\dict.txt"));
//        String line = null;
//        BufferedWriter writer = new BufferedWriter(new FileWriter("E:\\Code\\JAVA\\bot\\tg\\src\\main\\resources\\dict2.txt",false));
//        while ((line = reader.readLine()) != null) {
//            String[] tokens = line.split("\t");
//            while (tokens[0].length() > 0){
//                if (tokens[0].strip().startsWith("；")) {
//                    int  b=  1;
//                }
//
//                if (String.valueOf(tokens[0].charAt(0)).matches("[\\pP]"))
//                    tokens[0]= tokens[0].substring(1).strip();
//                else
//                    break;
//            }
//            if (tokens[0].length() <= 0) continue;
//            writer.write(HanLP.convertToSimplifiedChinese(tokens[0].toLowerCase()));
//            writer.write(" ");
//            writer.write("n");
//            writer.write(" ");
//            writer.write(tokens[1]);
//            writer.newLine();
//        }
//        writer.close();
//        solve(100000,"test_1.txt");
//        BufferedReader reader = new BufferedReader(new FileReader("test_1.txt"));
//        String line = null;
//        BufferedWriter writer = new BufferedWriter(new FileWriter("test_3.txt",false));
//        while ((line = reader.readLine())!=null){
//            line = line.replaceAll("\\s","");
//            if (line.contains("#")) {
//                String[] lines = line.split("#");
//                for (String word : lines) {
//                    if (!word.isBlank() && !word.equals("#")){
//                        writer.write(word);
//                        writer.newLine();
//                    }
//                }
//                continue;
//            }
//            int len = line.length();
//            for (int i = 0; i < len; i++) {
//                for (int j = i+1; j < len; j++) {
//                    String word = line.substring(i, j);
//                        if (!word.isBlank() && !word.matches("[-+*#]")){
//                            writer.write(word);
//                            writer.newLine();
//                        }
//                    }
//                }
//        }
//        writer.flush();
//        writer.close();

        try (BufferedReader reader = new BufferedReader(new FileReader("E:\\Code\\JAVA\\bot\\dll\\data\\dictionary\\CoreNatureDictionary.txt"));
            BufferedWriter writer = new BufferedWriter(
                    new FileWriter("E:\\Code\\JAVA\\bot\\vv\\data\\lex-av.lex",true)
            )

        ){
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] split = line.split("\t");
                String name = split[0].trim();
                while (name.startsWith("#")){
                    name = name.substring(1);
                }
                while (name.startsWith("/")){
                    name = name.substring(1);
                }
                while (name.startsWith("＃")){
                    name = name.substring(1);
                }
                while (name.startsWith("！")){
                    name = name.substring(1);
                }
                while (name.startsWith("!")){
                    name = name.substring(1);
                }
                while (name.startsWith("(")){
                    name = name.substring(1);
                }
                while (name.startsWith("（")){
                    name = name.substring(1);
                }
                if (name.length() > 0){
                    name += "/null";
                    writer.write(name);
                    writer.newLine();
                }
            }
            writer.flush();
        }

    }

    private final static String INDEX_SUBFIX = ".index.txt";
    private final static String SPLIT = "\t";
    private final static int order = -1;

    public static void solve(int limt,String... path) throws InterruptedException {
        String data = "vv/data";
        String tmp = "vv/tmp";
        File file = new File(tmp);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
        if (!file.exists()) {
            file.mkdirs();
        }
        file = new File(data);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
        if (!file.exists()) {
            file.mkdirs();
        }
        ExecutorService service = Executors.newFixedThreadPool(16);
        for (String s : path) {
            spiltFile(s,data,limt);
        }
        margeBaseHome(service,data,limt);
        service.shutdown();
    }


    public static void spiltFile(String path,String baseHome,int limit){
        try{

            TreeMap<String, Integer> index = new TreeMap<>(
                    (a,b)-> String.CASE_INSENSITIVE_ORDER.compare(a,b)
            );
            int size = 1;
            BufferedReader reader = new BufferedReader(new FileReader(path));

            String line;

            while ((line = reader.readLine()) != null) {
                int count = 1;
                line = line.trim();
                if (line.contains("\t")) {
                    String[] split = line.split("\t");
                    count = Integer.parseInt(split[1].trim());
                    line = split[0].trim();
                }
                index.put(line, index.getOrDefault(line, 0) + count);
                testMemory(index,baseHome,limit);

            }
            if (!index.isEmpty()){
                save(index, baseHome);
            }
            reader.close();

        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }
    }

    public static void margeBaseHome(ExecutorService executorService,String baseHome,int limit) throws InterruptedException {
        File[] files = new File(baseHome).listFiles(
                new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(INDEX_SUBFIX);
                    }
                }
        );
        TreeMap<String, String[]> index = new TreeMap<>();
        for (File indexFile : files) {
            try (BufferedReader reader = new BufferedReader(new FileReader(indexFile))){
                String info = reader.readLine();
                String[] infos = info.split(SPLIT);
                String fileName = infos[0].trim();
                String start = infos[2].trim();
                String end = infos[3].trim();
                index.put(start, new String[]{start,end,fileName});
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            }catch (NumberFormatException e) {}
        }
        String[] lastFile = null;
        if (index.size() <= 1) return;
        File tmpFile = new File(baseHome,"tmp");
        if (!tmpFile.exists()) {
            tmpFile.mkdirs();
        }
        List<Runnable> runnables = new ArrayList<>();
        while (!index.isEmpty()){
            if (lastFile != null) {
                final String[] now = index.pollFirstEntry().getValue();
                if (now[0].compareTo(lastFile[1]) * order < 0) {
                   final String[] finalLastFile = lastFile;
                    runnables.add(new Runnable() {
                       @Override
                       public void run() {
                           margeFile(finalLastFile[2],now[2],tmpFile.getPath(),limit);
                       }
                   });
                   lastFile = null;
                }else {
                    lastFile = now;
                }

            }else {
                lastFile = index.pollFirstEntry().getValue();
            }

        }
        if (runnables.isEmpty())
            return;
        CountDownLatch countDownLatch = new CountDownLatch(runnables.size());
        for (Runnable runnable : runnables) {
            executorService.execute(
                    new Runnable() {
                        @Override
                        public void run() {
                            runnable.run();
                            countDownLatch.countDown();
                        }
                    }
            );
        }
        countDownLatch.await();
        moveToOrgin(tmpFile.getPath(),baseHome);
        margeSizeSmail(executorService,baseHome,limit);
        System.out.println("marge ...............");
        margeBaseHome(executorService,baseHome,limit);
    }

    public static void margeFile(String path,String path2,String baseHome,int limit){
        try {
            TreeMap<String, Integer> index = new TreeMap<>(
                    (a,b)-> String.CASE_INSENSITIVE_ORDER.compare(a,b) * order
            );
            File file1 = new File(path);
            if ((file1.exists() && file1.isDirectory())) {
                return;
            }

            File file2 = new File(path2);
            if ((file2.exists() && file2.isDirectory())) {
                return;
            }

            if (!file1.exists() && !file2.exists()) {
                return;
            }

            if (!file1.exists()) {
                File index2 = new File(file2.getAbsoluteFile()+INDEX_SUBFIX);
                file2.renameTo(new File(baseHome + "/" + file2.getName()));
                if (index2.exists()) {
                    index2.renameTo(new File(baseHome + "/" + index2.getName()));
                }
                return;
            }
            if (!file2.exists()) {
                File index1 = new File(file1.getAbsoluteFile()+INDEX_SUBFIX);
                file1.renameTo(new File(baseHome + "/" + file1.getName()));
                if (index1.exists()) {
                    index1.renameTo(new File(baseHome + "/" + index1.getName()));
                }
                return;
            }

            BufferedReader reader1 = new BufferedReader(new FileReader(file1));
            BufferedReader reader2= new BufferedReader(new FileReader(file2));
            class Pair{
                String text;
                int count = 1;
                static Pair getInstance(String text){
                    if (text == null) return null;
                    Pair pair = new Pair();
                    if (text.contains("\t")){
                        String[] split = text.split("\t");
                        pair.text = split[0];
                        pair.count = Integer.parseInt(split[1]);
                        return pair;
                    }
                    pair.text = text;
                    pair.count = 1;
                    return pair;
                }
            }
            Pair line1 = Pair.getInstance(reader1.readLine());
            Pair line2 = Pair.getInstance(reader2.readLine());
            while (line1!=null && line2 != null){
                if (line1.text.compareTo(line2.text) <= 0){
                    index.put(line1.text, index.getOrDefault(line1.text,0)+line1.count);
                    line1 = Pair.getInstance(reader1.readLine());
                }else {
                    index.put(line2.text, index.getOrDefault(line2.text,0)+line2.count);
                    line2 = Pair.getInstance(reader2.readLine());
                }
               // System.out.println(line1 +" "+line2);
                testMemory(index,baseHome,limit);
            }
            while (line1 != null){
                index.put(line1.text, index.getOrDefault(line1.text,0)+line1.count);
                line1 = Pair.getInstance(reader1.readLine());
                testMemory(index,baseHome,limit);
            }
            while (line2 != null){
                index.put(line2.text, index.getOrDefault(line2.text,0)+line2.count);
                line2 = Pair.getInstance(reader2.readLine());
                testMemory(index,baseHome,limit);
            }
            if (!index.isEmpty()){
                save(index, baseHome);
            }
            reader1.close();
            reader2.close();
            delIndex(file1.getAbsolutePath());
            delIndex(file2.getAbsolutePath());
            file1.delete();
            file2.delete();

        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }
    }

    private static void delIndex(String path){
        File file = new File(path);
        File index1 = new File(file.getAbsoluteFile()+INDEX_SUBFIX);
        if (index1.exists()) {
            index1.delete();
        }
    }

    public static void margeSizeSmail(ExecutorService executorService,String baseHome,int limit) throws InterruptedException{
        margeSizeSmail(executorService,baseHome,limit,Integer.MAX_VALUE);
    }
    public static void margeSizeSmail(ExecutorService executorService,String baseHome,int limit,int deep) throws InterruptedException {
        if (deep <=0 ) return;
        File[] files = new File(baseHome).listFiles(
                new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(INDEX_SUBFIX);
                    }
                }
        );
        TreeMap<Long, String> index = new TreeMap<>();
        for (File indexFile : files) {
            try (BufferedReader reader = new BufferedReader(new FileReader(indexFile))){
                String info = reader.readLine();
                String[] infos = info.split(SPLIT);
                String fileName = infos[0].trim();
                Long lines = Long.parseLong(infos[1].trim());
                index.put(lines,fileName);
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            }catch (NumberFormatException e) {}
        }
        String lastFile = null;
        while (!index.isEmpty()){
            if (index.lastKey() >= limit){
                index.pollLastEntry();
            }else {
                break;
            }
        }
        if (index.size() <= 1){
            return ;
        }
        CountDownLatch countDownLatch = new CountDownLatch(index.size()/2);
        File tmpFile = new File(baseHome,"tmp");
        if (!tmpFile.exists()){
            tmpFile.mkdirs();
        }
        while (index.size() > 1){
            final String frist = index.pollFirstEntry().getValue();
            final String last = index.pollLastEntry().getValue();
            executorService
                    .submit(
                            new Runnable() {
                                @Override
                                public void run() {
                                    margeFile(frist,last,tmpFile.getPath(),limit);
                                    countDownLatch.countDown();
                                }
                            }
                    );
        }
        countDownLatch.await();
        moveToOrgin(tmpFile.getPath(),baseHome);
        margeSizeSmail(executorService,baseHome,limit,deep-1);
    }




    public static boolean testMemory(TreeMap<String,Integer> index,String baseHome,int limit) throws IOException {
        if (index.size() > limit) {
            return save(index,baseHome);
        }
        return false;
    }

    public static boolean save(TreeMap<String,Integer> index,String baseHome) throws IOException {
        String uuid = UUID.randomUUID().toString();
        File file = new File(baseHome, uuid+".txt");
        if (file == null) return false;
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(
                file, false
        ));
        for (Map.Entry<String, Integer> entry : index.entrySet()) {
            writer.write(entry.getKey() + SPLIT + entry.getValue().toString());
            writer.newLine();
        }
        int size = index.size();
        String frist = index.firstKey();
        String last = index.lastKey();
        writer.flush();
        writer.close();
        index.clear();
        writer = new BufferedWriter(new FileWriter(new File(baseHome,file.getName()+INDEX_SUBFIX),true));
        writer.write(file.getPath());
        writer.write(SPLIT);
        writer.write(String.valueOf(size));
        writer.write(SPLIT);
        writer.write(frist);
        writer.write(SPLIT);
        writer.write(last);
        writer.write(SPLIT);
        writer.write(String.valueOf(file.length()));
        writer.newLine();
        writer.flush();
        writer.close();
        return true;
    }

    public static void moveToOrgin(String filePath,String baseHome){
        File file = new File(filePath);
        if (!file.exists()) return;
        if (file.isDirectory()) {
            File[] dir = file.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile();
                }
            });
            for (File file1 : dir) {
                file1.renameTo(new File(baseHome,file1.getName()));
            }
        }else {
            file.renameTo(new File(baseHome,file.getName()));
        }
    }

}
