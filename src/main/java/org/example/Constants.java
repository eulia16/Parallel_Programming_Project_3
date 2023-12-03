package org.example;


public final class Constants {

    //copper thermal constant
    public static final double C1 = 0.75;
    //silver thermal constant
    public static final double C2 = 1.0;
    //gold silver constant
    public static final double C3 = 1.25;


    public static final double S = 100; // top left corner mesh element at index [0,0] is heated at S degrees Celsius
    public static final double T = 200; // bottom right corner (index [width - 1,height - 1]) is heated at T degrees Celsius


    public static final int levelOfNoise = 25;

    public static final int alloyHeight = 400;
    public static final int alloyWidth = alloyHeight * 4;

    public static final int numGenerations = 100_000;

    public static final int numProcessors = Runtime.getRuntime().availableProcessors();



}
