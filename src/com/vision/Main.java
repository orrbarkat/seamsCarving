package com.vision;


import java.util.stream.IntStream;
import static java.lang.Math.*;

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

        //straight_seam_1
        ImageProcessing imgTiger = new ImageProcessing("c:\\Users\\Oded_navon\\Documents\\GitHub\\Graphics1\\seamsCarving\\images\\Tiger.jpg",false);
        int[][] vertSeam1 = imgTiger.maskFromStraightSeams(150);
        imgTiger.removeSeams(vertSeam1).saveImage("c:\\Users\\Oded_navon\\Documents\\GitHub\\Graphics1\\seamsCarving\\images\\straight_seam_1.jpg");

        //General seam 1
        int[][] seams1 = imgTiger.computeAllOptimalSeams(150,false);
        imgTiger.removeSeams(seams1).saveImage("c:\\Users\\Oded_navon\\Documents\\GitHub\\Graphics1\\seamsCarving\\images\\general_seam_1.jpg");

        //straight_seam_2
        ImageProcessing imgCar = new ImageProcessing("c:\\Users\\Oded_navon\\Documents\\GitHub\\Graphics1\\seamsCarving\\images\\Car.jpg",false);
        int[][] vertSeam2 = imgCar.maskFromStraightSeams(150);
        imgCar.removeSeams(vertSeam2).saveImage("c:\\Users\\Oded_navon\\Documents\\GitHub\\Graphics1\\seamsCarving\\images\\straight_seam_2.jpg");

        //General seam 2
        int[][] seams2 = imgCar.computeAllOptimalSeams(150,false);
        imgCar.removeSeams(seams2).saveImage("c:\\Users\\Oded_navon\\Documents\\GitHub\\Graphics1\\seamsCarving\\images\\general_seam_2.jpg");

        //orig 1
        imgTiger.enlargeBySeams(vertSeam1,false).saveImage("c:\\Users\\Oded_navon\\Documents\\GitHub\\Graphics1\\seamsCarving\\images\\orig_1.jpg");
        //orig 2
        imgCar.enlargeBySeams(vertSeam1,false).saveImage("c:\\Users\\Oded_navon\\Documents\\GitHub\\Graphics1\\seamsCarving\\images\\orig_2.jpg");
        //interp 1
        imgTiger.enlargeBySeams(vertSeam1,true).saveImage("c:\\Users\\Oded_navon\\Documents\\GitHub\\Graphics1\\seamsCarving\\images\\interp_1.jpg");
        //interp 2
        imgCar.enlargeBySeams(vertSeam1,true).saveImage("c:\\Users\\Oded_navon\\Documents\\GitHub\\Graphics1\\seamsCarving\\images\\interp_2.jpg");
        //forward 1
        int[][] seams3 = imgTiger.computeAllOptimalSeams(150,true);
        imgTiger.removeSeams(seams3).saveImage("c:\\Users\\Oded_navon\\Documents\\GitHub\\Graphics1\\seamsCarving\\images\\forward_1.jpg");
        //forward 2
        int[][] seams4 = imgCar.computeAllOptimalSeams(150,true);
        imgCar.removeSeams(seams4).saveImage("c:\\Users\\Oded_navon\\Documents\\GitHub\\Graphics1\\seamsCarving\\images\\forward_2.jpg");

//        switch(energyType)
//        {
//            // Regular energy without entropy
//            case 0:
//                img = new ImageProcessing(inputPath,false);
//                computeHorizontalChange(false);
//                computeVerticalChange(false);
//                img.saveImage(outputPath);
//                break;
//            // Regular energy with entropy
//            case 1:
//                img = new ImageProcessing(inputPath,true);
//                computeHorizontalChange(false);
//                computeVerticalChange(false);
//                img.saveImage(outputPath);
//                break;
//            // Forward energy without entropy
//            case 2:
//                img = new ImageProcessing(inputPath,false);
//                computeHorizontalChange(true);
//                computeVerticalChange(true);
//                img.saveImage(outputPath);
//                break;
//        }
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
            curretResize = min(numOfSeams, round(img.getWidth() / 3));
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
