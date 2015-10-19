package com.demoRA;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Switch;

import com.demoRA.utils.MathLib;
import com.jme3.app.AndroidHarness;
import com.jme3.system.android.AndroidConfigChooser;
import com.qualcomm.vuforia.CameraCalibration;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.DataSet;
import com.qualcomm.vuforia.Frame;
import com.qualcomm.vuforia.Image;
import com.qualcomm.vuforia.ObjectTracker;
import com.qualcomm.vuforia.Matrix34F;
import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.PIXEL_FORMAT;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.STORAGE_TYPE;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.Tracker;
import com.qualcomm.vuforia.TrackerManager;
import com.qualcomm.vuforia.Vec2F;
import com.qualcomm.vuforia.VideoBackgroundConfig;

import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Created by MICHEL on 02/02/2015.
 */
public class MainActivity extends AndroidHarness implements ApplicationControl{

    private static final String LOGTAG = "MainActivity";

    ApplicationSession vuforiaAppSession;

    private DataSet mCurrentDataset;
    private int mCurrentDatasetSelectionIndex = 0;
    private ArrayList<String> mDatasetStrings = new ArrayList<String>();

    private boolean mExtendedTracking = true;
    private boolean mContAutofocus = false;

    public MainActivity() {
        appClass = "com.demoRA.MainJME"; // Set the application class to run
        //eglConfigType = AndroidConfigChooser.ConfigType.BEST; // Try ConfigType.FASTEST; or ConfigType.LEGACY if you have problems

        eglBitsPerPixel = 24;
        eglAlphaBits = 0;
        eglDepthBits = 16;
        eglSamples = 0;
        eglStencilBits = 0;

        exitDialogTitle = "Exit?";
        exitDialogMessage = "Press Yes";

        splashPicID = R.drawable.splashscreen;

        mouseEventsInvertX = true; // Invert the MouseEvents X
        mouseEventsInvertY = true; // Invert the MouseEvents Y

        logger.setLevel(Level.SEVERE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ((MainJME) this.getJmeApplication()).setActivity(this);

        vuforiaAppSession = new ApplicationSession(this);
        mDatasetStrings.add("VUforiaJME.xml");
        vuforiaAppSession.initAR(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try{
            vuforiaAppSession.stopAR();
        } catch (ApplicationException e){
            Log.e(LOGTAG, e.getString());
        }
        System.gc();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume(){
        super.onResume();
        // This is needed for some Droid devices to force portrait
        try{
            vuforiaAppSession.resumeAR();
        } catch (ApplicationException e){
            Log.e(LOGTAG, e.getString());
        }
    }

    @Override
    protected void onPause(){
        super.onPause();

        try{
            vuforiaAppSession.pauseAR();
        } catch (ApplicationException e){
            Log.e(LOGTAG, e.getString());
        }
    }

    @Override
    public boolean doInitTrackers() {
        // Indicate if the trackers were initialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        Tracker tracker;

        // Trying to initialize the image tracker
        tracker = tManager.initTracker(ObjectTracker.getClassType());
        if (tracker == null){
            Log.e(LOGTAG,"Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else{
            Log.i(LOGTAG, "Tracker successfully initialized");
        }
        return result;
    }

    @Override
    public boolean doLoadTrackersData() {
        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker imageTracker = (ObjectTracker) tManager.getTracker(ObjectTracker.getClassType());
        if (imageTracker == null)
            return false;

        if (mCurrentDataset == null)
            mCurrentDataset = imageTracker.createDataSet();

        if (mCurrentDataset == null)
            return false;

        if (!mCurrentDataset.load(mDatasetStrings.get(mCurrentDatasetSelectionIndex),STORAGE_TYPE.STORAGE_APPRESOURCE))
            return false;

        if (!imageTracker.activateDataSet(mCurrentDataset))
            return false;

        int numTrackables = mCurrentDataset.getNumTrackables();
        for (int count = 0; count < numTrackables; count++){
            Trackable trackable = mCurrentDataset.getTrackable(count);
            if(isExtendedTrackingActive()){
                trackable.startExtendedTracking();
            }

            String name = "Current Dataset : " + trackable.getName();
            trackable.setUserData(name);
            Log.d(LOGTAG, "UserData:Set the following user data "+ (String) trackable.getUserData());
        }
        return true;
    }

    @Override
    public boolean doStartTrackers() {
        // Indicate if the trackers were started correctly
        boolean result = true;

        Tracker imageTracker = TrackerManager.getInstance().getTracker(ObjectTracker.getClassType());
        if (imageTracker != null)
            imageTracker.start();

        return result;
    }

    @Override
    public boolean doStopTrackers() {
        // Indicate if the trackers were stopped correctly
        boolean result = true;

        Tracker imageTracker = TrackerManager.getInstance().getTracker(ObjectTracker.getClassType());
        if (imageTracker != null)
            imageTracker.stop();

        return result;
    }

    @Override
    public boolean doUnloadTrackersData() {
        // Indicate if the trackers were unloaded correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker imageTracker = (ObjectTracker) tManager.getTracker(ObjectTracker.getClassType());
        if (imageTracker == null)
            return false;

        if (mCurrentDataset != null && mCurrentDataset.isActive()){
            if (imageTracker.getActiveDataSet().equals(mCurrentDataset)
                    && !imageTracker.deactivateDataSet(mCurrentDataset))
            {
                result = false;
            } else if (!imageTracker.destroyDataSet(mCurrentDataset)){
                result = false;
            }
            mCurrentDataset = null;
        }
        return result;
    }

    @Override
    public boolean doDeinitTrackers() {
        // Indicate if the trackers were deinitialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(ObjectTracker.getClassType());

        return result;
    }

    @Override
    public void onInitARDone(ApplicationException exception) {
        if (exception == null){

            ((MainJME)getJmeApplication()).mIsActive=true;

            try{
                vuforiaAppSession.startAR(CameraDevice.CAMERA.CAMERA_DEFAULT);
            } catch (ApplicationException e){
                Log.e(LOGTAG, e.getString());
            }

            boolean result = CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);

            if (result)
                mContAutofocus = true;
            else
                Log.e(LOGTAG, "Unable to enable continuous autofocus");
        } else{
            Log.e(LOGTAG, exception.getString());
            finish();
        }
    }

    @Override
    public void onQCARUpdate(State state) {
        Image imageRGB565 = null;
        Frame frame = state.getFrame();

        for (int i = 0; i < frame.getNumImages(); ++i) {
            Image image = frame.getImage(i);
            if (image.getFormat() == PIXEL_FORMAT.RGB565) {
                imageRGB565 = image;
                break;
            }
        }
        if (this.getJmeApplication() != null) {
            ((MainJME) this.getJmeApplication()).setVideoBGTexture(imageRGB565.getPixels(), imageRGB565.getWidth(), imageRGB565.getHeight());
        }
    }

    public void updateTracking() {
        if (this.getJmeApplication() != null) {
            State state = Renderer.getInstance().begin();

            if (state.getNumTrackableResults() > 0) {
                //Log.d(LOGTAG, "Results Found..");

                for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++) {
                    TrackableResult result = state.getTrackableResult(tIdx);
                    Trackable trackable = result.getTrackable();
                    //printUserData(trackable);

                    Matrix34F res = result.getPose();
                    float[] dt = res.getData();
                    Matrix44F modelViewMatrix = Tool.convertPose2GLMatrix(result.getPose());

                    Matrix44F inverseMV = MathLib.Matrix44FInverse(modelViewMatrix);
                    Matrix44F invTranspMV = MathLib.Matrix44FTranspose(inverseMV);

                    //get position
                    float cam_x = invTranspMV.getData()[12];
                    float cam_y = invTranspMV.getData()[13];
                    float cam_z = invTranspMV.getData()[14];

                    //get rotation
                    float cam_right_x = invTranspMV.getData()[0];
                    float cam_right_y = invTranspMV.getData()[1];
                    float cam_right_z = invTranspMV.getData()[2];
                    float cam_up_x = invTranspMV.getData()[4];
                    float cam_up_y = invTranspMV.getData()[5];
                    float cam_up_z = invTranspMV.getData()[6];
                    float cam_dir_x = invTranspMV.getData()[8];
                    float cam_dir_y = invTranspMV.getData()[9];
                    float cam_dir_z = invTranspMV.getData()[10];

                    //get perspective transformation
                    float nearPlane = 1.0f;
                    float farPlane = 1000.0f;

                    CameraCalibration cameraCalibration = CameraDevice.getInstance().getCameraCalibration();
                    VideoBackgroundConfig config = Renderer.getInstance().getVideoBackgroundConfig();


                    float viewportWidth = config.getSize().getData()[0];
                    float viewportHeight = config.getSize().getData()[1];

                    Vec2F size = cameraCalibration.getSize();
                    Vec2F focalLength = cameraCalibration.getFocalLength();

                    float fovRadians = (float) (2 * Math.atan(0.5f * (size.getData()[1] / focalLength.getData()[1])));
                    float fovDegrees = (float) (fovRadians * 180.0f / Math.PI);
                    float aspectRatio = (size.getData()[0] / size.getData()[1]);

                    //adjust for screen vs camera size distorsion
                    float viewportDistort = 1.0f;

                    /*if (viewportWidth != mScreenWidth) {
                        viewportDistort = viewportWidth / (float) mScreenWidth;
                        fovDegrees = fovDegrees * viewportDistort;
                        aspectRatio = aspectRatio / viewportDistort;
                    }

                    if (viewportHeight != mScreenHeight) {
                        viewportDistort = viewportHeight / (float) mScreenHeight;
                        fovDegrees = fovDegrees / viewportDistort;
                        aspectRatio = aspectRatio * viewportDistort;
                    }*/

                    //((com.demoAR.DemoARJME) this.getJmeApplication()).setCameraPerspective(fovDegrees,aspectRatio);
                    //((com.demoAR.DemoARJME) this.getJmeApplication()).setCameraViewport(viewportWidth,viewportHeight,cameraCalibration.getSize().getData()[0],cameraCalibration.getSize().getData()[1]);
                    ((MainJME) this.getJmeApplication()).setCameraPose(cam_x, cam_y, cam_z);
                    ((MainJME) this.getJmeApplication()).setCameraOrientation(cam_right_x, cam_right_y, cam_right_z, cam_up_x, cam_up_y, cam_up_z, cam_dir_x, cam_dir_y, cam_dir_z);

                }

            } else {
                //Log.d(LOGTAG, "No results found!");
                ((MainJME) this.getJmeApplication()).lookAway();
            }


            Renderer.getInstance().end();
        }

    }

    boolean isExtendedTrackingActive()
    {
        return mExtendedTracking;
    }
}
