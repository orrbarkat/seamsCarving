package com.vision;


import java.util.stream.IntStream;
import static java.lang.Math.*;
import com.vision.ImageProcessing;

public class Main {

    private static int outputNumOfCols;
    private static int outputNumOfRows;
    private static int energyType;
    private static String inputPath;
    private static String outputPath;
    private static ImageProcessing img;

    public static void main(String[] args) {
        inputPath = args[0];
        outputNumOfCols = Integer.valueOf(args[1]);
        outputNumOfRows = Integer.valueOf(args[2]);
        energyType = Integer.valueOf(args[3]);
        outputPath = args[4];

        switch(energyType)
        {
            // Regular energy without entropy
            case 0:
                img = new ImageProcessing(inputPath,false);
                computeHorizontalChange(false);
                computeVerticalChange(false);
                img.saveImage(outputPath);
                break;
            // Regular energy with entropy
            case 1:
                img = new ImageProcessing(inputPath,true);
                computeHorizontalChange(false);
                computeVerticalChange(false);
                img.saveImage(outputPath);
                break;
            // Forward energy without entropy
            case 2:
                img = new ImageProcessing(inputPath,false);
                computeHorizontalChange(true);
                computeVerticalChange(true);
                img.saveImage(outputPath);
                break;
        }
    }


    private static void computeHorizontalChange(boolean Forward)
    {
        int numOfSeams = abs(outputNumOfCols-img.getWidth());
        int curretResize;
        while(numOfSeams >0){
            curretResize = min(numOfSeams,round(img.getWidth()/3));
            int[][] seams = img.computeAllOptimalSeams(curretResize,Forward);
            if (outputNumOfCols > img.getWidth())
            {

                img = img.enlargeBySeams(seams,true);
            }
            //Narrow image
            else
            {
                img = img.removeSeams(seams);
            }
            numOfSeams-=curretResize;
        }
    }

    private static void computeVerticalChange(boolean forward)
    {
        int curretResize;
        int numOfSeams = abs(outputNumOfRows-img.getHeight());
        img = img.transpose();
        while(numOfSeams >0) {
            curretResize = min(numOfSeams, round(img.getWidth() / 4));
            int[][] seams = img.computeAllOptimalSeams(curretResize, forward);
            //Enlarge image
            if (outputNumOfRows > img.getWidth()) {
                img = img.enlargeBySeams(seams, true);
            }
            //Narrow image
            else {
                img = img.removeSeams(seams);
            }
            numOfSeams-=curretResize;
        }
        img = img.transpose();
    }

}
