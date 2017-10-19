package com.jp.queue;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

/**
 * @desc 该类主要模拟工厂仓库类
 * 利用多线程+LinkedBlockingQueue阻塞队列实现生产者消费者模式
 * @class com.jp.queue.QueueMain
 * @author zhanghengyu
 * @time 2017-10-17 下午2:19
 */
public class MarketStorage {

    /**创建队列作为产品容器*/
    private static LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>(300);

    //生产线程
    private static ThreadPoolExecutor productExecutor = new ThreadPoolExecutor(3,3,20, TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(300));

    //消费线程
    private static ThreadPoolExecutor customExecutor = new ThreadPoolExecutor(3,5,20, TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(300));

    public static boolean PRODUCTOR_ORDER = true;   //生产命令

    public static boolean CUSTOMER_ORDER = true ; // 消费命令

    //启动生产
    public static void runProducer(){
        for (int i=0;i<10;i++){
            productExecutor.submit(new Producer("生产者"+i,queue));
        }
    }
    //停止生产
    public static void stopProducer(){
        if (!productExecutor.isShutdown()){
            PRODUCTOR_ORDER = false;
            productExecutor.shutdown();
        }
    }
    //开启消费
    public static void runCustom(){
        for (int i=0;i<9;i++){
            customExecutor.submit(new Customer("消费者"+i,queue));
        }
    }

    //停止消费
    public static void stopCustom(){
        if (!customExecutor.isShutdown()){
            CUSTOMER_ORDER = false;
            customExecutor.shutdown();
        }
    }


    /**
     * @desc
     * @class QueueMain
     * @param
     * @result
     * @author zhanghengyu
     * @time 2017-10-17
     */
    public static void main(String[] args){
        runProducer();   //开启生产
        runCustom();  //开启消费

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println(new Date()+"任务执行");
                stopProducer();
                stopCustom();
            }
        },30000);
    }

}

/**
 * @desc
 * @class com.jp.queue.MarketStorage
 * @author zhanghengyu
 * @time 17-10-18 上午9:48
 */
class Producer implements Runnable{
    private String name;  //生产这名称
    private BlockingQueue<String> queue;  //商品仓库
    public Producer(String name,BlockingQueue<String> queue){
        this.name = name;
        this.queue = queue;
    }

    public void run() {
        while (MarketStorage.PRODUCTOR_ORDER) {
            //使用队列的offer方法向队列中添加，返回true/false 当队列放满时将阻塞5秒，5秒后无响应将返回false
            boolean flag = false;
            try {
                int r = (int) (Math.random() * 1000);
                flag = queue.offer("线程【" + Thread.currentThread().getId() + "】，生产者：【" + this.name + "】 生产产品" +r ,Long.valueOf(5+""),TimeUnit.SECONDS);
                System.out.println("线程【" + Thread.currentThread().getId() + "】，生产者：【" + this.name + "】 生产完成。。。。");
                if (!flag){
                    System.out.println("仓库已满，入库失败！");
                }
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
/**
 * @desc
 * @class com.jp.queue.MarketStorage.
 * @param  * @param null
 * @result
 * @author zhanghengyu
 * @time 17-10-18 上午9:48
 */
class Customer implements Runnable{
    private String name;  //消费者名称
    private BlockingQueue<String> queue;  //商品仓库
    public Customer(String name,BlockingQueue<String> queue){
        this.name = name;
        this.queue = queue;
    }
    public void run() {
        while (MarketStorage.CUSTOMER_ORDER) {
            try {
                //使用poll方法，当队列为空时将等待3秒，3秒后返回空值
                String p = queue.poll(3,TimeUnit.SECONDS);
                if (p != null) {
                    System.out.println("线程【" + Thread.currentThread().getId() + "】，消费者：【" + this.name + "】 消费 产品【" + p + "】。。。。");
                }else{
                    System.out.println("仓库已空！");
                }
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

