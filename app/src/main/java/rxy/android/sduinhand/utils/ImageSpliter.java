package rxy.android.sduinhand.utils;
import android.graphics.Bitmap;

import rxy.android.sduinhand.constants.Numbers;

/**
 * util class:
 * used to splite the numberpad bitmap into numbers based on some pre-obtained data
 * coord:
 * $0 -> 4,3,29,28  +4 w=25
 * $1 -> 33,3,58,28 +4 w=25
 * $2 -> 62,3,88,28 +4 w=26
 * $3 -> 92,3,118,28 +4 w=26
 * $4 -> 122,3,148,28 +4 w=26
 * $5 -> 152,3,178,28 +4 w=26
 * $6 -> 182,3,207,28 +4 w=25
 * $7 -> 211,3,236,28 +4 w=25
 * $8 -> 241,3,266,28 +4 w=26
 * $9 -> 270,3,295,28 +4 w=25
 */

public class ImageSpliter {
    public static final String TAG = "ImageSpliter";
    public static String SpliteBitmap2Numbers(Bitmap numberpad){
        String result = "";
        int start_x = 0,width = 25;
        int start_y = 3,height = 28;
        for (int i=0;i<10;i++){
            start_x += 4;
            if(i == 0 || i == 1 ||i == 6 ||i == 7 || i == 9)
               width = 25;
            else
               width = 26;
            Bitmap tmp = Bitmap.createBitmap(numberpad,start_x,start_y,width,height);
            int[][] grey = convertGrayAndBinaryBitmap(tmp);
            int res = CheckWhichNumber(grey);
            result+=res;
            start_x += width;
        }
        return result;
    }

    private static int[][] convertGrayAndBinaryBitmap(Bitmap normalBitmap){
        int width = normalBitmap.getWidth();
        int height = normalBitmap.getHeight();
        int[][] result = new int[height][width];
        int[] pixels = new int[width * height];
        normalBitmap.getPixels(pixels,0,width,0,0,width,height);
        //final int alpha = 0xFF << 24;
        final int threadhold = 100;
        for (int i = 0;i < height;i++){
            for (int j = 0;j < width;j++){
                 int rgb = pixels[width * i+j];
                 int r = ((rgb & 0x00FF0000)>>16);
                 int g = ((rgb & 0x0000FF00)>>8);
                 int b = ((rgb & 0x000000FF));
                 //two ways
                 //one.to cal the average of R,G,B to get GREY
                 //two.use YUV Format gray = 0.3R + 0.59G + 0.11B
                 //int grey = (int)(0.3*r + 0.59*g + 0.11*b);
                 int grey = (int)((r+g+b)/3.0);
                 //grey = alpha | (grey << 16) | (grey << 8)| grey;
                 if(grey  > threadhold) grey = 1;
                 else grey = 0;
                 result[i][j] = grey;
//System.out.print(grey+",");
            }
//System.out.println();
        }
        return result;
    }

    private static int CheckWhichNumber(int[][] data){
        int x = 0;
        int y = 0;
        //寻找数字矩阵的起始点
        looper_y : for(int i=0;i<data.length;i++){
            for (int j=0;j< data[0].length;j++){
                if(data[i][j] == 0){
                    y = i;
                    break  looper_y;
                }
            }
        }
        looper_x : for (int i=0;i < data[0].length;i++){
            for (int j=0;j<data.length;j++){
                if(data[j][i] == 0){
                    x = i;
                    break looper_x;
                }
            }
        }

        double max_match = 0.0;
        int result = 0;
        for (int k=0;k<10;k++){
            int equalNum = 0;
            int target_0_count = 0;
            int model_0_count = 0;
            for (int i=y;i<y+9;i++){
                for (int j=x;j<x+5;j++){
                    if(data[i][j] == 0)
                        target_0_count++;
                    if(Numbers.NUM[k][i-y][j-x] == 0)
                        model_0_count++;
                    if(data[i][j] == 0 && Numbers.NUM[k][i-y][j-x] == 0){
                        equalNum++;
                    }
                }
            }
            double ratio = equalNum / (1.0 * target_0_count);

            if(ratio > max_match){
                max_match = ratio;
                result = k;
            }
        }
        return result;
    }
}
