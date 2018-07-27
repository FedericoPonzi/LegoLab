package me.fponzi.legolab;

import android.content.Context;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Utils {


    private static final String TAG = Utils.class.getSimpleName();

    public static void cleanMorph(Mat image)
    {
        Imgproc.erode(image, image, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2,2)));
        Imgproc.dilate(image, image, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2,2)));

    }
    /**
     * Copy a resource from raw, into the app's private directory.
     * This is needed because we cannot access raw package (it's inside the apk) and we need
     * a absolutePath.
     *
     * @param context
     * @param resourceId
     * @param filename   used for the filename
     * @return the resource path
     * @throws IOException
     */
    public static String copyFromRaw(Context context, int resourceId, String filename) throws IOException {
        InputStream is = context.getResources().openRawResource(resourceId);

        File cascadeDir = new File(Environment.getExternalStorageDirectory().toString() + "/cascade");
        cascadeDir.mkdir();

        File mCascadeFile = new File(cascadeDir, filename);
        FileOutputStream os = new FileOutputStream(mCascadeFile);

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        is.close();
        os.close();

        Log.i(TAG, "Classifier: " + filename + " loaded, file path: " + mCascadeFile.getAbsolutePath());
        return mCascadeFile.getAbsolutePath();
    }

    public static void errorAlertDialog(Context context, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);
        alertDialogBuilder
                .setTitle("Error")
                .setMessage(message)
                .setCancelable(false).create().show();
    }

}
