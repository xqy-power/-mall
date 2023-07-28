package com.xqy.gulimall.search.thread;

/**
 * @author xqy
 */
public class ThreadTest {
    public static void main(String[] args) {
//        1）、继承 Thread
        Thread01 thread01 = new Thread01();
        thread01.start();
        System.out.println("运行结束");
//        2）、实现 Runnable 接口

//        3）、实现 Callable 接口 + FutureTask （可以拿到返回结果，可以处理异常）
//        4）、线程池


    }

    public static class Thread01 extends Thread {
        @Override
        public void run() {
            System.out.println("当前线程" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果" + i);
        }
    }

    public static class runnable implements Runnable {
        @Override
        public void run() {
            System.out.println("当前线程" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果" + i);
        }
    }
}
