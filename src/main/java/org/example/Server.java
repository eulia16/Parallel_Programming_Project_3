package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Phaser;


public class Server {


    private static ServerSocket connection;
    private static int numColumns, numRows, whatHalfWeHave;

    private static ForkJoinPool forkJoinPool;
    private static MetalAlloy[][] wholeAlloy;
    private static double[][] previousTemperatures;


    private static ObjectInputStream objectInputStream;
    private static ObjectOutputStream objectOutputStream;
    private static final int PORT = 12345;
    public static void main(String[] a) throws IOException, ClassNotFoundException {

        while(true) {
            forkJoinPool = ForkJoinPool.commonPool();


            connection = new ServerSocket(PORT);


            //while(true){
            System.out.println("Awaiting connections...");
            Socket client = connection.accept();
            System.out.println("Connected to: " + connection.getInetAddress() + connection.getLocalPort());

            DataInputStream inputStream = new DataInputStream(client.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(client.getOutputStream());
            objectInputStream = new ObjectInputStream(inputStream);
            objectOutputStream = new ObjectOutputStream(outputStream);

            numColumns = inputStream.readInt();
            System.out.println("read integer: " + numColumns);
            numRows = inputStream.readInt();
            System.out.println("read integer: " + numRows);
            whatHalfWeHave = inputStream.readInt();
            System.out.println("read integer: " + whatHalfWeHave);
            System.out.println("");

            wholeAlloy = new MetalAlloy[numColumns][numRows];

            for (int i = 0; i < numColumns; ++i) {
                for (int j = 0; j < numRows; ++j) {
                    wholeAlloy[i][j] = (MetalAlloy) objectInputStream.readObject();
                }
            }


            //after reading all the data in

//        for (int i = 0; i < numColumns; ++i) {
//            for (int j = 0; j < numRows; ++j) {
//                if(wholeAlloy[i][j]!= null)
//                    System.out.println("temps: " + wholeAlloy[i][j].getCurrentTemperature());
//
//            }
//
//        }

            //now that we have the data, we calculate the neighbors
            setNeighbors(wholeAlloy);

//        for (int i = 0; i < numColumns; ++i) {
//            for (int j = 0; j < numRows; ++j) {
//                if(wholeAlloy[i][j]!= null)
//                    System.out.println("neighbors: " + wholeAlloy[i][j].getNeighbors());
//
//            }
//
//        }

            //now for main compuation loop, we will need to send the updated temperatures to back to my computer
            //when the calculations finish

            final Phaser phaser = new Phaser() {

                //remember its essentially single threaded in here, do any otherwise unpredictable multithreaded behavior
                //inside here (s/a changing the current floor to the previous floor and print statements etc...)
                @Override
                protected boolean onAdvance(int phase, int registeredParties) {


                    System.out.println("another one");

                    boolean performAnotherIteration;

                    // Read the boolean signal from the client
                    System.out.println("awaiting signal from client");
                    try {
                        performAnotherIteration = objectInputStream.readBoolean();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    System.out.println("Received signal to perform another iteration: " + performAnotherIteration);

                    if (!performAnotherIteration) {
                        // If the signal is false, terminate the computation
                        return true;
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


                    System.out.println("Phase/Generation: " + phase);
                    return phase >= Constants.numGenerations || registeredParties == 0;// || !performAnotherIteration;

                }
            };

            phaser.register();


            do {

                double[][] newTemperatures = performCalculations();

                sendTemperaturesBack(newTemperatures);


                phaser.arriveAndAwaitAdvance();
            }
            while (!phaser.isTerminated());


            phaser.arriveAndDeregister();

        }

    }

    public static void sendTemperaturesBack(double[][] newTemps) throws IOException {
        System.out.println("sending newly calculated temperatures to client");
        objectOutputStream.writeObject(newTemps);
        objectOutputStream.flush();
        System.out.println("Sent!");
    }

    public static double[][] performCalculations(){

        double[][] prev = fillTemperatures(wholeAlloy);
        previousTemperatures = prev;

        //again use the numbers originally to determine what parts you need to perform calculations for

        int numberOfNeighborsWeCalculate=numColumns, start=0;

        //plus one and minus one to calculate an additional row of columns
        if(whatHalfWeHave == 0)
            numberOfNeighborsWeCalculate = (numColumns / 2) ;
        else
            start = (numberOfNeighborsWeCalculate / 2);

        //split calculations per server(no need to perform redundant calculations)

        //correct for splitting board in half***
//        UpdateAlloyTemperature updateAlloyTemperature = new UpdateAlloyTemperature(
//                wholeAlloy, prev, start, numberOfNeighborsWeCalculate, 0, wholeAlloy[0].length);

        //trying this*****
        UpdateAlloyTemperature updateAlloyTemperature = new UpdateAlloyTemperature(
                wholeAlloy, prev, 0, wholeAlloy.length, 0, wholeAlloy[0].length);

        forkJoinPool.invoke(updateAlloyTemperature);

        //grab new temps to send back
        double[][] newTemps = fillTemperatures(wholeAlloy);

        return newTemps;

    }


    //helper functions
    public static double[][] fillTemperatures(MetalAlloy[][] blah){

        int numberOfNeighborsWeCalculate=numColumns, start=0;

        //plus one and minus one to calculate an additional row of columns
        if(whatHalfWeHave == 0)
            numberOfNeighborsWeCalculate = (numColumns / 2) +1;
        else
            start = (numberOfNeighborsWeCalculate / 2) - 1;

        double[][] returnValue = new double[blah.length][blah[0].length];

        for(int i=0; i<numColumns;++i){
            for(int j=0; j<numRows; ++j){
                returnValue[i][j] = blah[i][j].getCurrentTemperature();
            }
        }

        return returnValue;
    }

    private static void setNeighbors(MetalAlloy[][] wholeAlloy){

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

}
