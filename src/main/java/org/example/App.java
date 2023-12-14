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
    private static double[][] previousTemperatures;

    //what we want
    //private static int numRows= 66 , numColumns=numRows * 4;

    //orig numrows = 17
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
        previousTemperatures = new double[numColumns][numRows];

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
                        wholeAlloy[i][j].setCurrentTemperature(0);//ThreadLocalRandom.current().nextDouble(1));
                    } else {
                        wholeAlloy[i][j] = new MetalAlloy(i, j, false, false);
                        wholeAlloy[i][j].setCurrentTemperature(0);//ThreadLocalRandom.current().nextDouble(1));
                    }
                    countDownLatch.countDown();
                }
                previousTemperatures[i][j] = 0.0;
            }
        }



        //we also now know what neighbors each section of the allow has, therefore we can
        //add the neighbors(atleast their coordinates) as a final field inside each section of the
        //alloy

        setNeighbors(wholeAlloy);

        //wait for this to be done, then move on to other cool stuff
        System.out.println("countdown latch: " + countDownLatch.getCount());

        //Thread.sleep(100000);

        //forkJoinPool = new ForkJoinPool();
        forkJoinPool = ForkJoinPool.commonPool();


        GUI gui = new GUI();




        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Heat Propogation GUI");
            frame.setSize(new Dimension(Constants.alloyWidth + 200, Constants.alloyHeight + 600));
            frame.setResizable(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().setBackground(new Color(255, 255, 255));
            frame.add(gui);
            frame.setVisible(true);
        });

        countDownLatch.countDown();


//        for(int i=0; i<wholeAlloy.length;++i){
//            for(int j=0; j<wholeAlloy[0].length; ++j){
//                for(MetalAlloy m : wholeAlloy[i][j].getNeighbors()){
//                    if(m.getPosition().getXCoord() ==0 && m.getPosition().getYCoord() ==0)
//                        System.out.println("top left corner is neighbor");
//                    if(m.getPosition().getXCoord() ==wholeAlloy.length-1 && m.getPosition().getYCoord() ==wholeAlloy[0].length -1)
//                        System.out.println("bottom right corner is neighbor");
//                }
//            }
//        }
//
//
//        Thread.sleep(1000000000);
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


                //System.out.println("Time to fuck some shit up recursively and parallely(i know thats not a word...i think)");

                //check convergence

//                if(phase > 10 && converged()){
//                    System.out.println("System has converged");
//                    System.exit(0);
//                }


//                System.out.println("New values: " );
//                for(int i=0; i<wholeAlloy.length; ++i){
//                    for(int j=0; j< wholeAlloy[0].length; ++j){
//                        System.out.println("temp: " + wholeAlloy[i][j].getCurrentTemperature());
//                    }
//                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                gui.repaint();


                System.out.println("Phase/Generation: " + phase);
                return phase >= Constants.numGenerations || registeredParties == 0;

            }
        };

        phaser.register();


        do{

            performTemperatureUpdate(App.wholeAlloy);

            phaser.arriveAndAwaitAdvance();
        }
        while(!phaser.isTerminated());


        phaser.arriveAndDeregister();

    }




    private static MetalAlloy[][] performTemperatureUpdate(MetalAlloy[][] alloyToBeChanged) throws InterruptedException {

        double[][] prev = fillTemperatures(alloyToBeChanged);
        previousTemperatures = prev;

        UpdateAlloyTemperature updateAlloyTemperature = new UpdateAlloyTemperature(
                alloyToBeChanged, prev, 0, alloyToBeChanged.length, 0, alloyToBeChanged[0].length);

        forkJoinPool.invoke(updateAlloyTemperature);


        return alloyToBeChanged;
    }


    private static void setNeighbors(MetalAlloy[][] alloy){

        ArrayList<MetalAlloy> neighbors;

        for(int i=0; i < numColumns; ++i) {
            for (int j = 0; j < numRows; ++j) {
                neighbors = new ArrayList<>();

                   if(i == 1 && j ==1 ){
                       neighbors.add(wholeAlloy[0][0]);
                   }
                   if(i == numColumns -2 && j == numRows -2){
                       neighbors.add(wholeAlloy[numColumns -1][numRows -1]);
                   }

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

    public static boolean converged(){
        int numMeetsThreshold=0;
        for(int i =0; i< wholeAlloy.length; ++i){
            for(int j=0; j< wholeAlloy[0].length; ++j){
                if((wholeAlloy[i][j].getCurrentTemperature() - previousTemperatures[i][j]) < Constants.THRESHOLD)
                    numMeetsThreshold++;
            }
        }
        //if over 90 perfent of the metal allow is very close to the same temp, end the simulation
        if(numMeetsThreshold > (wholeAlloy.length * wholeAlloy[0].length) -1){
            return true;
        }
        else {
            return false;
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
