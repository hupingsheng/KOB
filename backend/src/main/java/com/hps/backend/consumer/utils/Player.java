package com.hps.backend.consumer.utils;

import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    //玩家id   玩家的起始位置sx,sy
    private Integer id;
    private Integer sx;
    private Integer sy;

    //玩家的移动方向的历史序列
    private List<Integer> steps;

    /**
     * 检查当前回合，蛇的长度是否增加
     *
     * @return
     */
    public boolean check_tail_increasing(int step) {
        if (step <= 10) return true;
        return step % 3 == 1;
    }

    /**
     * 更新蛇的身体
     * @return
     */
    public List<Cell> getCells() {
        List<Cell> res = new ArrayList<>();

        int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};
        int x = sx, y = sy;
        int step = 0;
        res.add(new Cell(x,y));
        for(int d : steps){
            x += dx[d];
            y += dy[d];
            res.add(new Cell(x,y));
            if(!check_tail_increasing(++step)){
                res.remove(0);
            }
        }

        return res;
    }

    public String getStepsString(){
        StringBuilder res = new StringBuilder();
        for(int d : steps){
            res.append(d);
        }
        return res.toString();
    }

}
