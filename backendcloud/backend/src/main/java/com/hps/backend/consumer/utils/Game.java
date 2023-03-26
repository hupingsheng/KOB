package com.hps.backend.consumer.utils;

import com.alibaba.fastjson2.JSONObject;
import com.hps.backend.consumer.WebSocketServer;
import com.hps.backend.mapper.RecordMapper;
import com.hps.backend.pojo.Record;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Game extends Thread {

    private final Integer rows, cols, inner_walls_count;    //行数，列数，墙的数量

    private final int[][] g;  //地图的每个格子  0表示空地， 1表示障碍物

    private final static int[] dx = {-1, 0, 1, 0}, dy = {0, 1, 0, -1};

    private final Player playerA, playerB;

    //记录玩家的下一步操作方向 初始为空
    private Integer nextStepA = null;
    private Integer nextStepB = null;

    private ReentrantLock lock = new ReentrantLock();

    private String status = "playing";      //playing -> finished

    private String loser = "";        //all:平局  A : A输      B： B输


    public Player getPlayerA() {
        return playerA;
    }

    public Player getPlayerB() {
        return playerB;
    }


    public void setNextStepA(Integer nextStepA) {
        lock.lock();
        try {
            this.nextStepA = nextStepA;
        } finally {
            lock.unlock();
        }
    }

    public void setNextStepB(Integer nextStepB) {
        lock.lock();
        try {
            this.nextStepB = nextStepB;
        } finally {
            lock.unlock();
        }
    }


    public Game(Integer rows, Integer cols, Integer inner_walls_count, Integer idA, Integer idB) {
        this.rows = rows;
        this.cols = cols;
        this.inner_walls_count = inner_walls_count;
        this.g = new int[rows][cols];
        playerA = new Player(idA, rows - 2, 1, new ArrayList<>());
        playerB = new Player(idB, 1, cols - 2, new ArrayList<>());
    }

    public int[][] getG() {
        return g;
    }

    /**
     * [sx, sy]:起点  [tx, ty]:终点  判断连通性
     *
     * @param sx
     * @param sy
     * @param tx
     * @param ty
     * @return
     */
    public boolean check_connectivity(int sx, int sy, int tx, int ty) {
        if (sx == tx && sy == ty) return true;
        g[sx][sy] = 1;

        for (int i = 0; i < 4; i++) {
            int x = sx + dx[i], y = sy + dy[i];
            if (x >= 0 && x < this.rows && y >= 0 && y <= this.cols && g[x][y] == 0) {
                if (check_connectivity(x, y, tx, ty)) {
                    g[sx][sy] = 0;   //恢复现场
                    return true;
                }
            }
        }
        g[sx][sy] = 0;
        return false;
    }

    /**
     * 构造一个满足连通性的地图
     *
     * @return
     */
    private boolean draw() {         //画地图
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                g[i][j] = 0;
            }
        }

        //给四周加上障碍物
        for (int r = 0; r < this.rows; r++) {
            g[r][0] = g[r][this.cols - 1] = 1;
        }
        for (int c = 0; c < this.cols; c++) {
            g[0][c] = g[this.rows - 1][c] = 1;
        }

        Random random = new Random();
        for (int i = 0; i < this.inner_walls_count / 2; i++) {
            for (int j = 0; j < 1000; j++) {
                int r = random.nextInt(this.rows);
                int c = random.nextInt(this.cols);

                if (g[r][c] == 1 || g[this.rows - 1 - r][this.cols - 1 - c] == 1) continue;
                if (r == this.rows - 2 && c == 1 || r == 1 && c == this.cols - 2) continue;

                g[r][c] = g[this.rows - 1 - r][this.cols - 1 - c] = 1;
                break;
            }
        }

        return check_connectivity(this.rows - 2, 1, 1, this.cols - 2);

    }

    /**
     * 1000次创建地图的机会
     */
    public void createMap() {
        //1000次画图机会
        for (int i = 0; i < 1000; i++) {
            if (draw()) {
                break;
            }
        }
    }

    /**
     * 等待两名玩家的下一步操作
     *
     * @return
     */
    private boolean nextStep() {

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < 50; i++) {
            try {
                Thread.sleep(100);
                lock.lock();
                try {
                    if (nextStepA != null && nextStepB != null) {
                        playerA.getSteps().add(nextStepA);
                        playerB.getSteps().add(nextStepB);
                        return true;
                    }
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }


    public String getMapString() {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                res.append(g[i][j]);
            }
        }
        return res.toString();
    }

    /**
     * 将对局结果保存到数据库
     */
    public void saveToDataBase() {
        Record record = new Record(
                null,
                playerA.getId(),
                playerA.getSx(),
                playerA.getSy(),
                playerB.getId(),
                playerB.getSx(),
                playerB.getSy(),
                playerA.getStepsString(),
                playerB.getStepsString(),
                getMapString(),
                loser,
                new Date()
        );

        WebSocketServer.recordMapper.insert(record);
    }

    /**
     * 向前端发送结果
     */
    private void sendResult() {
        JSONObject resp = new JSONObject();
        resp.put("event", "result");
        resp.put("loser", loser);
        saveToDataBase();
        sendAllMessage(resp.toJSONString());
    }

    /**
     * 向两个client传递移动信息
     */
    private void sendMove() {
        lock.lock();
        try {
            JSONObject resp = new JSONObject();
            resp.put("event", "move");
            resp.put("a_direction", nextStepA);
            resp.put("b_direction", nextStepB);
            sendAllMessage(resp.toJSONString());
            nextStepA = nextStepB = null;


        } finally {
            lock.unlock();
        }
    }

    /**
     * 判断玩家A的下一步操作是否合法
     *
     * @param cellsA
     * @param cellsB
     * @return
     */
    private boolean check_valid(List<Cell> cellsA, List<Cell> cellsB) {
        int n = cellsA.size();
        Cell cell = cellsA.get(n - 1);      //蛇头
        if (g[cell.x][cell.y] == 1) return false;         //判断蛇头是否撞到墙

        //蛇的下一步操作撞到A的蛇身
        for (int i = 0; i < n - 1; i++) {
            if (cellsA.get(i).x == cell.x && cellsA.get(i).y == cell.y) return false;
        }
        //蛇的下一步操作撞到B的蛇身
        for (int i = 0; i < n - 1; i++) {
            if (cellsB.get(i).x == cell.x && cellsB.get(i).y == cell.y) return false;
        }
        return true;

    }

    /**
     * 判断两名玩家下一步操作是否合法
     */
    private void judge() {
        List<Cell> cellsA = playerA.getCells();
        List<Cell> cellsB = playerB.getCells();

        boolean validA = check_valid(cellsA, cellsB);
        boolean validB = check_valid(cellsB, cellsA);
        if (!validA || !validB) {
            status = "finished";

            if (!validA && !validB) {
                loser = "all";
            } else if (!validA) {
                loser = "A";
            } else {
                loser = "B";
            }
        }


    }

    /**
     * 向两个client发送结果信息
     *
     * @param message
     */
    private void sendAllMessage(String message) {
        if (WebSocketServer.users.get(playerA.getId()) != null) {
            WebSocketServer.users.get(playerA.getId()).sendMessage(message);
        }
        if (WebSocketServer.users.get(playerB.getId()) != null) {
            WebSocketServer.users.get(playerB.getId()).sendMessage(message);
        }
    }

    /**
     * 线程的入口函数
     * 等待用户输入的下一步操作
     */
    @Override
    public void run() {
        for (int i = 0; i < 1000; i++) {
            if (nextStep()) {     //是否获取两条蛇的下一步
                judge();

                if (status.equals("playing")) {
                    sendMove();
                } else {
                    sendResult();
                    break;
                }
            } else {
                status = "finished";
                lock.lock();
                try {
                    if (nextStepA == null && nextStepB == null) {
                        loser = "all";
                    } else if (nextStepA == null) {
                        loser = "A";
                    } else {
                        loser = "B";
                    }
                } finally {
                    lock.unlock();
                }
                sendResult();

                break;
            }
        }
    }
}
