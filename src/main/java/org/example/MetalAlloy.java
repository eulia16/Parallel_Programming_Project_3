package org.example;


import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

//Represents the alloy we will be simulating, all constants/metal resistances are held in the Constants class
public class MetalAlloy {

    private final LinkedHashMap<MetalType, Integer> metalTypes;
    private final Position position;
    private int percentageOfGold, percentageOfSilver, percentageOfCopper;
    private double currentTemperature = -1;


    //used to initialize a metal alloy that consists of 3 randomly assigned percentages, with some noise as well
    public MetalAlloy(int i, int j){
        //first calculate the noise
        int noise = ThreadLocalRandom.current().nextInt(Constants.levelOfNoise);
        this.metalTypes = new LinkedHashMap<>();
        System.out.println("Percentage of noise in metal: " + noise);
        //calculate the metals percentage randomly, ensuring there is maximum 25 percent noise
        int totalPercentage=0;
        for(MetalType type : MetalType.values()){
            switch(type){
                case Copper:
                    this.percentageOfCopper = ThreadLocalRandom.current().nextInt(100 - noise);
                    totalPercentage +=this.percentageOfCopper;
                    this.metalTypes.put(MetalType.Copper, this.percentageOfCopper);
                     //System.out.println("Percentage of copper in metal: " + this.percentageOfCopper);
                        break;
                case Silver:
                    this.percentageOfSilver = ThreadLocalRandom.current().nextInt(100 - (this.percentageOfCopper + noise) );
                    this.metalTypes.put(MetalType.Silver, this.percentageOfSilver);
                    totalPercentage +=this.percentageOfSilver;
                     //System.out.println("Percentage of silver in metal: " + this.percentageOfSilver);
                        break;
                case Gold:
                    if(totalPercentage >= 75){
                        totalPercentage=0;
                    }
                    else{
                        totalPercentage = 75 - totalPercentage;
                    }
                    this.percentageOfGold = ThreadLocalRandom.current().nextInt(totalPercentage,100 - (this.percentageOfCopper + this.percentageOfSilver+ noise) );
                    this.metalTypes.put(MetalType.Gold, this.percentageOfGold);
                     //System.out.println("Percentage of gold in metal: " + this.percentageOfGold);
                        break;

            }
        }


        this.position = new Position(i, j);

        for(Map.Entry<MetalType, Integer> s : this.metalTypes.entrySet()){
            System.out.println("metal type: " + s.getKey() + ", percentage " + s.getValue());
        }

    }
    public void setCurrentTemperature(double t){
        this.currentTemperature = t;
    }

    public Position getPosition(){
        return this.position;
    }

    public LinkedHashMap<MetalType, Integer> getLinkedHashmap(){
        return this.metalTypes;
    }

    public ArrayList<Integer> getArrayOfPercentagesOfMetal(){
       ArrayList<Integer> temp  = new ArrayList<>();
       temp.add(percentageOfCopper);
       temp.add(percentageOfSilver);
       temp.add(percentageOfGold);
       return temp;
    }

    public int getPercentageOfCopper() {
        return percentageOfCopper;
    }

    public int getPercentageOfGold() {
        return percentageOfGold;
    }

    public int getPercentageOfSilver() {
        return percentageOfSilver;
    }



    //needs to be synchronized in case more than one thread tries to access it at the same time
    public synchronized double getCurrentTemperature(){
        return this.currentTemperature;
    }


}
