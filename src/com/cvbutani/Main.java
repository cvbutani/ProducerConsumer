package com.cvbutani;

/**
 * Project name: ProducerConsumer
 * Created by: cvbutani
 * Date: 28/04/18
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

import static com.cvbutani.Main.EOF;

public class Main {

    public static final String EOF = "EOF";

    public static void main(String[] args) {
//        List<String> buffer = new ArrayList<>();
        ArrayBlockingQueue<String> buffer = new ArrayBlockingQueue<String>(5);

        //  ArrayBlockingQueue : while using this package, you need to remove ReentrantLock if you are using it.
        //  It will replace Arraylist/List and ReentrantLock.

        ReentrantLock bufferLock = new ReentrantLock();

        // ReenterantLock : use it when you actually need something it provides that synchronized doesn't, like timed lock waits,
        // interruptible lock waits, non-block-structured locks, multiple condition variables, or lock polling.
        // ReentrantLock also has scalability benefits, and you should use it if you actually have a situation that exhibits high
        // contention, but remember that the vast majority of synchronized blocks hardly ever exhibit any contention, let alone
        // high contention. I would advise developing with synchronization until synchronization has proven to be inadequate,
        // rather than simply assuming "the performance will be better" if you use ReentrantLock. Remember,
        // these are advanced tools for advanced users. (And truly advanced users tend to prefer the simplest tools they can find
        // until they're convinced the simple tools are inadequate.) As always, make it right first, and then worry about
        // whether or not you have to make it faster.

        ExecutorService ex = Executors.newFixedThreadPool(3);
        MyProducer producer = new MyProducer(buffer, ThreadColor.ANSI_BLUE);
        MyConsumer consumer1 = new MyConsumer(buffer, ThreadColor.ANSI_RED);
        MyConsumer consumer2 = new MyConsumer(buffer, ThreadColor.ANSI_GREEN);

        ex.execute(producer);
        ex.execute(consumer1);
        ex.execute(consumer2);
        Future<String> future = ex.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                System.out.println(ThreadColor.ANSI_CYAN + "Collable class is being interrupted");
                return "This is callable result";
            }
        });

        try {
            System.out.println(future.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        ex.shutdown();

//        new Thread(producer).start();
//        new Thread(consumer1).start();
//        new Thread(consumer2).start();
    }
}

class MyProducer implements Runnable {

    private ArrayBlockingQueue<String> buffer;
    private String color;


    public MyProducer(ArrayBlockingQueue<String> buffer, String color) {
        this.buffer = buffer;
        this.color = color;
    }

    @Override
    public void run() {
        Random random = new Random();
        String[] nums = {"1", "2", "3", "4", "5"};

        for (String num : nums) {
            try {
                System.out.println(color + "Adding: " + num);
                buffer.put(num);
                Thread.sleep(random.nextInt(1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(color + "Adding color and EOF");

        try {
            buffer.put("EOF");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class MyConsumer implements Runnable {
    private ArrayBlockingQueue<String> buffer;
    private String color;

    public MyConsumer(ArrayBlockingQueue<String> buffer, String color) {
        this.buffer = buffer;
        this.color = color;
    }

    @Override
    public void run() {

        while (true) {
            synchronized (buffer) {
                try {
                    if (buffer.isEmpty()) {
                        continue;
                    }
                    if (buffer.peek().equals(EOF)) {
                        System.out.println(color + "Exiting");
                        break;
                    } else {
                        System.out.println(color + "Removed: " + buffer.take());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
