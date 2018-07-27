package me.fponzi.legolab;


import android.util.Log;

import org.opencv.core.Scalar;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles the state of the Legobot
 */
public class Legobot {

    private static final String TAG = Legobot.class.getSimpleName();

    public enum Analysis {
        NO_DETECTION(new Scalar(0, 0, 0)), //nothing detected.
        SEMAPHORE_RED(new Scalar(255, 0, 0)),
        SEMAPHORE_GREEN(new Scalar(83, 244, 66)),
        SIGN_STOP(new Scalar(0, 255, 0));

        public Scalar debugColor;

        Analysis(Scalar debugColor) {
            this.debugColor = debugColor;
        }
    }

    /** It will hold the findings of the last {@see ConnectionHandler.N_MILLISECS} milliseconds.*/
    private HashMap<Analysis, Integer> findings;


    Legobot() {
        findings = new HashMap<>();
        resetFindings();
    }

    public void addAnalysis(Analysis a) {
        Log.i(TAG, "Adding analysis : " + a);
        findings.put(a, findings.get(a) + 1);
    }

    private void resetFindings() {
        for (Analysis a : Analysis.values()) {
            findings.put(a, 0);
        }
    }

    public Analysis getAnalysis()
    {
        //printFindings();
        Analysis analysis = getMaxAnalysis();
        Log.i(TAG, "Max analyusis found: " + analysis);

        resetFindings();
        return analysis;
    }


    private void printFindings() {
        String s = "Printing findings: { ";
        for (Map.Entry<Analysis, Integer> e : findings.entrySet()) {
            s += e.getKey() + ": " + e.getValue() + ", ";
        }
        Log.i(TAG, s + "} ");
    }


    private Analysis getMaxAnalysis() {
        Map.Entry<Analysis, Integer> maxEntry = null;
        for (Map.Entry<Analysis, Integer> entry : findings.entrySet()) {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                maxEntry = entry;
            }
        }
        assert maxEntry != null;
        //if we found more than 10 times the same object in the last n millisecond, then send it.
        if (maxEntry.getValue() > 1) {
            return maxEntry.getKey();
        }
        return Analysis.NO_DETECTION;

    }

}