package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * Hello world!
 *
 */
public class App {


    //the current state of the alloy
    public MetalAlloy[][] wholeAlloy;
    private static double[][] previousTemperatures;

    //server connections
    private static Socket rhoConnection;
    private static Socket piConnection;

    static ObjectOutputStream objectOutputStreamPi, objectOutputStreamRho;
    static ObjectInputStream objectInputStreamPi, objectInputStreamRho;
    //what we want
    //private static int numRows= 66 , numColumns=numRows * 4;

    //orig numrows = 17
    private static int numRows= 17 , numColumns=numRows * 4;

    //the threads that will be working on the board
    private static ForkJoinPool forkJoinPool;


    //the previous state of the allow, used for calculating the temperatures
    //public static MetalAlloy[][] previousStateOfAlloy, currentStateOfAlloy;



    DataOutputStream sendMessagePi;
    DataInputStream receiveMessagePi;
    DataOutputStream sendMessageRho;
    DataInputStream receiveMessageRho;

    public void Main() throws InterruptedException, IOException, CloneNotSupportedException, ClassNotFoundException {
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


        //networking stuff

        System.out.println("Attempting to connect to servers...");

        //System.out.println("Attempting to connect to servers...");
        rhoConnection = new Socket("moxie.cs.oswego.edu", 12345);
        System.out.println("Connected to Moxie!");
        piConnection = new Socket("pi.cs.oswego.edu", 12345);
        System.out.println("Connected to Pi!");

         sendMessagePi = new DataOutputStream(piConnection.getOutputStream());
         receiveMessagePi = new DataInputStream(piConnection.getInputStream());
         sendMessageRho = new DataOutputStream(rhoConnection.getOutputStream());
         receiveMessageRho = new DataInputStream(rhoConnection.getInputStream());

         //set up object output stream
        objectOutputStreamPi = new ObjectOutputStream(sendMessagePi);
        objectOutputStreamRho = new ObjectOutputStream(sendMessageRho);
        //set up object input streams as well
        objectInputStreamPi = new ObjectInputStream(receiveMessagePi);
        objectInputStreamRho = new ObjectInputStream(receiveMessageRho);


        //work stealing pool to share the work among many threads
        //App.forkJoinPool =  new ForkJoinPool();
        App.forkJoinPool = ForkJoinPool.commonPool();



        CountDownLatch countDownLatch = new CountDownLatch(numRows * numColumns +1 +1);
        System.out.println("countdown latch: " + countDownLatch.getCount());
        Thread.sleep(1000);


        this.wholeAlloy = new MetalAlloy[numColumns][numRows];
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


        //setNeighbors(wholeAlloy);


        //wait for this to be done, then move on to other cool stuff
        System.out.println("countdown latch: " + countDownLatch.getCount());


        //Thread.sleep(10000);

        //forkJoinPool = new ForkJoinPool();
        forkJoinPool = ForkJoinPool.commonPool();


        GUI gui = new GUI();
        gui.setFloor(wholeAlloy);




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

        //other testing thuings




        sentDataToRespectiveServer(Constants.PI_NUMBER, sendMessagePi, objectOutputStreamPi);
        sentDataToRespectiveServer(Constants.RHO_NUMBER, sendMessageRho, objectOutputStreamRho);

        //Thread.sleep(1000000);

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

                if(phase > 10 && converged()){
                    System.out.println("System has converged");
                    System.out.println("Closing all input/output/data streams");
                    try {
                        objectOutputStreamPi.close();
                        objectOutputStreamRho.close();
                         sendMessagePi.close();
                         receiveMessagePi.close();
                         sendMessageRho.close();
                         receiveMessageRho.close();

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    System.exit(0);
                }


                //testing


//                System.out.println("New values: " );
//                for(int i=0; i<wholeAlloy.length; ++i){
//                    for(int j=0; j< wholeAlloy[0].length; ++j){
//                        System.out.println("temp: " + wholeAlloy[i][j].getCurrentTemperature());
//                    }
//                }

//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }


                gui.setFloor(wholeAlloy);
                gui.repaint();


                System.out.println("Phase/Generation: " + phase);
                return phase >= Constants.numGenerations || registeredParties == 0;

            }
        };

        phaser.register();


        do{

            performTemperatureUpdate(this.wholeAlloy);

            phaser.arriveAndAwaitAdvance();
        }
        while(!phaser.isTerminated());


        phaser.arriveAndDeregister();

    }


    private  MetalAlloy[][] performTemperatureUpdate(MetalAlloy[][] alloyToBeChanged) throws InterruptedException, IOException, ClassNotFoundException {

        double[][] prev = fillTemperatures(alloyToBeChanged);
        previousTemperatures = prev;

        //the pi server is doing 0-N, and moxie is doing N-M
        int numberOfNeighborsWeCalculate=numColumns, start=0;

        //first read data in
        double[][] piData = (double[][]) objectInputStreamPi.readObject();
        double[][] rhoData = (double[][]) objectInputStreamRho.readObject();
//        objectOutputStreamPi.writeBoolean(true);
//        objectOutputStreamRho.writeBoolean(true);

        //add data to one whole array
        double[][] fullNewTemperatures = new double[numColumns][numRows];
        //for pi data
        for(int i=0; i<(numColumns / 2) ; ++i){
            for(int j=0; j< numRows; ++j){
                fullNewTemperatures[i][j] = piData[i][j];
            }
        }

        //for moxie data
        for(int i=(numColumns / 2); i<numColumns ; ++i){
            for(int j=0; j< numRows; ++j){
                fullNewTemperatures[i][j] = rhoData[i][j];
            }
        }

        //update wholeAlloy
        for(int i=0; i<numColumns ; ++i){
            for(int j=0; j< numRows; ++j){
                wholeAlloy[i][j].setCurrentTemperature(fullNewTemperatures[i][j]);
            }
        }


        return alloyToBeChanged;
    }


    private void sentDataToRespectiveServer(int server, DataOutputStream sendData, ObjectOutputStream objectOutputStream) throws IOException, InterruptedException {



        int start;

        //pi will do the 0 - N computation
        if(server == Constants.PI_NUMBER) {
            start =0;
        }
        //rho will do the N - M computation
        else {
            start = numColumns / 2 ;
        }


        System.out.println(sendData.toString());

        sendData.writeInt(App.numColumns);
        sendData.flush();



        sendData.writeInt(App.numRows);
        sendData.flush();

        //need to define a split so each server knows where to start their computation at
        sendData.writeInt(start);
        sendData.flush();

        System.out.println("sending object data to servers");
        //send the resective chunk once, and then never again
        for(int i=0; i < numColumns; ++i) {
            for (int j = 0; j < numRows; ++j) {
                  objectOutputStream.writeObject(wholeAlloy[i][j]);
            }
        }
        System.out.println();
        System.out.println("sAll sent!!");



//        for(int i=0; i < numColumns; ++i) {
//            for (int j = 0; j < numRows; ++j) {
//                System.out.println("temps: " + this.wholeAlloy[i][j].getCurrentTemperature());
//                sendData.writeDouble(this.wholeAlloy[i][j].getCurrentTemperature());// .writeObject(prev);
//                sendData.flush();
//            }
//        }



    }


    private  void setNeighbors(MetalAlloy[][] alloy){

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

    public  boolean converged(){
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



    public App() throws IOException {
    }

    public MetalAlloy[][] getWholeAlloy(){
        return wholeAlloy;
    }


}
