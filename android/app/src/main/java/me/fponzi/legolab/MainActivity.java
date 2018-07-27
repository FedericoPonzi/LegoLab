package me.fponzi.legolab;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

import me.fponzi.legolab.detectors.AbstractDetector;
import me.fponzi.legolab.detectors.SemaphoreDetector;
import me.fponzi.legolab.detectors.StopDetector;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = MainActivity.class.getCanonicalName();
    public static final double SCALE = 1d;

    private CameraBridgeViewBase mOpenCvCameraView;

    private Mat mRgba;
    private Mat mGray;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    try {
                        initializeOpenCVDependencies();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
    private Legobot legobot;
    private AbstractDetector[] mDetectors;

    private void initializeOpenCVDependencies() throws IOException {

        mDetectors = new AbstractDetector[]{
                new SemaphoreDetector(this, null, true),
                new StopDetector(this, null, true)};

        mOpenCvCameraView.enableView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        this.legobot = new Legobot();
        new Thread(new ConnectionHandler(legobot)).start();


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            Log.i(TAG, "Requesting camera permissions because are: " +
                    ContextCompat.checkSelfPermission(this,
                            Manifest.permission.CAMERA));
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 13);
            return;
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {
            Log.i(TAG, "Requesting write external storage permission. ");
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 131);
            return;
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mOpenCvCameraView = findViewById(R.id.tutorial1_activity_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            Snackbar.make(findViewById(R.id.container), "Permission Granted",
                    Snackbar.LENGTH_LONG).show();

        } else {

            Snackbar.make(findViewById(R.id.container), "Permission denied",
                    Snackbar.LENGTH_LONG).show();

        }
    }

    @Override
    public void onResume() {

        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
    @Override
    public void onPause() {

        super.onPause();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat(height, width, CvType.CV_8UC4);
        mRgba = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }



    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mGray = inputFrame.gray();
        mRgba = inputFrame.rgba();


        Core.rotate(mGray, mGray, Core.ROTATE_180);
        Core.rotate(mRgba, mRgba, Core.ROTATE_180);
        //Core.rotate(mGray, mGray, Core.ROTATE_90_CLOCKWISE);
        //Core.rotate(mRgba, mRgba, Core.ROTATE_90_CLOCKWISE);
        detect();

        return mRgba;

    }

    /**
     * Main detect method. The frames are in mGray and mRgb.
     * Tries to detect the stop sign and semaphore, if is semaphore tries to find it's colour.
     * If a street sign is detected, it will send a message legobot to behave accordingly.
     */
    Legobot.Analysis d;
    private void detect() {
        Imgproc.equalizeHist(mGray, mGray);
        if(mDetectors == null) return;
        for(AbstractDetector detector : mDetectors)
        {
                d = detector.detect(mRgba, mGray);
                if(!d.equals(Legobot.Analysis.NO_DETECTION))
                {
                    legobot.addAnalysis(d);
                }
        }
    }
}
