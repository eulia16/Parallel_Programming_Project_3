package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * Hello world!
 *
 */
public class App 
{


    //the current state of the alloy
    public static MetalAlloy[][] wholeAlloy, prevAllow;

    private static int numRows= 66 , numColumns=numRows * 4;
    //the threads that will be working on the board
    private static ForkJoinPool forkJoinPool;
    //the previous state of the allow, used for calculating the temperatures
    //public static MetalAlloy[][] previousStateOfAlloy, currentStateOfAlloy;

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
        //App.forkJoinPool =  new ForkJoinPool();
        App.forkJoinPool = ForkJoinPool.commonPool();


        CountDownLatch countDownLatch = new CountDownLatch(numRows * numColumns +1 +1);
        System.out.println("countdown latch: " + countDownLatch.getCount());
        Thread.sleep(1000);


        wholeAlloy = new MetalAlloy[numColumns][numRows];

        for(int i=0; i < numColumns; ++i){
            for(int j=0; j < numRows; ++j){
                final boolean isHeatedCorner = (i == 0 && j == 0) || (i == numColumns - 1 && j == numRows - 1);
                if(isHeatedCorner) {
                    wholeAlloy[i][j] = new MetalAlloy(i, j, true, true);
                    //top left
                    if(i ==0 && j == 0) {
                        wholeAlloy[i][j].setCurrentTemperature(Constants.S);
                    }
                    //bottom right
                    else {
                        wholeAlloy[i][j].setCurrentTemperature(Constants.T);
                    }
                    countDownLatch.countDown();
                }

                //now that the heated corners are dealt with, we need to determine if the part of the allow
                //is an edge, we can do this by looking at the index to see if certain cond are met
                else {
                    //not a corner peice but on the outside
                    if (!isHeatedCorner && (i == 0 || j == 0 || j == numRows - 1 || i == numColumns - 1)) {
                        wholeAlloy[i][j] = new MetalAlloy(i, j, false, true);
                        wholeAlloy[i][j].setCurrentTemperature(ThreadLocalRandom.current().nextDouble(40));
                    } else {
                        wholeAlloy[i][j] = new MetalAlloy(i, j, false, false);
                        wholeAlloy[i][j].setCurrentTemperature(ThreadLocalRandom.current().nextDouble(40));
                    }
                    countDownLatch.countDown();
                }
            }
        }



        //we also now know what neighbors each section of the allow has, therefore we can
        //add the neighbors(atleast their coordinates) as a final field inside each section of the
        //alloy

        setNeighbors(wholeAlloy);

        for(int i=0; i<numColumns; ++i){
            for(int j=0; j<numRows; ++j){
                for(MetalAlloy m : wholeAlloy[i][j].getNeighbors()){
                    if(m.getPosition().getXCoord() < 0 || m.getPosition().getYCoord() < 0 || m.getPosition().getXCoord() > numColumns || m.getPosition().getYCoord() > numRows){
                        System.out.printf("FUCK");
                        System.exit(0);
                    }
                    System.out.println("x coord: " + m.getPosition().getXCoord() + ", y coord: " + m.getPosition().getYCoord() );
                }
                System.out.println("");
            }
        }

        //wait for this to be done, then move on to other cool stuff
        System.out.println("countdown latch: " + countDownLatch.getCount());

        //forkJoinPool = new ForkJoinPool();
        forkJoinPool = ForkJoinPool.commonPool();


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

        countDownLatch.countDown();


        countDownLatch.countDown();

        countDownLatch.await();






        /*
        we now are inside  the main logic of the program, we need a class that implements a recursive action
        based on 'chunks' or a mesh

        */

        final Phaser phaser = new Phaser(){

            //remember its essentially single threaded in here, do any otherwise unpredictable multithreaded behavior
            //inside here (s/a changing the current floor to the previous floor and print statements etc...)
            @Override
            protected boolean onAdvance(int phase, int registeredParties) {


                System.out.println("Time to fuck some shit up recursively and parallely(i know thats not a word...i think)");

                gui.repaint();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                System.out.println("Phase/Generation: " + phase);
                return phase >= Constants.numGenerations || registeredParties == 0;

            }
        };

        phaser.register();


