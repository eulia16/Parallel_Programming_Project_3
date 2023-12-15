package org.example;


import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

//Represents the alloy we will be simulating, all constants/metal resistances are held in the Constants class
public class MetalAlloy implements Serializable{

    private MetalAlloy m;


    public MetalAlloy(MetalAlloy m){
        this.m = m;
    }

     //private  LinkedHashMap<MetalType, Double> metalTypes;
    private  Position position;
    private double percentageOfGold, percentageOfSilver, percentageOfCopper;
    private double currentTemperature = -1;

    private ArrayList<MetalAlloy> neighbors;

    private boolean isHeatedCorner=false;


    //used to initialize a metal alloy that consists of 3 randomly assigned percentages, with some noise as well
    public MetalAlloy(int i, int j, boolean isHeatedCorner, boolean isEdge){

        this.isHeatedCorner = isHeatedCorner;
        //used to determine if it is on the outside of the rectangle
        //DONT DELETE******

        //this works perfectly with these values and
        //constant values of:
        //    public static final double C1 = 0.78;
        //    //silver thermal constant
        //    public static final double C2 = 1.00;
        //    //gold silver constant
        //    public static final double C3 = 1.25;
//        this.metalTypes = new LinkedHashMap<>();
//        this.percentageOfGold = 0.33;
//        this.metalTypes.put(MetalType.Gold, this.percentageOfGold);
//        this.percentageOfSilver = 0.33;
//        this.metalTypes.put(MetalType.Silver, this.percentageOfSilver);
//        this.percentageOfCopper = 0.33;
//        this.metalTypes.put(MetalType.Copper, this.percentageOfCopper);
        //************


        this.percentageOfCopper =  ThreadLocalRandom.current().nextDouble(25, 34);

        this.percentageOfSilver =  ThreadLocalRandom.current().nextDouble(25, 34);

        this.percentageOfGold = ThreadLocalRandom.current().nextDouble(25, 34);

        this.position = new Position(i, j);


    }
    public void setCurrentTemperature(double t){
        this.currentTemperature = t;
    }

    public Position getPosition(){
        return this.position;
    }

    public ArrayList<Double> getArrayOfPercentagesOfMetal(){
       ArrayList<Double> temp  = new ArrayList<>();
       temp.add(percentageOfCopper);
       temp.add(percentageOfSilver);
       temp.add(percentageOfGold);
       return temp;
    }

    public ArrayList<MetalAlloy> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(ArrayList<MetalAlloy> neighbors) {
        this.neighbors = neighbors;
    }

    public double getPercentageOfCopper() {
        return percentageOfCopper;
    }

    public double getPercentageOfGold() {
        return percentageOfGold;
    }

    public double getPercentageOfSilver() {
        return percentageOfSilver;
    }



    //needs to be synchronized in case more than one thread tries to access it at the same time
    public synchronized double getCurrentTemperature(){
        return this.currentTemperature;
    }

    public double calculate(double[][] temperatures){


        //for each metal that is inside the calculation
        double newTemperature =0;

        if(isHeatedCorner)
            return this.currentTemperature;



            //for each metal inside the alloy
            double thermalConstantTimeLoop=0.0;

            for (int i = 0; i < MetalType.values().length; ++i) {
                //get respective thermal constant for neighbors metal
                double innerSummation = 0.0;
                for(MetalAlloy neighbor : neighbors){
                    innerSummation += (temperatures[neighbor.getPosition().getXCoord()][neighbor.getPosition().getYCoord()]
                            * (neighbor.getArrayOfPercentagesOfMetal().get(i) /100.0));
                }
                thermalConstantTimeLoop += MetalType.values()[i].getThermalConstant() * (innerSummation /neighbors.size());


            }
             //System.out.println("Newly calculated temp: " + thermalConstantTimeLoop / neighbors.size());

            temperatures[this.getPosition().getXCoord()][this.getPosition().getYCoord()] = this.currentTemperature;

            newTemperature = thermalConstantTimeLoop;

            this.setCurrentTemperature(newTemperature);



        return newTemperature;
    }


}
