package org.example;

import java.util.concurrent.RecursiveAction;

public class UpdateAlloyTemperature extends RecursiveAction {

    private MetalAlloy[][] currentBoard, updatedBoard;


    public UpdateAlloyTemperature(MetalAlloy[][] m1, MetalAlloy[][] m2){
        this.currentBoard = m1;
        this.updatedBoard = m2;
    }


    @Override
    protected void compute() {
        System.out.println("INside compute methid");

    }



}
