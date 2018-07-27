package me.fponzi.legolab.detectors;

import android.content.Context;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.io.IOException;

import me.fponzi.legolab.Legobot;
import me.fponzi.legolab.R;
import me.fponzi.legolab.Utils;

import static android.support.constraint.Constraints.TAG;

public class StopDetector extends AbstractDetector {
    private CascadeClassifier mClassifier;


    public StopDetector(Context context, Legobot legobot, boolean debug) {
        super(context, legobot, debug);
        try {
            // load cascade file from application resources
            String stopSignPath = Utils.copyFromRaw(context, R.raw.stopsign_haar, "stop_sign_haar");
            mClassifier = new CascadeClassifier(stopSignPath);
            mClassifier.load(stopSignPath); // needed, the constructor is bugged.


            if (mClassifier.empty()) {
                Log.e(TAG, "Failed to load cascade classifiers.");
                Utils.errorAlertDialog(context,
                        "Failed to load cascade mStopDetector:");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
        }
    }

    @Override
    public Legobot.Analysis detect(Mat mRgba, Mat mGray) {

        MatOfRect stopDetected = new MatOfRect();

        mClassifier
                .detectMultiScale(mGray, stopDetected,
                        1.1, 2, Objdetect.CASCADE_SCALE_IMAGE
                                | Objdetect.CASCADE_FIND_BIGGEST_OBJECT,
                        new Size(300, 300), new Size(480, 480));

        draw(mRgba, stopDetected, "StopDetected", Legobot.Analysis.SIGN_STOP.debugColor);
        Legobot.Analysis toRet = stopDetected.toArray().length == 0 ? Legobot.Analysis.NO_DETECTION :
                Legobot.Analysis.SIGN_STOP;

        stopDetected.release();

        return toRet;
    }

}
