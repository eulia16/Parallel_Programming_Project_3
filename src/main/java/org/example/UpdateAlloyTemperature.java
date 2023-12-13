package org.example;

import java.util.concurrent.RecursiveAction;

public class UpdateAlloyTemperature extends RecursiveAction {

    private MetalAlloy[][] currentBoard;
    private double[][] temperatures;
    private int verticalLow, verticalHigh, horizontalLow, horizontalHigh;

    public UpdateAlloyTemperature(MetalAlloy[][] board, double[][] temperatures, int vertLow, int vertHigh, int horizontalLow, int horizontalHigh){
        this.currentBoard = board;
        this.temperatures = temperatures;
        this.verticalLow = vertLow;
        this.verticalHigh = vertHigh;
        this.horizontalLow = horizontalLow;
        this.horizontalHigh = horizontalHigh;

    }

    //the goal is to allow each part of the allow to be surrounded by neighbors north, east, west, and south
    //to have this, each piece will need to have a square around it, if its a corner or a top piece, just dont add the null
    //neighbors in the calculation
    @Override
    protected void compute() {

        boolean splitDownMiddle = (this.verticalHigh - this.verticalLow ) > 2;
        boolean splitInHalf = (this.horizontalHigh - this.horizontalLow ) > 2;


        //if we done have a small enough square, recurse again w/ two newly split halves
        if(splitDownMiddle){

            int middle = (this.verticalHigh + this.verticalLow ) / 2;


            UpdateAlloyTemperature left = new UpdateAlloyTemperature(this.currentBoard, this.temperatures, this.verticalLow, middle, this.horizontalLow, this.horizontalHigh);
            UpdateAlloyTemperature right = new UpdateAlloyTemperature(this.currentBoard, this.temperatures, middle, this.verticalHigh, this.horizontalLow, this.horizontalHigh);

            invokeAll(left, right);

        }//same for if not big enough down the middle
        if(splitInHalf){
            int middle = (this.horizontalHigh + this.horizontalLow ) / 2;

            UpdateAlloyTemperature left = new UpdateAlloyTemperature(this.currentBoard, this.temperatures, this.verticalLow, this.verticalHigh, this.horizontalLow, middle);
            UpdateAlloyTemperature right = new UpdateAlloyTemperature(this.currentBoard, this.temperatures, this.verticalLow, this.verticalHigh, middle, this.horizontalHigh);

            invokeAll(left, right);
        }
        else{//otherwise we will compute directly
            computeDirectly();
        }


    }


    protected void computeDirectly(){

        for(int i=horizontalLow; i<horizontalHigh; ++i){
            for(int j=verticalLow;  j<verticalHigh; ++j){

                currentBoard[j][i].calculate(temperatures);
            }

        }

    }



}
