package org.example;

import java.io.Serializable;

public final class Position implements Serializable {
    private final int i;
    private final int j;
    public Position(int i, int j){
        this.i = i;
        this.j = j;
    }

    public int getXCoord(){
        return this.i;
    }
    public  int getYCoord(){
        return this.j;
    }

}
