package com.demoRA;


import android.util.Log;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ColorSpace;
import com.qualcomm.vuforia.CameraCalibration;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.Vec2F;
import com.qualcomm.vuforia.VideoBackgroundConfig;

import java.nio.ByteBuffer;

/**
 * Created by MICHEL on 02/02/2015.
 */
public class MainJME extends SimpleApplication {

    private static final String LOGTAG = "demoRA_LOG";

    private Geometry mVideoBGGeom;
    private Material mVideoBGMat;
    private Texture2D mCameraTexture;
    private Image mCameraImage;

    private Node objNode;

    Camera videoBGCam;
    Camera fgCam;

    private boolean mSceneInitialized = false;
    //private boolean mVideoImageInitialized = false;

    boolean mNewCameraFrameAvailable = false;
    boolean mIsActive=false;

    //private float mForegroundCamFOVY = 90;

    private MainActivity activity;

    public boolean started=false;
    public boolean targetOver=false;

    @Override
    public void simpleInitApp() {
        viewPort.detachScene(rootNode);

        objNode=new Node("Object Node");
        rootNode.attachChild(objNode);

        initVideoBackground(settings.getWidth(), settings.getHeight());
        initBackgroundCamera();
        initForegroundScene();
    }

    @Override
    public void simpleUpdate(float tpf) {
        if(mIsActive) {
            activity.updateTracking();

            if (mNewCameraFrameAvailable) {
                mCameraTexture.setImage(mCameraImage);
                mVideoBGMat.setTexture("ColorMap", mCameraTexture);
            }
        }
        mVideoBGGeom.updateLogicalState(tpf);
        mVideoBGGeom.updateGeometricState();
    }

    @Override
    public void simpleRender(RenderManager rm) {

    }

    public void startUp(){
        if(!started){
            started=true;
            initForegroundCamera();
        }
    }

    public void setActivity(MainActivity activity) {
        this.activity = activity;
    }

    public MainActivity getActivity() {
        return activity;
    }

    /**
     * Funcion para inciartodo lo relacionado con mostrar los frames que
     * se reciben de la camara en el SceneGraph.
     *
     * @param screenWidth Ancho del Screen
     * @param screenHeight Largo del Screen
     */

    public void initVideoBackground(int screenWidth, int screenHeight) {
        Quad videoBGQuad = new Quad(1, 1, true);
        mVideoBGGeom = new Geometry("quad", videoBGQuad);
        float newWidth = 1.f * screenWidth / screenHeight;
        mVideoBGGeom.setLocalTranslation(-0.5f * newWidth, -0.5f, 0.f);
        mVideoBGGeom.setLocalScale(1.f * newWidth, 1.f, 1);
        mVideoBGMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mVideoBGGeom.setMaterial(mVideoBGMat);
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
        // Attach the geometry representing the video background to the viewport.
        videoBGVP.attachScene(mVideoBGGeom);
    }

    public void setVideoBGTexture(final ByteBuffer image, int width, int height) {
        if (!mSceneInitialized) {
            return;
        }

        mCameraImage = new Image(Image.Format.RGB565, width, height, image, ColorSpace.sRGB);
        mNewCameraFrameAvailable = true;
    }

    /**
     *  Iniciar la escena que se sobre pone a la camara
     */
    public void initForegroundScene() {
        /*Spatial logo = assetManager.loadModel("Models/logo/unetLogo.mesh.j3o");
        logo.scale(100f, 100f, 100f);
        Material cube1Mat = new Material(assetManager,"Common/MatDefs/Misc/Unshaded.j3md");
        Texture cube1Tex = assetManager.loadTexture(new TextureKey("Models/logo/unetlogoCompleteMap.tga", false));
        cube1Mat.setTexture("ColorMap", cube1Tex);
        logo.setMaterial(cube1Mat);

        Quaternion rotateNinjaX=new Quaternion();
        rotateNinjaX.fromAngleAxis(3.14f/2.0f,new Vector3f(1.0f,0.0f,0.0f));
        Quaternion rotateNinjaZ=new Quaternion();
        rotateNinjaZ.fromAngleAxis(3.14f,new Vector3f(0.0f,0.0f,1.0f));
        Quaternion rotateNinjaXZ=rotateNinjaZ.mult(rotateNinjaX);

        logo.rotate(rotateNinjaXZ);
        logo.setLocalTranslation(0.0f, 0.0f, 0.0f);

        Quaternion YAW180   = new Quaternion().fromAngleAxis(FastMath.PI  ,   new Vector3f(0,1,0));
        logo.rotate(YAW180);

        objNode.attachChild(logo);*/

        objNode.attachChild(createAxisMarker(300));
    }

