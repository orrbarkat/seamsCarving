package com.vision;
import javax.imageio.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import Jama.*;

import static java.lang.Math.abs;
import static java.lang.Math.log;
import static java.lang.Math.min;



public class Main {

    private static int outputNumOfCols;
    private static int outputNumOfRows;
    private static int imgHeight;
    private static int imgWidth;

    public static int[][] computeAllOptimalSeams(double[][] energy,int numOfSeams)
    {
        int[][] seams = new int[imgHeight][imgWidth];
        for (int i=1 ; i<numOfSeams+1 ; i++)
        {
            seams = computeOptimalSeam(energy,seams,i);
        }
        return seams;
    }

    private static int[][] computeOptimalSeam(double[][] energy, int[][] seams, int nextSeamIndex)
    {
        //find minimal pixel in bottom row
        double minVal= Double.MAX_VALUE;
        int minIndex = 0;
        for (int i=0 ; i<imgWidth ; i++)
        {
            if (seams[imgHeight-1][i] != 0)continue;
            minVal = min(minVal,energy[imgHeight-1][i]);
            minIndex = (minVal == energy[imgHeight - 1][i]) ? i : minIndex;
        }

        seams[0][minIndex] = nextSeamIndex;

        for (int i=1 ; i<imgHeight ; i++)
        {
            //check if one of the 3 pixels above are already taken by another seam
            double leftPixel = seams[i][minIndex-1] == 0 ? energy[i][minIndex-1] : Double.MAX_VALUE;
            double rightPixel = seams[i][minIndex+1] == 0 ? energy[i][minIndex+1] : Double.MAX_VALUE;
            double midPixel = seams[i][minIndex] == 0 ? energy[i][minIndex] : Double.MAX_VALUE;

            double minTopPixel = min(leftPixel,min(rightPixel,midPixel));

            if (minTopPixel == leftPixel)
            {
                seams[i][minIndex-1] = nextSeamIndex;
                minIndex--;
            }
            else if (minTopPixel == rightPixel)
            {
                seams[i][minIndex+1] = nextSeamIndex;
                minIndex++;
            }
            else seams[i][minIndex] = nextSeamIndex;
        }

        return seams;
    }

    private static double[][] computeEnergyWithDynamicProg(double[][] energy)
    {
        double[][] result = new double[imgHeight][imgWidth];
        System.arraycopy(energy,0,result,0,imgWidth);

        for (int i=1 ; i<imgHeight ; i++)//start from second row, hence start from i=1
        {
            //compute the left edge first
            result[i][0] = energy[i][0] + min(result[i-1][0],result[i-1][1]);
            //compute the rest of the row
            for (int j=1 ; j<imgWidth-1 ; j++)
            {
                result[i][j] = energy[i][j] + min(result[i-1][j-1],min(result[i-1][j],result[i-1][j+1]));
            }
            //compute right edge
            result[i][imgWidth-1] = energy[i][0] + min(result[i-1][imgWidth-1],result[i-1][imgWidth-2]);

        }
        return result;
    }

    public static void main(String[] args) {
        String path = args[0];
        outputNumOfCols = Integer.valueOf(args[1]);
        outputNumOfRows = Integer.valueOf(args[2]);

        System.out.println("hello");

        ImageProcessing img = new ImageProcessing(path,false);
        ImageProcessing trans = img.transpose();

        System.out.println("done calculating!");

        int vertSeam = img.findStraightSeam();
        int horizontal = trans.findStraightSeam();

        //for vertical seams
//       int[][] seams = computeAllOptimalSeams(computeEnergyWithDynamicProg(img.energy),imgWidth-outputNumOfCols);
        //for horizontal seams we need to transpose the matrix and then use this function
//        computeAllOptimalSeams(computeEnergyWithDynamicProg(energy),imgHeight-outputNumOfRows);

    }
}
