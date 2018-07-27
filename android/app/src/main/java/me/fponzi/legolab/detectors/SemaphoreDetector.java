package me.fponzi.legolab.detectors;

import android.content.Context;
import android.util.Pair;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import me.fponzi.legolab.Legobot;
import me.fponzi.legolab.Utils;

public class SemaphoreDetector extends AbstractDetector {
    private static final String TAG = SemaphoreDetector.class.getSimpleName();

    private List<Pair<Scalar, Scalar>> redColors;
    private List<Pair<Scalar, Scalar>> greenColors;

    public SemaphoreDetector(Context context, Legobot legobot, boolean debug) {
        super(context, legobot, debug);
        redColors = new ArrayList<>();
        greenColors = new ArrayList<>();

        redColors.add(new Pair<>(new Scalar(0, 95, 100),
                new Scalar(10, 255, 255)));

        redColors.add(new Pair<>(new Scalar(170, 95, 100),
                new Scalar(180, 255, 255)));

        greenColors.add(new Pair<>(new Scalar(28, 80, 100),
                new Scalar(90, 255, 255)));


    }

    private Legobot.Analysis detectColor(List<Pair<Scalar, Scalar>> colors, Mat mRgb, Mat mGrey,
                                         String nameForLog, Legobot.Analysis detectedTrafficColor) {
        Mat hsv = new Mat();
        Imgproc.cvtColor(mRgb, hsv, Imgproc.COLOR_RGB2HSV);

        Mat mask1, mask2;
        mask1 = new Mat();
        mask2 = new Mat();

        Imgproc.GaussianBlur(hsv, hsv, new Size(5, 5), 0);
        Imgproc.medianBlur(hsv, hsv, 5);

        // FOR SIMPLICITY - should have just duplicated code insteaf of overengineering :(
        if (detectedTrafficColor.equals(Legobot.Analysis.SEMAPHORE_RED)) {
            Core.inRange(hsv, redColors.get(0).first,
                    redColors.get(0).second, mask1);
            Core.inRange(hsv, redColors.get(1).first,
                    redColors.get(1).second, mask2);
            Core.bitwise_or(mask1, mask1, hsv);
            //hsv.copyTo(mRgb);
        }
        else
        {
            Core.inRange(hsv, greenColors.get(0).first,
                    greenColors.get(0).second, mask1);
            Core.bitwise_or(mask1, mask1, hsv);
            //hsv.copyTo(mRgb);
        }

        Utils.cleanMorph(hsv);


        Mat circles = new Mat();

        if(detectedTrafficColor.equals(Legobot.Analysis.SEMAPHORE_GREEN))
        Imgproc.HoughCircles(hsv, circles, Imgproc.HOUGH_GRADIENT, 2,
                hsv.size().height / 4, 100, 30, (int)hsv.size().height/24,
                (int) hsv.size().height/15);
        else{
            Imgproc.HoughCircles(hsv, circles, Imgproc.HOUGH_GRADIENT, 2,
                    hsv.size().height / 4, 100, 30, (int)hsv.size().height/19,
                    (int) hsv.size().height/15);

        }
        /* Setting the result. */
        Legobot.Analysis toRet = detectedTrafficColor;
        /* If there are no detected circles
        Or there is more then one, then this is probably not a red semaphore.
        This second statement is done for not detect stop signs.
         */
        if(circles.cols() == 0 || circles.cols() > 1)
            toRet = Legobot.Analysis. NO_DETECTION;
        else
                draw(mRgb, circles, "Semaphore" + nameForLog, detectedTrafficColor.debugColor);

        mask1.release();
        mask2.release();
        circles.release();
        hsv.release();
        return toRet;
    }



    @Override
    public Legobot.Analysis detect(Mat mRgb, Mat mGrey) {

        Legobot.Analysis ret = detectColor(greenColors, mRgb, mGrey, "Green", Legobot.Analysis.SEMAPHORE_GREEN);
        //return detectColor(redColors, mRgb, mGrey, "Red", Legobot.Analysis.SEMAPHORE_RED);
        //return ret;// ret.equals(Legobot.Analysis.SEMAPHORE_GREEN) ? ret : detectColor(redColors, mRgb, mGrey, "Red", Legobot.Analysis.SEMAPHORE_RED);

       if(ret.equals(Legobot.Analysis.SEMAPHORE_GREEN))
           return Legobot.Analysis.SEMAPHORE_GREEN;
        return detectColor(redColors, mRgb, mGrey, "Red", Legobot.Analysis.SEMAPHORE_RED);

    }

}