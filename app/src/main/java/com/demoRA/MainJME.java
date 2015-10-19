package com.demoRA;

import android.content.pm.ActivityInfo;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;

import java.nio.ByteBuffer;

/**
 * Created by MICHEL on 02/02/2015.
 */
public class MainJME extends SimpleApplication {

    private static final String LOGTAG = "MainJME";

    private Geometry mVideoBGGeom;
    private Material mvideoBGMat;
    private Texture2D mCameraTexture;
    private Image mCameraImage;

    Camera videoBGCam;
    Camera fgCam;

    private boolean mSceneInitialized = false;
    private boolean mVideoImageInitialized = false;

    boolean mNewCameraFrameAvailable = false;
    boolean mIsActive=false;

    private float mForegroundCamFOVY = 90; // for a Samsung Galaxy SII

    private MainActivity activity;

    @Override
    public void simpleInitApp() {
        viewPort.detachScene(rootNode);

        initVideoBackground(settings.getWidth(), settings.getHeight());
        initBackgroundCamera();

        initForegroundScene();
        initForegroundCamera(mForegroundCamFOVY);
    }

    @Override
    public void simpleUpdate(float tpf) {
        if(mIsActive) {
            activity.updateTracking();

            if (mNewCameraFrameAvailable) {
                mCameraTexture.setImage(mCameraImage);
                mvideoBGMat.setTexture("ColorMap", mCameraTexture);
            }
        }
        mVideoBGGeom.updateLogicalState(tpf);
        mVideoBGGeom.updateGeometricState();
    }

    @Override
    public void simpleRender(RenderManager rm) {

    }

    public void setActivity(MainActivity activity) {
        this.activity = activity;
    }

    public MainActivity getActivity() {
        return activity;
    }

    // This function creates the geometry, the viewport and the virtual camera
    // needed for rendering the incoming Android camera frames in the scene
    // graph
    public void initVideoBackground(int screenWidth, int screenHeight) {
        // Create a Quad shape.
        Quad videoBGQuad = new Quad(1, 1, true);
        // Create a Geometry with the Quad shape
        mVideoBGGeom = new Geometry("quad", videoBGQuad);
        float newWidth = 1.f * screenWidth / screenHeight;
        // Center the Geometry in the middle of the screen.
        mVideoBGGeom.setLocalTranslation(-0.5f * newWidth, -0.5f, 0.f);//
        // Scale (stretch) the width of the Geometry to cover the whole screen
        // width.
        mVideoBGGeom.setLocalScale(1.f * newWidth, 1.f, 1);
        // Apply a unshaded material which we will use for texturing.
        mvideoBGMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mVideoBGGeom.setMaterial(mvideoBGMat);

        // Create a new texture which will hold the Android camera preview frame
        // pixels.
        mCameraTexture = new Texture2D();
        mSceneInitialized = true;
    }

    public void initBackgroundCamera() {
        // Create a custom virtual camera with orthographic projection
        videoBGCam = new Camera(settings.getWidth(), settings.getHeight());
        videoBGCam.setViewPort(0.0f, 1.0f, 0.f, 1.0f);
        videoBGCam.setLocation(new Vector3f(0f, 0f, 1.f));
        videoBGCam.setAxes(new Vector3f(-1f, 0f, 0f), new Vector3f(0f, 1f, 0f), new Vector3f(0f, 0f, -1f));
        videoBGCam.setParallelProjection(true);

        // Also create a custom viewport.
        ViewPort videoBGVP = renderManager.createMainView("VideoBGView", videoBGCam);
        // Attach the geometry representing the video background to the
        // viewport.
        videoBGVP.attachScene(mVideoBGGeom);

        //videoBGVP.setClearFlags(true, false, false);
        //videoBGVP.setBackgroundColor(new ColorRGBA(1,0,0,1));

    }

    public void setVideoBGTexture(final ByteBuffer image, int width, int height) {
        if (!mSceneInitialized) {
            return;
        }

        mCameraImage = new Image(Image.Format.RGB565, width, height, image);
        mNewCameraFrameAvailable = true;
    }

