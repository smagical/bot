package io.github.smagical.bot.hanlp;

import jep.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HanLp2 implements AutoCloseable{
    private static String pythonHome;
    private static PyConfig pyConfig;
    public static final String COARSE_ELECTRA_SMALL_ZH = "hanlp.pretrained.tok.COARSE_ELECTRA_SMALL_ZH";
    public static final String FINE_ELECTRA_SMALL_ZH = "hanlp.pretrained.tok.FINE_ELECTRA_SMALL_ZH";
    public static final String UD_TOK_MMINILMV2L6 = "hanlp.pretrained.tok.UD_TOK_MMINILMV2L6";
    public static final String UD_TOK_MMINILMV2L12 = "hanlp.pretrained.tok.UD_TOK_MMINILMV2L12";
    public static final String LARGE_ALBERT_BASE = "hanlp.pretrained.tok.LARGE_ALBERT_BASE";
    public static final String CTB9_TOK_ELECTRA_SMALL = "hanlp.pretrained.tok.CTB9_TOK_ELECTRA_SMALL";
    public static final String CTB9_TOK_ELECTRA_BASE_CRF = "hanlp.pretrained.tok.CTB9_TOK_ELECTRA_BASE_CRF";

    private static final Set<String> sets =
            Set.of(
                    COARSE_ELECTRA_SMALL_ZH,FINE_ELECTRA_SMALL_ZH,
                    UD_TOK_MMINILMV2L6,UD_TOK_MMINILMV2L12,
                    LARGE_ALBERT_BASE,CTB9_TOK_ELECTRA_SMALL,
                    CTB9_TOK_ELECTRA_BASE_CRF
            );


    static {
        HanLp2.pythonHome = System.getenv("PYTHONHOME");
        pyConfig = new PyConfig();
        pyConfig.setPythonHome(pythonHome);
        MainInterpreter.setInitParams(pyConfig);
    }
    public static void setPythonHome(String pythonHome) {
        HanLp2.pythonHome = pythonHome;
        pyConfig = new PyConfig();
        pyConfig.setPythonHome(pythonHome);
        MainInterpreter.setInitParams(pyConfig);
    }


    private ThreadLocal<Jep> interpreter;
    public HanLp2(String modelPath) {
        interpreter = new ThreadLocal<>(){
            @Override
            protected Jep initialValue() {
                SharedInterpreter interpreter = new SharedInterpreter();
                 interpreter.exec("import hanlp");
                if (sets.contains(modelPath)) {
                    interpreter.exec("modelPath = "+modelPath);
                }else {
                    interpreter.set("modelPath", modelPath.replace("\\","/"));
                }
                interpreter.exec("model = hanlp.load(modelPath)");
                interpreter.exec("model.config.output_spans = True");
                interpreter.exec("hanlp2 = hanlp.pipeline().append(hanlp.utils.rules.split_sentence).append(model).append(lambda sents: sum(sents, []))");
                return interpreter;
            }

            @Override
            public void remove() {
                get().close();
            }
        };

    }

    public HanLp2() {
        this(HanLp2.FINE_ELECTRA_SMALL_ZH);
    }

    public List<Term> seg(String text) {
        List<String> list = new ArrayList<String>();
        Object object = interpreter.get().invoke("hanlp2",text);
        List<List> res = (List<List>) object;

        return res.stream()
                .map(
                        e->new Term(
                                e.get(0).toString(),
                                Integer.parseInt(e.get(1).toString()),
                                Integer.parseInt(e.get(2).toString())
                        )
                ).toList();
    }

    @Override
    public void close() throws Exception {
        interpreter.remove();
        interpreter = null;
    }

    public static class Term{
        public String word;
        public int start;
        public int end;

        public Term(String word, int start, int end) {
            this.word = word;
            this.start = start;
            this.end = end;
        }


        @Override
        public String toString() {
            return "Term{" +
                    "word='" + word + '\'' +
                    ", start=" + start +
                    ", end=" + end +
                    '}';
        }
    }

    public static void main(String[] args) {
        HanLp2.setPythonHome("F:\\python_env\\envs\\hanlp");
        HanLp2 hanlp = new HanLp2();
        System.out.println(
                hanlp.seg("sajdiaos哈哈哈")
        );
    }

}
