package com.vision;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by orrbarkat on 19/04/2017.
 */
public class ImageProcessing {
    private BufferedImage img = null;
    private int[][][] energy = null;
    private int width;
    private int hight;

    public ImageProcessing(String path){
        try {
            this.img = ImageIO.read(new File(path));
        } catch (IOException e) {
            System.out.println(e);
            System.out.println("error in reading image from: " + path);
        }
        this.hight = img.getHeight();
        this.width = img.getWidth();
    }

    public boolean transpose(){
        return true;
    }


}
