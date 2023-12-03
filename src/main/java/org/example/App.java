package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.*;

/**
 * Hello world!
 *
 */
public class App 
{

    public static MetalAlloy[][] wholeAlloy;
    private static ExecutorService workStealingPool;

    public static void main( String[] args ) throws InterruptedException {
        /**
         *
         * This program will utilize the Fork/Join framework supplied with java, as a part of this project
         * we will also be placing the workload not only on multiple cores of this local machine, but also
         * on at least 2 school servers via sockets, this will require some sort of coordination and work
         * distribution logic, so we will define that later once the bulk of the logic for forking the work
         * locally is done. The use of sending parts of the alloy to be processed and receiving futures and
         * then subsequently waiting is going to be a necessary evil when distributing the work load among
         * many servers, that is technically part B of this assignment, but development/structuring of code
         * must allow for sending data/pieces to servers and waiting for them to complete their respective
         * workloads. More to come...
         */

        //work stealing pool to share the work among many threads
        App.workStealingPool =  Executors.newWorkStealingPool();


        int numRows=33 * 2 , numColumns=numRows * 4;
        CountDownLatch countDownLatch = new CountDownLatch(numRows * numColumns);
        System.out.println("countdown latch: " + countDownLatch.getCount());


        wholeAlloy = new MetalAlloy[numColumns][numRows];

        for(int i=0; i< numColumns; ++i){
            for(int j=0; j< numRows; ++j){
                wholeAlloy[i][j] = new MetalAlloy(i, j);
                countDownLatch.countDown();
            }
        }
        //wait for this to be done, then move on to other cool stuff
        countDownLatch.await();

        System.out.println("countdown latch: " + countDownLatch.getCount());
        GUI gui = new GUI();



        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Optimized Floor Plan");
            frame.setSize(new Dimension(Constants.alloyWidth + 200, Constants.alloyHeight + 600));
            frame.setResizable(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().setBackground(new Color(255, 255, 255));
            frame.add(gui);
            frame.setVisible(true);
        });


        int counter=0;


        /*
        we now are inside  the main logic of the program, we need a class that implements a recursive action
        based on 'chunks' or



        */
        Phaser phaser = new Phaser();




    }
}
