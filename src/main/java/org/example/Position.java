package org.example;

public final class Position {
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
