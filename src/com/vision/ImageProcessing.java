package com.vision;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.IntStream;

import static java.lang.Math.*;



public class ImageProcessing {
    private BufferedImage img = null;
    private int[][][] rgb;
    private double[][] energy;
    private double[][] energyAfterDynamicProg;
    private double[][] entropy = null;
    private int imgWidth;
    private int imgHeight;

    public int getWidth()
    {
        return imgWidth;
    }

    public int getHeight()
    {
        return imgHeight;
    }


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
        return new ImageProcessing(transposed,isEntropy());
    }

    public ImageProcessing removeSeams(int[][] mask){
        int i, j, color, index;
        int excess = Arrays.stream(mask[0])
                .reduce(0,(a,b) -> b > 0 ? a+1 : a);
        BufferedImage res = new BufferedImage(imgWidth - excess ,imgHeight, img.getType());
        for(i=0; i<imgHeight; i++){
            index = 0;
            for (j=0; j<imgWidth; j++){
                if(mask[i][j] > 0){
                    continue;
                }
                color = img.getRGB(j,i);
                res.setRGB(index,i,color);
                index++;
            }
        }
        return new ImageProcessing(res,isEntropy());
    }

    public int[][] maskFromStraightSeams(int excess){
        int i, j, col;
        assert excess >= imgWidth;
        int[][] mask = new int[imgHeight][imgWidth];
        for(i=0; i<excess; i++){
            col = findStraightSeam();
            for(j=0; j<imgHeight; j++){
                energy[j][col] = Double.MAX_VALUE;
                mask[j][col] = i+1;
            }
        }
        return mask;
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
                    sum += abs(rgb[x+i][y+j][k] - rgb[x][y][k]);
                    denominator++;
                }
            }
        }
        return (sum != 0.0 ? sum : 0.001)/denominator;
    }

    public int findStraightSeam(){
        int i,j;
        double[] weights = new double[imgWidth];
        for(i=0; i<imgWidth; i++){
            if(energy[0][i] == Double.MAX_VALUE){
                weights[i] = Double.MAX_VALUE;
                continue;
            }
            for(j=0; j<imgHeight; j++){
                weights[i] += energy[j][i];
            }
        }
        i = IntStream.range(0,weights.length)
                .reduce(0,(a,b) -> weights[a] <= weights[b] ? a : b);
        return i;
    }

    private double[][] computeEntropyHelper() {
        int x,y,i,j;
        double sum;
        double[][] pmn = new double[imgHeight][imgWidth];
        for(x=0;x<imgHeight;x++){
            for(y=0; y<imgWidth; y++){
                sum = 0;
                if (rgb[x][y][3] == 0){
                    pmn[x][y] = Double.MIN_NORMAL;
                    continue;
                }
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
                energy[x][y] += entropy[x][y]/3;
            }
        }
    }

    public boolean isEntropy(){
        return entropy != null;
    }

    public int[][] computeAllOptimalSeams(int numOfSeams) {
        int[][] seams = new int[imgHeight][imgWidth];
        for (int i = 1; i < numOfSeams + 1; i++) {
            energyAfterDynamicProg = computeEnergyWithDynamicProg();
            seams = computeOptimalSeam(seams, i);
        }
        return seams;
    }

    private int[][] computeOptimalSeam(int[][] seams, int nextSeamIndex) {
        //find minimal pixel in bottom row
        double minVal = Double.MAX_VALUE;
        int minIndex = 0;
        for (int i = 0; i < imgWidth; i++) {
            if (seams[imgHeight - 1][i] != 0) continue;
            minVal = min(minVal, energyAfterDynamicProg[imgHeight - 1][i]);
            minIndex = (minVal == energyAfterDynamicProg[imgHeight - 1][i]) ? i : minIndex;
        }

        seams[imgHeight - 1][minIndex] = nextSeamIndex;

        for (int i = imgHeight - 2; i >= 0; i--) {
            //check if one of the 3 pixels above are already taken by another seam
            double leftPixel=Double.MAX_VALUE,rightPixel=Double.MAX_VALUE;
            //make sure we're in bounds of the array
            if (minIndex - 1 >= 0)
            {
                leftPixel = seams[i][minIndex - 1] == 0 ? energyAfterDynamicProg[i][minIndex - 1] : Double.MAX_VALUE;
            }
            if (minIndex + 1 < imgWidth)
            {
                rightPixel = seams[i][minIndex + 1] == 0 ? energyAfterDynamicProg[i][minIndex + 1] : Double.MAX_VALUE;
            }
            double midPixel = seams[i][minIndex] == 0 ? energyAfterDynamicProg[i][minIndex] : Double.MAX_VALUE;

            double minTopPixel = min(leftPixel, min(rightPixel, midPixel));
            //we're blocked
            if (minTopPixel == Double.MAX_VALUE)
            {
                for (int f=2 ; f<imgWidth ; f++)
                {
                    if (minIndex - f >= 0)
                    {
                        if (seams[i][minIndex - f] == 0)
                        {
                            seams[i][minIndex - f] = nextSeamIndex;
                            minIndex-=f;
                            break;
                        }
                    }
                    if (minIndex + f < imgWidth)
                    {
                        if (seams[i][minIndex + f] == 0)
                        {
                            seams[i][minIndex + f] = nextSeamIndex;
                            minIndex += f;
                            break;
                        }
                    }
                }
                continue;
            }

            if (minTopPixel == leftPixel)
            {
                seams[i][minIndex - 1] = nextSeamIndex;
                energy[i][minIndex - 1] = Double.MAX_VALUE;
                minIndex--;
            }
            else if (minTopPixel == rightPixel)
            {
                seams[i][minIndex + 1] = nextSeamIndex;
                energy[i][minIndex + 1] = Double.MAX_VALUE;
                minIndex++;
            }
            else
            {
                seams[i][minIndex] = nextSeamIndex;
                energy[i][minIndex] = Double.MAX_VALUE;

            }
        }

        return seams;
    }

    private double[][] computeEnergyWithDynamicProg() {
        double[][] result = new double[imgHeight][imgWidth];
        System.arraycopy(energy[0], 0, result[0], 0, imgWidth);

        for (int i = 1; i < imgHeight; i++)//start from second row, hence start from i=1
        {
            //compute the left edge first
            result[i][0] = energy[i][0] + min(result[i - 1][0], result[i - 1][1]);
            //compute the rest of the row
            for (int j = 1; j < imgWidth - 1; j++) {
                result[i][j] = energy[i][j] + min(result[i - 1][j - 1], min(result[i - 1][j], result[i - 1][j + 1]));
            }
            //compute right edge
            result[i][imgWidth - 1] = energy[i][0] + min(result[i - 1][imgWidth - 1], result[i - 1][imgWidth - 2]);

        }
        return result;
    }

    public void saveImage(String path){
        try {
            File outputfile = new File(path);
            ImageIO.write(img, "jpg", outputfile);
        }catch (IOException e) {
            System.out.println(e);
            System.exit(1);
        }
    }

    public ImageProcessing enlargeBySeams(int[][] mask, boolean smartMerge){
        int i, j, color, index;
        int seamsToAdd = Arrays.stream(mask[0])
                .reduce(0,(a,b) -> b > 0 ? a+1 : a);
        BufferedImage res = new BufferedImage(imgWidth + seamsToAdd ,imgHeight, img.getType());
        for(i=0; i<imgHeight; i++){
            index = 0;
            for (j=0; j<imgWidth; j++){
                if(mask[i][j] > 0){
                    color =  (smartMerge && j!=0) ? img.getRGB(j,i) : packRgbToInt(rgb[i][j-1], rgb[i][j]);
                    res.setRGB(index,i,color);
                    index++;
                }
                if(index>=imgWidth + seamsToAdd){
                    System.out.println("Asdfa");
                }
                color = img.getRGB(j,i);
                res.setRGB(index,i,color);
                index++;
            }
        }
        return new ImageProcessing(res,isEntropy());
    }

    private static int packRgbToInt(int[] rgbLeft, int[] rgbRight){
        int i;
        int res = 0xff;
        for(i=0; i<3; i++){
            res = res<<8;
            res += round((rgbLeft[i]+rgbRight[i])/2);
        }
        return res;
    }

}