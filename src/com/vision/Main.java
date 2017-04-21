package com.vision;


import java.util.stream.IntStream;

public class Main {

    private static int outputNumOfCols;
    private static int outputNumOfRows;

    public static void main(String[] args) {
        String path = args[0];
        outputNumOfCols = Integer.valueOf(args[1]);
        outputNumOfRows = Integer.valueOf(args[2]);

        System.out.println("hello");

        // without entropy
        ImageProcessing img = new ImageProcessing(path,false);
//        ImageProcessing trans = img.transpose();

        System.out.println("done calculating!");

        //for straight vertical seams
        int[][] vertSeam = img.maskFromStraightSeams(200);

        img.removeSeams(vertSeam).saveImage("c:\\Users\\Oded_navon\\Documents\\GitHub\\Graphics1\\seamsCarving\\images\\straight_seam_1.jpg");


        //for vertical seams
        //for general vertical seams
        int[][] seams = img.computeAllOptimalSeams(200);

        //for horizontal seams we need to transpose the matrix and then use this function
//        computeAllOptimalSeams(computeEnergyWithDynamicProg(energy),imgHeight-outputNumOfRows);
        img.removeSeams(vertSeam).saveImage("/Users/orrbarkat/repos/java/seamsCarving/images/straight_seam_1.jpg");
        img.removeSeams(seams).saveImage("/Users/orrbarkat/repos/java/seamsCarving/images/general_seam_1.jpg");
        img.enlargeBySeams(seams,false).saveImage("/Users/orrbarkat/repos/java/seamsCarving/images/orig_1.jpg");

        // with entropy
        img = new ImageProcessing(path,true);

        vertSeam = img.maskFromStraightSeams(200);

        img.removeSeams(vertSeam).saveImage("c:\\Users\\Oded_navon\\Documents\\GitHub\\Graphics1\\seamsCarving\\images\\straight_seam_2.jpg");


        //for vertical seams
        seams = img.computeAllOptimalSeams(200);

        img.enlargeBySeams(seams,false).saveImage("/Users/orrbarkat/repos/java/seamsCarving/images/orig_1_entropy.jpg");

        img.enlargeBySeams(seams,true).saveImage("/Users/orrbarkat/repos/java/seamsCarving/images/interp_1_entropy.jpg");

        //for vertical seams

        //for horizontal seams we need to transpose the matrix and then use this function
//        computeAllOptimalSeams(computeEnergyWithDynamicProg(energy),imgHeight-outputNumOfRows);
        img.removeSeams(seams).saveImage("c:\\Users\\Oded_navon\\Documents\\GitHub\\Graphics1\\seamsCarving\\images\\general_seam_2.jpg");

    }
}
