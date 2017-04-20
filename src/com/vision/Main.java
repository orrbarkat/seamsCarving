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

    public static int findStraightSeam(double[][] energy){
        int i,j,w,h;
        w = energy[0].length;
        h = energy.length;
        double temp;
        double[] weights = new double[w];
        for(i=0; i<w; i++){
            for(j=0;j<h;j++){
                weights[i] += energy[j][i];
            }
        }
        i = IntStream.range(0,weights.length)
                .reduce((a,b) -> weights[a] < weights[b] ? a : b)
                .getAsInt();
        System.out.println(i);
        return i;
    }

    public static double[][] computeEntropyHelper(int[][][] rgb) {
        int x,y,i,j,h,w;
        double sum;
        h = rgb.length;
        w = rgb[0].length;
        double[][] pmn = new double[h][w];
        for(x=0;x<h;x++){
            for(y=0; y<w; y++){
                sum = 0;
                for(i=-4;i<5; i++){
                    if (x+i<0 || x+i>=h){ continue;}
                    for(j=-4; j<5;j++) {
                        if (y + j >= w || y + j < 0) {continue;}
                        sum += rgb[x+i][y+j][3];
                    }
                }
                pmn[x][y] = (double)rgb[x][y][3] / sum;
            }
        }
        return pmn;
    }

    public static double[][] computeEntropy(int[][][] rgb) {
        int h,w,x,y,i,j;
        h = rgb.length;
        w = rgb[0].length;
        double[][] pmn = computeEntropyHelper(rgb);
        double[][] entropy = new double[h][w];
        for(x=0;x<h;x++) {
            for (y = 0; y < w; y++) {
                entropy[x][y] = 0;
                for(i=-4;i<5; i++){
                    if (x+i<0 || x+i>=h){ continue;}
                    for(j=-4; j<5;j++) {
                        if (y + j >= w || y + j < 0) {continue;}
                        entropy[x][y] -= pmn[x+i][y + j]*log(pmn[x+i][y + j]);
                    }
                }
            }
        }
        return entropy;
    }

    public static double computeEnergy(int x,int y, int[][][] rgb){
        int denominator = 0;
        double sum = 0;
        int i,j,k,h,w;
        h = rgb.length;
        w = rgb[0].length;
        for(i=-1;i<2; i++){
            if (x+i<0 || x+i>=h){ continue;}
            for(j=-1; j<2;j++){
                if ( y+j>=w || y+j<0 || (i==0 && j==0) ) { continue;}
                for(k=0;k<3;k++){
                    try{
                        sum += abs(rgb[x+i][y+j][k] - rgb[x][y][k]);
                        denominator++;
                    }catch (IndexOutOfBoundsException e){
                        System.out.println("error");
                    }
                }
            }
        }
        return (sum != 0.0 ? sum : 0.001)/denominator;
    }

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
        String path = args[1];
        System.out.println("hello");
        ImageProcessing imgp = new ImageProcessing(path);
        imgp.transpose();
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(path));
        } catch (IOException e) {
            System.out.println(e);
            System.out.println("error in reading image from: " + path);
            System.exit(1);
        }

        outputNumOfCols = Integer.valueOf(args[2]);
        outputNumOfRows = Integer.valueOf(args[3]);

        imgWidth = img.getWidth();
        imgHeight = img.getHeight();
        int local,i,j;
        int[][][] rgb = new int[imgHeight][imgWidth][4];
        // get rgb
        for(i=0; i<imgHeight; i++) {
            for (j = 0; j < imgHeight; j++){
                local = img.getRGB(i, j);
                //blue
                rgb[i][j][2] = (0xff & local);
                //green
                rgb[i][j][1] = (0xff & local >> 8);
                //red
                rgb[i][j][0] = (0xff & local >> 16);
                // grayscale
                rgb[i][j][3] = (rgb[i][j][2] + rgb[i][j][2] + rgb[i][j][0]) / 3;
            }
        }

        double[][] energy = new double[imgHeight][imgWidth];
        // compute energy
        for(i=0; i<imgHeight; i++) {
            for (j = 0; j < imgWidth; j++) {
                energy[i][j] = computeEnergy(i,j,rgb);
            }
        }
        double[][] entropy = computeEntropy(rgb);

        System.out.println("done calculating!");

        int seam = findStraightSeam(energy);

        //for vertical seams
       int[][] seams = computeAllOptimalSeams(computeEnergyWithDynamicProg(energy),imgWidth-outputNumOfCols);
        //for horizontal seams we need to transpose the matrix and then use this function
//        computeAllOptimalSeams(computeEnergyWithDynamicProg(energy),imgHeight-outputNumOfRows);

    }
}