        do{

            performTemperatureUpdate(App.wholeAlloy);

//
//            //just to see some temperature changes/testing
//            for(int i=0; i< numColumns; ++i){
//                for(int j=0; j< numRows; ++j){
//                    if(i ==0 && j == 0 || i == numColumns -1 && j == numRows -1) {
//                        //top left
//                        if(i ==0 && j ==0){}
//                            //wholeAlloy[i][j].setCurrentTemperature(Constants.S);
//                            //bottom right
//                        else {
//                            //wholeAlloy[i][j].setCurrentTemperature(Constants.T);
//                            break;
//                        }
//                    }
//
//                    else {
//                        if(i+j >255)
//                            wholeAlloy[i][j].setCurrentTemperature(255);
//                        else
//                            wholeAlloy[i][j].setCurrentTemperature(i+j);//ThreadLocalRandom.current().nextDouble(255));
//                    }
//                }
//            }



            System.out.println("method returned");
            phaser.arriveAndAwaitAdvance();
        }
        while(!phaser.isTerminated());


        phaser.arriveAndDeregister();

    }




    private static MetalAlloy[][] performTemperatureUpdate(MetalAlloy[][] alloyToBeChanged) throws InterruptedException {

        System.out.println("Inside performTemperatureUpdate function");

        //i think this is correct
          //MetalAlloy[][] newBoard = new MetalAlloy[alloyToBeChanged.length][alloyToBeChanged[0].length];
        double[][] previousTemperatures = fillTemperatures(alloyToBeChanged);


        UpdateAlloyTemperature updateAlloyTemperature = new UpdateAlloyTemperature(
                alloyToBeChanged, previousTemperatures, 0, alloyToBeChanged.length, 0, alloyToBeChanged[0].length);



        long startTime = System.currentTimeMillis();

        forkJoinPool.invoke(updateAlloyTemperature);

        long endTime = System.currentTimeMillis();
        System.out.println("changing the temp time: " + (endTime - startTime) +
                " milliseconds");


        return alloyToBeChanged;
    }


    private static void setNeighbors(MetalAlloy[][] alloy){

        ArrayList<MetalAlloy> neighbors;

        for(int i=0; i < numColumns; ++i) {
            for (int j = 0; j < numRows; ++j) {
                neighbors = new ArrayList<>();
                final boolean isHeatedCorner = (i == 0 && j == 0) || (i == numColumns - 1 && j == numRows - 1);

                if (isHeatedCorner) {
                    //top left
                    if (i == 0 && j == 0) {
                        neighbors.add(wholeAlloy[i + 1][j]);//one to the right
                        neighbors.add(wholeAlloy[i][j + 1]);//one below
                        neighbors.add(wholeAlloy[i + 1][j + 1]);//one in the center-ish
                        wholeAlloy[i][j].setNeighbors(neighbors);
                    }
                    //bottom right
                    else {
                        neighbors.add(wholeAlloy[i - 1][j]);//one to the left
                        neighbors.add(wholeAlloy[i][j - 1]);//one above
                        neighbors.add(wholeAlloy[i - 1][j - 1]);//one in the center-ish
                        wholeAlloy[i][j].setNeighbors(neighbors);
                    }


                }
                else {
                    if (i > 0) {
                        // Add the cell above the current cell
                        neighbors.add(wholeAlloy[i - 1][j]);
                    }

                    if (i < wholeAlloy.length - 1) {
                        // Add the cell below the current cell
                        neighbors.add(wholeAlloy[i + 1][j]);
                    }

                    if (j > 0) {
                        // Add the cell to the left of the current cell
                        neighbors.add(wholeAlloy[i][j - 1]);
                    }

                    if (j < wholeAlloy[0].length - 1) {
                        // Add the cell to the right of the current cell
                        neighbors.add(wholeAlloy[i][j + 1]);
                    }

                    wholeAlloy[i][j].setNeighbors(neighbors);
                }


            }
        }

    }

    public static double[][] fillTemperatures(MetalAlloy[][] blah){
        double[][] returnValue = new double[blah.length][blah[0].length];
        for(int i=0; i<blah.length;++i){
            for(int j=0; j<blah[0].length; ++j){
                returnValue[i][j] = blah[i][j].getCurrentTemperature();
            }
        }

        return returnValue;
    }



}
