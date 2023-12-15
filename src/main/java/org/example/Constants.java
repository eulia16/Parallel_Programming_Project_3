package org.example;


public final class Constants {

    //copper thermal constant
    public static final double C1 =0.9;//= 0.923;
    //silver thermal constant
    public static final double C2 =1.056;//= 1.145;
    //gold silver constant
    //worked decently with 1.305
    public static final double C3 =1.42;//= 1.305;

    public static final int PI_NUMBER=1;
    public static final int RHO_NUMBER=2;


    public static final double THRESHOLD = 0.0000001;


    public static final double S = 300; // top left corner mesh element at index [0,0] is heated at S degrees Celsius
    public static final double T = 255; // bottom right corner (index [width - 1,height - 1]) is heated at T degrees Celsius


    public static final int levelOfNoise = 25;

    public static final int alloyHeight = 400;
    public static final int alloyWidth = alloyHeight * 4;

    public static final int numGenerations = 100_000;

    public static final int numProcessors = Runtime.getRuntime().availableProcessors();

    public static final int sizeThreshold = 3;



}
