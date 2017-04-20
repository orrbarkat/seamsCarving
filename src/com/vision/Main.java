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


public class Main {

    public static int findStraightSeam(double[][] energy){
        int i,j,w,h;
        w = energy[0].length;
        h = energy.length;
        double temp;
        double[] weights = new double[w];
        for(i=0; i<w; i++){
            temp =0;
            for(j=0;j<h;j++){
                temp += energy[j][i];
            }
            weights[i] = temp;
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
        int denominator;
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
        return sum/denominator;
    }

    public static void main(String[] args) {
        String path = "/Users/orrbarkat/repos/java/seamsCarving/images/halong_bay.jpg";
        System.out.println("hello");
//        ImageProcessing img = new ImageProcessing(path);
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(path));
        } catch (IOException e) {
            System.out.println(e);
            System.out.println("error in reading image from: " + path);
            System.exit(1);
        }
        int width = img.getWidth();
        int hight = img.getHeight();
        int local,i,j;
        int[][][] rgb = new int[hight][width][4];
        // get rgb
        for(i=0; i<hight; i++) {
            for (j = 0; j < hight; j++){
                local = img.getRGB(i, j);
                //blue
                rgb[i][j][2] = (0xff & local);
                //green
                rgb[i][j][1] = (0xff & local >> 8);
                //red
                rgb[i][j][0] = (0xff & local >> 16);
                // greyscale
                rgb[i][j][3] = (rgb[i][j][2] + rgb[i][j][2] + rgb[i][j][0]) / 3;
            }
        }

        double[][] energy = new double[hight][width];
        // compute energy
        for(i=0; i<hight; i++) {
            for (j = 0; j < width; j++) {
                energy[i][j] = computeEnergy(i,j,rgb);
            }
        }
        double[][] entropy = computeEntropy(rgb);

        Matrix energia = new Matrix(energy);
        energia = energia.plusEquals(new Matrix(entropy));
        System.out.println("done calculating!");

        findStraightSeam(energy);

    }
}