    /**
     *
     *
     */
    public void initForegroundCamera() {
        fgCam = new Camera(settings.getWidth(), settings.getHeight());
        fgCam.setLocation(new Vector3f(0f, 0f, 0f));

        CameraCalibration cameraCalibration = CameraDevice.getInstance().getCameraCalibration();
        VideoBackgroundConfig config = Renderer.getInstance().getVideoBackgroundConfig();

        float viewportWidth = config.getSize().getData()[0];
        float viewportHeight = config.getSize().getData()[1];

        float cameraWidth = cameraCalibration.getSize().getData()[0];
        float cameraHeight = cameraCalibration.getSize().getData()[1];

        float screenWidth = settings.getWidth();
        float screenHeight = settings.getHeight();

        Vec2F size = new Vec2F(cameraWidth, cameraHeight);
        Vec2F focalLength = cameraCalibration.getFocalLength();

        float fovRadians = 2 * (float) Math.atan(0.5f * (size.getData()[1] / focalLength.getData()[1]));
        float fovDegrees = fovRadians * 180.0f / (float) Math.PI;
        float aspectRatio = (size.getData()[0] / size.getData()[1]);

        // Adjust for screen / camera size distortion
        float viewportDistort = 1.0f;

        if (viewportWidth != screenWidth){
            viewportDistort = viewportWidth / screenWidth;
            fovDegrees = fovDegrees * viewportDistort;
            aspectRatio = aspectRatio / viewportDistort;
        }

        if (viewportHeight != screenHeight){
            viewportDistort = viewportHeight / screenHeight;
            fovDegrees = fovDegrees / viewportDistort;
            aspectRatio = aspectRatio * viewportDistort;
        }

        setCameraPerspective(fovDegrees, aspectRatio);
        setCameraViewport(viewportWidth, viewportHeight, cameraWidth, cameraHeight);

        ViewPort fgVP = renderManager.createMainView("ForegroundView", fgCam);
        fgVP.attachScene(rootNode);
        fgVP.setClearFlags(false, true, false);
        fgVP.setBackgroundColor(new ColorRGBA(0, 0, 0, 1));
    }

    /**
     *
     * @param fovY
     * @param aspectRatio
     */
    public void setCameraPerspective(float fovY, float aspectRatio) {
        Log.d(LOGTAG, "setCameraPerspective: fovY= "+ fovY + " - aspectRatio= " + aspectRatio);
        fgCam.setFrustumPerspective(fovY, aspectRatio, 1.f, 5000.f);
        fgCam.update();

    }

    /**
     *
     * @param viewport_w
     * @param viewport_h
     * @param size_x
     * @param size_y
     */
    public void setCameraViewport(float viewport_w, float viewport_h, float size_x, float size_y) {
        Log.d(LOGTAG, "setCameraViewport: viewport_w= "+ viewport_w + " - viewport_h= " + viewport_h);
        float newWidth = 1.f;
        float newHeight = 1.f;

        if (viewport_h != settings.getHeight()) {
            newWidth = viewport_w / viewport_h;
            newHeight = 1.0f;
            videoBGCam.resize((int) viewport_w, (int) viewport_h, true);
            videoBGCam.setParallelProjection(true);
        }

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

    /**
     * Funcion para poner la posicion de la camara en relacion al objeto rastreado
     * @param cam_x
     * @param cam_y
     * @param cam_z
     */
    public void setCameraPose(float cam_x, float cam_y, float cam_z) {
        fgCam.setLocation(new Vector3f(cam_x, cam_y, cam_z));
        fgCam.update();
    }

    /**
     * Funcion para poner la direccion en la que esta viendo la camara en relacion al objeto rastreado
     * @param cam_right_x
     * @param cam_right_y
     * @param cam_right_z
     * @param cam_up_x
     * @param cam_up_y
     * @param cam_up_z
     * @param cam_dir_x
     * @param cam_dir_y
     * @param cam_dir_z
     */
    public void setCameraOrientation(float cam_right_x, float cam_right_y, float cam_right_z, float cam_up_x, float cam_up_y, float cam_up_z, float cam_dir_x, float cam_dir_y, float cam_dir_z) {
        //left,up,direction
        fgCam.setAxes(new Vector3f(-cam_right_x, -cam_right_y, -cam_right_z), new Vector3f(-cam_up_x, -cam_up_y, -cam_up_z), new Vector3f(cam_dir_x, cam_dir_y, cam_dir_z));
        fgCam.update();
    }

    /**
     *  Ocultar los objetos cuando no se esta rastreando nada.
     */
    public void hideObjects() {
        objNode.setCullHint(Spatial.CullHint.Always);
    }

    /**
     *  Mostrar los objetos cuando se rastrea algo.
     */
    public void showObjects(){
        objNode.setCullHint(Spatial.CullHint.Dynamic);
    }

    /**
     *  Funcion Debug para crear un Axis X,Y,Y
     * @param arrowSize
     * @return
     */
    private Node createAxisMarker(float arrowSize) {
        Material redMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        redMat.getAdditionalRenderState().setWireframe(true);
        redMat.setColor("Color", ColorRGBA.Red);

        Material greenMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        greenMat.getAdditionalRenderState().setWireframe(true);
        greenMat.setColor("Color", ColorRGBA.Green);

        Material blueMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        blueMat.getAdditionalRenderState().setWireframe(true);
        blueMat.setColor("Color", ColorRGBA.Blue);

        Node axis = new Node();

        // create arrows
        Geometry arrowX = new Geometry("arrowX", new Arrow(new Vector3f(arrowSize, 0, 0)));
        arrowX.setMaterial(redMat);
        Geometry arrowY = new Geometry("arrowY", new Arrow(new Vector3f(0, arrowSize, 0)));
        arrowY.setMaterial(greenMat);
        Geometry arrowZ = new Geometry("arrowZ", new Arrow(new Vector3f(0, 0, arrowSize)));
        arrowZ.setMaterial(blueMat);
        axis.attachChild(arrowX);
        axis.attachChild(arrowY);
        axis.attachChild(arrowZ);
        return axis;
    }

    public void createTargetOver(float x,float y){
        if(!targetOver) {
            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", ColorRGBA.White);
            Node node = new Node();
            Quad quad = new Quad(x, y, true);
            Geometry geom = new Geometry("targetOver", quad);
            geom.setLocalTranslation(-x/2, -y/2, 0);
            geom.setMaterial(mat);
            node.attachChild(geom);

            objNode.attachChild(node);
            targetOver=true;
        }
    }


}
