package me.fponzi.legolab.detectors;

import android.content.Context;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import me.fponzi.legolab.Legobot;
import me.fponzi.legolab.MainActivity;

public abstract class AbstractDetector
{
    private static final String TAG = AbstractDetector.class.getSimpleName();
    final Legobot legobot;
    private final Context context;
    boolean debug;

    AbstractDetector(Context context, Legobot legobot, boolean debug)
    {
        this.legobot = legobot;
        this.context = context;
        this.debug = debug;
    }
    public abstract Legobot.Analysis detect(Mat mRgba, Mat mGray);

    void draw(Mat mRgb, Mat circles, String nameForLog, Scalar color)
    {
        if(!debug) return;
        logDet(nameForLog, circles.cols());


        for(int i = 0; i < circles.cols(); i++)
        {
            double[] circle = circles.get(0, i);
            Point pt = new Point(Math.round(circle[0]), Math.round(circle[1]));
            int radius = (int) Math.round(circle[2]);
            Imgproc.circle(mRgb, pt, radius, color, 3);
        }
    }
    private void logDet(String name, int size)
    {
        if(size > 0) Log.i(TAG, "[" + name + "]: detected " + size);
    }
    void draw(Mat mRgba, MatOfRect detected, String nameForLog, Scalar color) {
        if(!debug) return;
        logDet(nameForLog, detected.toArray().length);

        for (Rect r : detected.toArray()) {
            Point center = new Point();
            int radius;
            double aspect_ratio = (double) r.width / r.width;
            if (0.75 < aspect_ratio && aspect_ratio < 1.3) {
                center.x = Math.round((r.x + r.width * 0.5) * MainActivity.SCALE);
                center.y = Math.round((r.y + r.height * 0.5) * MainActivity.SCALE);
                radius = (int) Math.round((r.width + r.height) * 0.25 * MainActivity.SCALE);
                Imgproc.circle(mRgba, center, radius, color, 3, 8, 0);
            } else {
                Imgproc.rectangle(mRgba, new Point(Math.round(r.x * MainActivity.SCALE), Math.round(r.y * MainActivity.SCALE)),
                        new Point(Math.round((r.x + r.width - 1) * MainActivity.SCALE), Math.round((r.y + r.height - 1) * MainActivity.SCALE)),
                        color, 3, 8, 0);
            }
        }
    }
}
