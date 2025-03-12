package io.github.smagical.bot.plugin.handler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageHelper<T> {

    private int page = 0;
    private int pageSize = 10;
    private List<T> messages = new ArrayList<>();
    private int total = 0;

    public boolean hasPre(){
        return page > 0;
    }
    public boolean hasNext(){
        return page * pageSize + pageSize < total;
    }
    public int nextPage(){
        if(hasNext()){
            return page + 1;
        }
        return page;
    }
    public int prevPage(){
        if(hasPre()){
            return page - 1;
        }
        return page;
    }
    public int getFristPage(){
        return  0;
    }
    public int getLastPage(){
        if (pageSize <= 0) return 0;
        return total/pageSize;
    }
    public int getOffset(){
        return page*pageSize;
    }

}
