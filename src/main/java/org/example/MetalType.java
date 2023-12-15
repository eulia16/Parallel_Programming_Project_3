package org.example;

import java.awt.Color;
import java.io.Serializable;

public enum MetalType implements Serializable {

    Copper(Constants.C1, Color.ORANGE),
    Silver(Constants.C2, Color.LIGHT_GRAY),
    Gold(Constants.C3, Color.YELLOW);

    private final double thermalConstant;
    private final Color color;

    public double getThermalConstant(){
        return this.thermalConstant;
    }

    public Color getColor(){
        return this.color;
    }


    MetalType(double thermalConstant, Color color) {
        this.thermalConstant = thermalConstant;
        this.color = color;
    }

}
