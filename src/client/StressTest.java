package client;

import java.util.concurrent.CountDownLatch;

public class StressTest {
    public static void main(String[] args) throws Exception {
        CountDownLatch latch = new CountDownLatch(20);
        long startTime = System.currentTimeMillis();
        for(int i=0;i<20;i++){
            System.out.println("开始测试第 " + (i+1) + " 个实例");
            new Thread(() -> {
                try {
                    Client.sendGet("/");
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        latch.await();
        long endTime = System.currentTimeMillis();
        System.out.println();
        System.out.println("总用时: " + (endTime - startTime)/1000.0 + " 秒");
    }
}