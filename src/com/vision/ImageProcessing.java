package com.vision;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.stream.IntStream;

import static java.lang.Math.abs;
import static java.lang.Math.log;

/**
 * Created by orrbarkat on 19/04/2017.
 */
public class ImageProcessing {
    private BufferedImage img = null;
    private int[][][] rgb;
    private double[][] energy;
    private double[][] entropy = null;
    private int imgWidth;
    private int imgHeight;

    public ImageProcessing(String path, boolean withEntropy){
        try {
            this.img = ImageIO.read(new File(path));
        } catch (IOException e) {
            System.out.println(e);
            System.out.println("error in reading image from: " + path);
        }
        this.imgHeight = img.getHeight();
        this.imgWidth = img.getWidth();
        setRgb();
        setEnergy();
        if (withEntropy) setEntropy();
    }

    public ImageProcessing(BufferedImage image, boolean withEntropy){
        this.img = image;
        this.imgHeight = image.getHeight();
        this.imgWidth = image.getWidth();
        setRgb();
        setEnergy();
        if (withEntropy) setEntropy();
    }

    public ImageProcessing transpose(){
        int i,j,k,h,w;
        BufferedImage transposed = new BufferedImage(imgHeight, imgWidth, img.getType());
        for(i=0; i<imgWidth; i++){
            for(j=0;j<imgHeight;j++){
                transposed.setRGB(j,i, img.getRGB(i,j));
            }
        }
        boolean withEntropy =  entropy == null;
        ImageProcessing res = new ImageProcessing(transposed,withEntropy);
        return res;
    }

    private void setRgb(){
        int local,i,j;
        rgb = new int[imgHeight][imgWidth][4];
        // get rgb
        for(i=0; i<imgHeight; i++) {
            for (j = 0; j < imgWidth; j++){
                local = img.getRGB(j, i);
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
    }

    public void setEnergy(){
        int i,j;
        energy = new double[imgHeight][imgWidth];
        // compute energy
        for(i=0; i<imgHeight; i++) {
            for (j = 0; j < imgWidth; j++) {
                energy[i][j] = computeEnergy(i,j,rgb);
            }
        }
    }

    private static double computeEnergy(int x,int y, int[][][] rgb){
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

    public int findStraightSeam(){
        int i,j;
        double[] weights = new double[imgWidth];
        for(i=0; i<imgWidth; i++){
            for(j=0; j<imgHeight; j++){
                weights[i] += energy[j][i];
            }
        }
        i = IntStream.range(0,imgWidth)
                .reduce((a,b) -> weights[a] < weights[b] ? a : b)
                .getAsInt();
        return i;
    }

    private double[][] computeEntropyHelper() {
        int x,y,i,j;
        double sum;
        double[][] pmn = new double[imgHeight][imgWidth];
        for(x=0;x<imgHeight;x++){
            for(y=0; y<imgWidth; y++){
                sum = 0;
                for(i=-4;i<5; i++){
                    if (x+i<0 || x+i>=imgHeight){ continue;}
                    for(j=-4; j<5;j++) {
                        if (y + j >= imgWidth || y + j < 0) {continue;}
                        sum += rgb[x+i][y+j][3];
                    }
                }
                pmn[x][y] = (double)rgb[x][y][3] / sum;
            }
        }
        return pmn;
    }

    public void setEntropy() {
        int x,y,i,j;
        double[][] pmn = computeEntropyHelper();
        entropy = new double[imgHeight][imgWidth];
        for(x=0;x<imgHeight;x++) {
            for (y = 0; y < imgWidth; y++) {
                entropy[x][y] = 0;
                for(i=-4;i<5; i++){
                    if (x+i<0 || x+i>=imgHeight){ continue;}
                    for(j=-4; j<5;j++) {
                        if (y + j >= imgWidth || y + j < 0) {continue;}
                        entropy[x][y] -= pmn[x+i][y + j]*log(pmn[x+i][y + j]);
                    }
                }
            }
        }
    }

}

//        try {
//                File outputfile = new File("/Users/orrbarkat/repos/java/seamsCarving/images/saved.jpg");
//
//                ImageIO.write(transposed, "jpg", outputfile);
//                }catch (IOException e) {
//                System.out.println(e);
//                System.exit(1);
//                }