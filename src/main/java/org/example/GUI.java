package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class GUI extends JPanel {
    private final static int gridSize = 10;
    private final static int squareSize = 10;

    private final static int moveTextDownPixels = 25;
    private static int space;

    public GUI(){
        super();

        space = (800 - gridSize)/10;
        this.setBounds(600,600,800,800);
        this.setOpaque(true);
        this.setSize(1000,1000);
        this.setBackground(Color.WHITE);

        this.setVisible(true);

    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int squareSize = this.squareSize;
        int padding = 20, fontSize = 9;
        Graphics2D g2 = (Graphics2D) g;

        //set font size to be smaller to allow painting the strings to be easier
        Font font = new Font("Comic Sans MS", Font.BOLD, fontSize);
        g2.setFont(font);

        MetalAlloy[][] floorToDraw = App.wholeAlloy;
        //Station[][] floorToDraw = App.bestEverFloor;
        for(int i =0; i < floorToDraw.length; ++i){
            for (int j = 0; j< floorToDraw[0].length; ++j){
                int x = i * squareSize + padding;
                int y = j * squareSize + padding;
                //get originalRBG colors from the respective metal
                Color mixtureOfColors;
                double r=0, b=0, green=0;
                r += (floorToDraw[i][j].getPercentageOfCopper() * MetalType.Copper.getColor().getRed()) /100;
                r += (floorToDraw[i][j].getPercentageOfSilver() * MetalType.Silver.getColor().getRed()) /100;
                r += (floorToDraw[i][j].getPercentageOfGold() * MetalType.Gold.getColor().getRed()) / 100;
                r /= 3;
                System.out.println("red: " + r);

                b += (floorToDraw[i][j].getPercentageOfCopper() * MetalType.Copper.getColor().getBlue()) / 100;
                b += (floorToDraw[i][j].getPercentageOfSilver() * MetalType.Silver.getColor().getBlue()) / 100;
                b += (floorToDraw[i][j].getPercentageOfGold() * MetalType.Gold.getColor().getBlue()) / 100;
                b /= 3;
                System.out.println("blue: " + b);

                green += (floorToDraw[i][j].getPercentageOfCopper() * MetalType.Copper.getColor().getGreen()) / 100;
                green += (floorToDraw[i][j].getPercentageOfSilver() * MetalType.Silver.getColor().getGreen()) / 100;
                green += (floorToDraw[i][j].getPercentageOfGold() * MetalType.Gold.getColor().getGreen()) / 100;
                green /= 3;
                System.out.println("green: " + green);

                Color averagedColor = new Color((int)r, (int)green, (int)b);

                //start actually presenting the data of the best factory floor
                g2.setColor(averagedColor); // You can set your desired color

                g2.fillRect(x + padding, y + padding, squareSize, squareSize);
                g2.setColor(Color.BLACK);
//                if(floorToDraw[i][j].getStation().getColor() == Color.BLACK)
//                    g2.setColor(Color.WHITE);
//                g2.drawString("(" + x + "," + y + ")", x + padding, y + padding + moveTextDownPixels);
//                g2.drawString(floorToDraw[i][j].getStation().getStationType(floorToDraw[i][j].getStation()), x + padding, y + padding + moveTextDownPixels + moveTextDownPixels);



            }
        }
//        g2.setColor(Color.BLACK);
//        g2.drawString("Total Affinity: " + App.bestAffinityEver,350, 600 + padding + (moveTextDownPixels * 3));



    }

}