package io.github.smagical.bot.plugin.util;

import com.hankcs.hanlp.dictionary.other.CharType;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.Viterbi.ViterbiSegment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SegUtil {


    private final static class Seg{
        private  static Seg seg;
        private Segment segment;
        private Seg(){
           segment = new ViterbiSegment(){{
                CharType.type['-'] = CharType.CT_DELIMITER;
            }}.enableIndexMode(1)
                    .enableOffset(true)
                    .enableMultithreading(Runtime.getRuntime().availableProcessors()/2<=0?1:Runtime.getRuntime().availableProcessors()/2)
                    .enableAllNamedEntityRecognize(true)
                    .enablePartOfSpeechTagging(true)
                    .enableCustomDictionary(true)
                    .enableNormalization(true)
                    .enablePartOfSpeechTagging(true)
                    .enableCustomDictionaryForcing(true);
        }
        private final static Seg getInstance(){
            if(seg==null){
                synchronized (Seg.class){
                    if (seg==null){
                        seg = new Seg();
                    }
                }
            }
            return seg;
        }

        public Segment getSegment() {
            return segment;
        }
    }

    public static List<String> seqByHanLP(String sentence) {
        return Seg.getInstance().getSegment().seg(sentence).stream()
                .map(term -> term.word).
                collect(Collectors.toList());
    }

    public static  List<String> seqByAll(String sentence) {
        List<String> result = new ArrayList<>(sentence.length());
        for (char c : sentence.toCharArray()) {
            if (c == '|' || c == '&') result.add(String.valueOf("\\"+c));
            else result.add(String.valueOf(c));
        }
        return result;
    }

    public static  String concat(List<String> sentences,String concatWord) {
        return  sentences
                .stream()
                .reduce((a,b)->a+concatWord+b)
                .orElse("");

    }


}
