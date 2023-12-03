package org.example;


import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

//every method should be static as it does not rely on any state except that passed into it
public final class Formula {


    //I think the division is in the correct place but check/ensure later!!!!
    public static double calculate(ArrayList<MetalAlloy> neighbors){
        //for each metal that is inside the calculation
        double newTemperature =0;

        //for each neighbor, should be 4 every time(north east west south neighbors)
        for(MetalAlloy neighbor : neighbors) {
            //for each metal inside the alloy
            double thermalConstantTimeLoop=0.0;
            for (int i = 0; i < MetalType.values().length; ++i) {
                //get respective thermal constant for neighbors metal
                double innerSummation = 0.0;
                for(int j=0; j < neighbors.size(); ++j){
                    innerSummation += (neighbor.getCurrentTemperature() * neighbor.getArrayOfPercentagesOfMetal().get(i));
                }
                //get the thermal constant for each respective metal and mult it to the sum of the loop above
                thermalConstantTimeLoop += innerSummation / (double) MetalType.values().length;;
            }

            newTemperature += thermalConstantTimeLoop;
        }


        return newTemperature;
    }



}
