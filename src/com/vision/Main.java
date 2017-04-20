package com.vision;



public class Main {

    private static int outputNumOfCols;
    private static int outputNumOfRows;

    public static void main(String[] args) {
        String path = args[0];
        outputNumOfCols = Integer.valueOf(args[1]);
        outputNumOfRows = Integer.valueOf(args[2]);

        System.out.println("hello");

        ImageProcessing img = new ImageProcessing(path,false);
//        ImageProcessing trans = img.transpose();

        System.out.println("done calculating!");

        int[][] vertSeam = img.maskFromStraightSeams(100);

        img.removeSeams(vertSeam).saveImage("/Users/orrbarkat/repos/java/seamsCarving/images/straight_seam_1.jpg");
        //for vertical seams
       int[][] seams = img.computeAllOptimalSeams(100);
        //for horizontal seams we need to transpose the matrix and then use this function
//        computeAllOptimalSeams(computeEnergyWithDynamicProg(energy),imgHeight-outputNumOfRows);
        img.removeSeams(seams).saveImage("/Users/orrbarkat/repos/java/seamsCarving/images/general_seam_1.jpg");

    }
}