    public void initForegroundScene() {

        //use the box for debugging

        /*Box b = new Box(10, 10, 10); // create cube shape at the origin
        Geometry geom = new Geometry("Box", b);  // create cube geometry from the shape
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");  // create a simple material
        mat.setColor("Color", ColorRGBA.Blue);   // set color of material to blue
        geom.setMaterial(mat);                   // set the cube's material
        rootNode.attachChild(geom);              // make the cube appear in the scene

        geom.setLocalTranslation(new Vector3f(0.0f, 0.0f, 0.0f));*/


        Spatial teapot = assetManager.loadModel("Models/logo/unetLogo.mesh.j3o");
        teapot.scale(50f, 50f, 50f);
        Material cube1Mat = new Material(assetManager,"Common/MatDefs/Misc/Unshaded.j3md");
        Texture cube1Tex = assetManager.loadTexture(new TextureKey("Models/logo/unetlogoCompleteMap.tga", false));
        cube1Mat.setTexture("ColorMap", cube1Tex);
        teapot.setMaterial(cube1Mat);

        Quaternion rotateNinjaX=new Quaternion();
        rotateNinjaX.fromAngleAxis(3.14f/2.0f,new Vector3f(1.0f,0.0f,0.0f));
        Quaternion rotateNinjaZ=new Quaternion();
        rotateNinjaZ.fromAngleAxis(3.14f,new Vector3f(0.0f,0.0f,1.0f));
        Quaternion rotateNinjaXZ=rotateNinjaZ.mult(rotateNinjaX);

        teapot.rotate(rotateNinjaXZ);
        teapot.setLocalTranslation(0.0f, 0.0f, 0.0f);

        Quaternion YAW180   = new Quaternion().fromAngleAxis(FastMath.PI  ,   new Vector3f(0,1,0));
        teapot.rotate(YAW180);

        rootNode.attachChild(teapot);

        /*Spatial liberty = assetManager.loadModel("Models/Liberty.mesh.j3o");
        liberty.setLocalTranslation(new Vector3f(0.0f, 0.0f, 0.0f));
        rootNode.attachChild(liberty);*/

        /*DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f).normalizeLocal());
        rootNode.addLight(sun);*/

        // Load a model from test_data (OgreXML + material + texture)
        /*Spatial ninja = assetManager.loadModel("Models/Ninja/Ninja.mesh.xml");
        ninja.scale(0.125f, 0.125f, 0.125f);
        Quaternion rotateNinjaX=new Quaternion();
        rotateNinjaX.fromAngleAxis(3.14f/2.0f,new Vector3f(1.0f,0.0f,0.0f));
        Quaternion rotateNinjaZ=new Quaternion();
        rotateNinjaZ.fromAngleAxis(3.14f,new Vector3f(0.0f,0.0f,1.0f));

        // rotateNinjaX.mult(rotateNinjaZ);
        Quaternion rotateNinjaXZ=rotateNinjaZ.mult(rotateNinjaX);

        ninja.rotate(rotateNinjaXZ);

        //3.14/2.,new Vector3f(1.0.,0.0,1.0)));
        // ninja.rotate(0.0f, -3.0f, 0.0f);
        ninja.setLocalTranslation(0.0f, 0.0f, 0.0f);
        rootNode.attachChild(ninja);

        // You must add a light to make the model visible
        DirectionalLight back = new DirectionalLight();
        back.setDirection(new Vector3f(0.f,-1.f,1.0f));
        rootNode.addLight(back);

        DirectionalLight front = new DirectionalLight();
        front.setDirection(new Vector3f(0.f,1.f,1.0f));
        rootNode.addLight(front);*/
    }

    public void initForegroundCamera(float fovY) {
        fgCam = new Camera(settings.getWidth(), settings.getHeight());

        fgCam.setViewPort(0, 1.0f, 0.f, 1.0f);
        fgCam.setLocation(new Vector3f(0f, 0f, 0f));
        fgCam.setAxes(new Vector3f(-1f, 0f, 0f), new Vector3f(0f, 1f, 0f), new Vector3f(0f, 0f, -1f));
        fgCam.setFrustumPerspective(fovY, settings.getWidth() / settings.getHeight(), 1, 1000);

        ViewPort fgVP = renderManager.createMainView("ForegroundView", fgCam);
        fgVP.attachScene(rootNode);
        //color,depth,stencil
        fgVP.setClearFlags(false, true, false);
        fgVP.setBackgroundColor(new ColorRGBA(0, 0, 0, 1));
//		fgVP.setBackgroundColor(new ColorRGBA(0,0,0,0));
    }

    public void setCameraPerspective(float fovY, float aspectRatio) {
        //Log.d(LOGTAG, "setCameraPerspective: fovY= "+ fovY + " - aspectRatio= " + aspectRatio);
        fgCam.setFrustumPerspective(fovY, aspectRatio, 1.f, 100.f);
    }

    public void setCameraViewport(float viewport_w, float viewport_h, float size_x, float size_y) {
        //Log.d(LOGTAG, "setCameraViewport: viewport_w= "+ viewport_w + " - viewport_h= " + viewport_h);
        float newWidth = 1.f;
        float newHeight = 1.f;

        if (viewport_h != settings.getHeight()) {
            newWidth = viewport_w / viewport_h;
            newHeight = 1.0f;
            videoBGCam.resize((int) viewport_w, (int) viewport_h, true);
            videoBGCam.setParallelProjection(true);
        }
        //exercise: find the similar transformation
        //when viewport_w != settings.getWidth

        //Adjusting viewport: from BackgroundTextureAccess example in Qualcomm Vuforia
        float viewportPosition_x = (((int) (settings.getWidth() - viewport_w)) / (int) 2);//+0
        float viewportPosition_y = (((int) (settings.getHeight() - viewport_h)) / (int) 2);//+0
        float viewportSize_x = viewport_w;//2560
        float viewportSize_y = viewport_h;//1920

        //transform in normalized coordinate
        viewportPosition_x = viewportPosition_x / viewport_w;
        viewportPosition_y = viewportPosition_y / viewport_h;
        viewportSize_x = viewportSize_x / viewport_w;
        viewportSize_y = viewportSize_y / viewport_h;

        //adjust for viewport start (modify video quad)
        mVideoBGGeom.setLocalTranslation(-0.5f * newWidth + viewportPosition_x, -0.5f * newHeight + viewportPosition_y, 0.f);
        //adust for viewport size (modify video quad)
        mVideoBGGeom.setLocalScale(newWidth, newHeight, 1.f);
    }

    public void setCameraPose(float cam_x, float cam_y, float cam_z) {
        fgCam.setLocation(new Vector3f(cam_x, cam_y, cam_z));
    }

    public void setCameraOrientation(float cam_right_x, float cam_right_y, float cam_right_z, float cam_up_x, float cam_up_y, float cam_up_z, float cam_dir_x, float cam_dir_y, float cam_dir_z) {
        //left,up,direction
        fgCam.setAxes(new Vector3f(-cam_right_x, -cam_right_y, -cam_right_z), new Vector3f(-cam_up_x, -cam_up_y, -cam_up_z), new Vector3f(cam_dir_x, cam_dir_y, cam_dir_z));
    }

    public void lookAway() {
        fgCam.setLocation(new Vector3f(0, 100f, 0));
        fgCam.lookAt(new Vector3f(0f, 500f, 0f), Vector3f.UNIT_Y);
    }
}
