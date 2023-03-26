package com.hps.botrunningsysytem.service.impl.utils;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BotPool extends Thread {

    private final ReentrantLock lock = new ReentrantLock();

    private final Condition condition = lock.newCondition();

    //bots队列公共变量，读写注意加锁
    private final Queue<Bot> bots = new LinkedList<>();

    //向队列添加元素
    public void addBot(Integer userId, String botCode, String input){
       lock.lock();
       try {
            bots.add(new Bot(userId, botCode, input));
            condition.signalAll();                    //唤醒
       }finally {
           lock.unlock();
       }
    }
    /**
     * 动态执行bot的代码
     * @param bot
     */
    private void consume(Bot bot){
        Consumer consumer = new Consumer();
        consumer.startTimeout(2000,bot);

    }


    @Override
    public void run() {

        //手动实现一个消息队列
        while (true){
            lock.lock();
            if(bots.isEmpty()){
                try{
                    condition.await();   //阻塞
                }catch (InterruptedException e){
                    e.printStackTrace();
                    lock.unlock();
                    break;
                }
            }else {
                Bot bot = bots.remove();
                lock.unlock();
                consume(bot);   //比较耗时，可能要执行几秒钟 若此时再来任务，存到bots队列中
            }
        }
    }


}
