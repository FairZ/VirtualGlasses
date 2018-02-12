package com.bournemouthuniversity.afaiers.virtualglassesdraft;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TryOnActivity extends AppCompatActivity {

    private static final String TAG = "TryOn";

    //layout setup
    private Button m_captureButton;
    private TextureView m_previewTexture;

    //TODO: ADD THE ABILITY TO SWITCH CAMERAS (WILL NEED ANOTHER ORIENTATIONS ARRAY)

    //Camera Setup
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 270);
        ORIENTATIONS.append(Surface.ROTATION_90, 180);
        ORIENTATIONS.append(Surface.ROTATION_180, 90);
        ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }
    private String m_camId;
    protected CameraDevice m_camDevice;
    protected CameraCaptureSession m_camCaptureSession;
    protected CaptureRequest m_captureRequest;
    protected CaptureRequest.Builder m_captureRequestBuilder;
    private boolean m_flashSupported;
    private static final int REQUEST_CAMERA_PERMISSION = 200;

    //file setup
    private Size m_imgDimensions;
    private File m_file;

    //multithreading setup
    private Handler m_backgroundHandler;
    private HandlerThread m_backgroundThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_try_on);

        //get and setup preview texture
        m_previewTexture = (TextureView)findViewById(R.id.camera_preview);
        assert m_previewTexture != null;
        m_previewTexture.setSurfaceTextureListener(textureListener);

        //get and setup capture button
        m_captureButton = (Button)findViewById(R.id.capture_button);
        assert m_captureButton != null;
        m_captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CaptureImage();
            }
        });
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            OpenCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
            //TODO: transform image capture size
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            CloseCamera();
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {}
    };

    private final CameraDevice.StateCallback camStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            m_camDevice = cameraDevice;
            CreateCamPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            m_camDevice.close();
            m_camDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            m_camDevice.close();
            m_camDevice = null;
        }
    };

    final CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            CreateCamPreview();
        }
    };

    protected void StartBackgroundThread(){
        m_backgroundThread = new HandlerThread("Camera");
        m_backgroundThread.start();
        m_backgroundHandler = new Handler(m_backgroundThread.getLooper());
    }

    protected void StopBackgroundThread(){
        m_backgroundThread.quitSafely();
        try{
            m_backgroundThread.join();
            m_backgroundThread = null;
            m_backgroundHandler = null;
        }
        catch (InterruptedException e){
            Log.d(TAG, "Problem when quitting background thread: " + e.getMessage());
        }
    }

    //TODO: Possibly change catches to uses logcat rather than just printing stack trace
    private void CreateCamPreview() {
        try{
            //get surface texture of the preview and set up buffer sizes for receiving preview
            SurfaceTexture surfaceTexture = m_previewTexture.getSurfaceTexture();
            assert surfaceTexture != null;
            surfaceTexture.setDefaultBufferSize(m_imgDimensions.getWidth(), m_imgDimensions.getHeight());
            Surface surface = new Surface(surfaceTexture);
            //send a template capture request to the camera device and set it up
            m_captureRequestBuilder = m_camDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            m_captureRequestBuilder.addTarget(surface);
            m_camDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if(m_camDevice == null)
                    {
                        //Camera is closed;
                        return;
                    }
                    m_camCaptureSession = cameraCaptureSession;
                    UpdatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //TODO: ADD ERROR HANDLING
                }
            }, null);

        }catch(CameraAccessException e)
        {
            e.printStackTrace();
        }
    }

    protected void UpdatePreview(){
        if(m_camDevice == null){
            Log.e(TAG, "error in UpdatePreview");
            return;
        }
        m_captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

        try{
            //set the capture session to be loop over frames
            m_camCaptureSession.setRepeatingRequest(m_captureRequestBuilder.build(), null, m_backgroundHandler);
        }
        catch (CameraAccessException e)
        {
            e.printStackTrace();
        }
    }

    //TODO: Possibly change catches to uses logcat rather than just printing stack trace
    private void CaptureImage() {
        if (m_camDevice == null)
        {
            Log.d(TAG, "CameraDevice null while trying to take picture");
            return;
        }

        CameraManager camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try{

            //get characteristics of camera and check possible output sizes
            CameraCharacteristics camCharacteristics = camManager.getCameraCharacteristics(m_camId);
            Size[] jpegSizes = null;
            if(camCharacteristics != null)
            {
                jpegSizes = camCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }
            //assign default values and then reassign if values have been found for width and height;
            int width = 480;
            int height = 640;
            if(jpegSizes != null && jpegSizes.length > 0)
            {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            final ImageReader imgReader = ImageReader.newInstance(width,height,ImageFormat.JPEG,1);
            //setup a list of output surfaces for the captured image to use
            List<Surface> outputSurfaces = new ArrayList<Surface>();
            outputSurfaces.add(imgReader.getSurface());
            outputSurfaces.add(new Surface(m_previewTexture.getSurfaceTexture()));

            //send a single fram capture request
            final CaptureRequest.Builder captureBuilder = m_camDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            //set the target to the Image handler
            captureBuilder.addTarget(imgReader.getSurface());
            //ensure the output has the correct orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION,ORIENTATIONS.get(rotation));
            //create a new file in the correct place
            //TODO: CHANGE PICNAME BASED ON GLASSES TYPE AND NUMBER OF PICTURE
            final File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + getResources().getString(R.string.folder_name) + "/PicName1.jpg");
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {

                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    Image img = null;

                    try
                    {
                        //capture the latest image of the reader and store its bytes
                        img = imgReader.acquireLatestImage();
                        ByteBuffer buffer = img.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        Save(bytes);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    finally
                    {
                        if (img != null) {
                            img.close();
                        }
                    }
                }

                private void Save(byte[] bytes) throws IOException{
                    OutputStream outputStream = null;
                    try{
                        //open a stream to the created file and send the correct data
                        outputStream = new FileOutputStream(outputFile);
                        outputStream.write(bytes);
                    } finally {
                        if (outputStream != null){
                            outputStream.close();
                        }
                    }
                }
            };
            //set the listener to the one just created and push it to the background thread
            imgReader.setOnImageAvailableListener(readerListener,m_backgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    //once the capture has been completed restart the preview
                    CreateCamPreview();
                }
            };
            //create a new capture session from the device
            m_camDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try{
                        //send the session to the background thread
                        cameraCaptureSession.capture(captureBuilder.build(), captureListener, m_backgroundHandler);
                    }
                    catch(CameraAccessException e)
                    {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //TODO: ERROR HANDLING
                }
            }, m_backgroundHandler);
        }catch (CameraAccessException e)
        {
            e.printStackTrace();
        }

    }

    private void OpenCamera() {
        //get the camera manager of the system
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try{
            //get the first front facing camera of the system
            String[] camIds = manager.getCameraIdList();
            int facing;
            for(int i = 0; i < camIds.length; i++)
            {
                facing = manager.getCameraCharacteristics(camIds[i]).get(CameraCharacteristics.LENS_FACING);
                if (facing == CameraCharacteristics.LENS_FACING_FRONT)
                {
                    m_camId = camIds[i];
                    break;
                }
            }

            //get the characteristics of the camera and set the dimensions up correctly
            CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(m_camId);
            StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            m_imgDimensions = map.getOutputSizes(SurfaceTexture.class)[0];
            //correctly set the size of the display to fit the screen
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            float layoutScale = (float)dm.widthPixels / (float)m_imgDimensions.getHeight();
            m_previewTexture.setLayoutParams(new RelativeLayout.LayoutParams(dm.widthPixels, (int)(m_imgDimensions.getWidth()*layoutScale)));
            //check if the app has camera and writing permissions and ask for them otherwise
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(TryOnActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            //open the camera
            manager.openCamera(m_camId, camStateCallback, null);
        }
        catch (CameraAccessException e)
        {
            e.printStackTrace();
        }
    }

    private void CloseCamera(){
        //if the camera device is not already closed close it and clear its variable
        if (m_camDevice != null) {
            m_camDevice.close();
            m_camDevice = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // inform the user that the permission is required and then close the program
                Toast.makeText(TryOnActivity.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //when the program re-enters startup the background thread again and open the camera
        StartBackgroundThread();
        if(m_previewTexture.isAvailable()){
            OpenCamera();
        }
        else{
            m_previewTexture.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    protected void onPause() {
        //when the program exits close the camera and stop the background thread
        super.onPause();
        StopBackgroundThread();
        CloseCamera();

    }
}