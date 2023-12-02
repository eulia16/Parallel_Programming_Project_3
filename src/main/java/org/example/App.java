package org.example;

/**
 * Hello world!
 *
 */
public class App 
{

    public static MetalAlloy[][] wholeAlloy;

    public static void main( String[] args )
    {
        /**
         *
         * This program will utilize the Fork/Join framework supplied with java, as a part of this project
         * we will also be placing the workload not only on multiple cores of this local machine, but also
         * on at least 2 school servers via sockets, this will require some sort of coordination and work
         * distribution logic, so we will define that later once the bulk of the logic for forking the work
         * locally is done. The use of sending parts of the alloy to be processed and receiving futures and
         * then subsequently waiting is going to be a necessary evil when distributing the work load among
         * many servers, that is technically part B of this assignment, but development/structuring of code
         * must allow for sending data/pieces to servers and waiting for them to complete their respective
         * workloads. More to come...
         */

        wholeAlloy = new MetalAlloy[100][100];
        System.out.println(wholeAlloy.length);
        System.out.println(wholeAlloy[0].length);
        for(int i=0; i< wholeAlloy.length; ++i){
            for(int j=0; j< wholeAlloy[0].length; ++j){
                wholeAlloy[i][j] = new MetalAlloy(i, j);
            }
        }




    }
}
