package com.example.boris.goglobaltask;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by boris on 3/24/15.
 */
public class CaptureFragment extends Fragment
        implements Camera.PictureCallback, SurfaceHolder.Callback {

    private static final String KEY_IS_CAPTURING = "is_capturing";
    private final String PACKAG_NAME = "com.example.boris.goglobaltask";
    private Camera mCamera;
    private ImageView mCameraImage;
    private SurfaceView mCameraPreview;
    private Button mCaptureImageButton;
    private byte[] mCameraData;
    private boolean mIsCapturing;
    private Bitmap mCameraBitmap;
    private Button mSaveImageButton;
    private Button flashButton;
    private Button selfieButton;
    private int frontCameraId = -1;
    private int baskCameraId = -1;

    private View.OnClickListener mCaptureImageButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            captureImage();
        }
    };

    private View.OnClickListener mRecaptureImageButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setupImageCapture();
        }
    };

    private View.OnClickListener flashButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switchFlash();
        }
    };

    private View.OnClickListener selfieButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switchCamera();
        }
    };

    private View.OnClickListener mSaveImageButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(getActivity(), "Saving, please wait...",
                    Toast.LENGTH_LONG).show();
            File saveFile = openFileForImage();
            if (saveFile != null) {
                imageToBitMap();
                saveImageToFile(saveFile);
            } else {
                Toast.makeText(getActivity(), "Unable to open file for saving image.",
                        Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putBoolean(KEY_IS_CAPTURING, mIsCapturing);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mCamera == null) {
            try {
                mCamera = Camera.open();
                mCamera.setPreviewDisplay(mCameraPreview.getHolder());
                if (mIsCapturing) {
                    mCamera.startPreview();
                    mCamera.setDisplayOrientation(90);
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Unable to open camera.", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        mCameraData = data;
        setupImageDisplay();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(holder);
                if (mIsCapturing) {
                    mCamera.startPreview();
                }
            } catch (IOException e) {
                Toast.makeText(getActivity(), "Unable to start camera preview.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    private void captureImage() {
        mCamera.takePicture(null, null, this);
    }

    private void setupImageCapture() {
        mCameraImage.setVisibility(View.INVISIBLE);
        mCameraPreview.setVisibility(View.VISIBLE);
        mCamera.startPreview();
        mCaptureImageButton.setText(R.string.capture_image);
        mCaptureImageButton.setOnClickListener(mCaptureImageButtonClickListener);
        mSaveImageButton.setEnabled(false);
    }

    private int[] getNewImageSizes(Bitmap bitmap){
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        float screenWidth = size.x;
        float screenHeight = size.y;
        float imageWidth = bitmap.getWidth();
        float imageHeight = bitmap.getHeight();

        float xKoef = screenWidth / imageWidth;
        float yKoef = screenHeight / imageHeight;

        float koef = (xKoef > yKoef ? yKoef : xKoef);
        return new int[]{(int) (imageWidth * koef), (int) (imageHeight * koef)};
    }

    private void setupImageDisplay() {
        Bitmap bitmap = BitmapFactory.decodeByteArray(mCameraData, 0, mCameraData.length);
        int[] imageSizes = getNewImageSizes(bitmap);
        mCameraImage.setImageBitmap(Bitmap.createScaledBitmap(bitmap, imageSizes[0], imageSizes[1], true));
        mCamera.stopPreview();
        mCameraPreview.setVisibility(View.INVISIBLE);
        mCameraImage.setVisibility(View.VISIBLE);
        mCaptureImageButton.setText(R.string.recapture_image);
        mCaptureImageButton.setOnClickListener(mRecaptureImageButtonClickListener);
        mSaveImageButton.setEnabled(true);
    }

    private void switchFlash(){
        String value;
        if(flashButton.getText() == getResources().getString(R.string.flash_on)) {
            value =  Camera.Parameters.FLASH_MODE_ON;
            flashButton.setText(getResources().getString(R.string.flash_off));
        }else {
            value =  Camera.Parameters.FLASH_MODE_OFF;
            flashButton.setText(getResources().getString(R.string.flash_on));
        }
        Camera.Parameters params = mCamera.getParameters();
        params.setFlashMode(value);
        mCamera.setParameters(params);
    }

    private void switchCamera(){
        if(frontCameraId == -1 && baskCameraId == -1) {
            findAllCameras();
        }
        int camId;
        if(selfieButton.getText() == getResources().getString(R.string.selfie_camera)) {
            camId = frontCameraId;
            selfieButton.setText(getResources().getString(R.string.normal_camera));
        }else {
            camId = baskCameraId;
            selfieButton.setText(getResources().getString(R.string.selfie_camera));
        }
        mCamera.release();
        try {
            mCamera = Camera.open(camId);
            mCamera.setPreviewDisplay(mCameraPreview.getHolder());
            if (mIsCapturing) {
                mCamera.startPreview();
                mCamera.setDisplayOrientation(90);
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Unable to open camera.", Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void findAllCameras() {
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                frontCameraId = camIdx;
            } else if(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                baskCameraId = camIdx;
            }
        }
    }

    private File openFileForImage() {
        File imageDirectory = null;
        String storageState = Environment.getExternalStorageState();
        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
            imageDirectory = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    PACKAG_NAME);
            if (!imageDirectory.exists() && !imageDirectory.mkdirs()) {
                imageDirectory = null;
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_mm_dd_hh_mm_ss",
                        Locale.getDefault());

                return new File(imageDirectory.getPath() +
                        File.separator + "image_" +
                        dateFormat.format(new Date()) + ".jpeg");
            }
        }
        return null;
    }

    private void imageToBitMap(){
        // Recycle the previous bitmap.
        if (mCameraBitmap != null) {
            mCameraBitmap.recycle();
            mCameraBitmap = null;
        }
        byte[] cameraData = mCameraData;
        if (cameraData != null) {
            mCameraBitmap = BitmapFactory.decodeByteArray(cameraData, 0, cameraData.length);
            mSaveImageButton.setEnabled(true);
        }
    }

    private void saveImageToFile(File file) {
        if (mCameraBitmap != null) {
            FileOutputStream outStream = null;
            try {
                outStream = new FileOutputStream(file);
                if (!mCameraBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)) {
                    Toast.makeText(getActivity(), "Unable to save image to file.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), "Saved image to: " + file.getPath(),
                            Toast.LENGTH_LONG).show();
                }
                outStream.close();
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Unable to save image to file.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_capture, container, false);

        mCameraImage = (ImageView) view.findViewById(R.id.camera_image_view);
        mCameraImage.setVisibility(View.INVISIBLE);

        flashButton = (Button) view.findViewById(R.id.flash_button);
        flashButton.setOnClickListener(flashButtonClickListener);

        selfieButton = (Button) view.findViewById(R.id.front_camera_button);
        if (Camera.getNumberOfCameras() > 1) {
            selfieButton.setEnabled(true);
            selfieButton.setOnClickListener(selfieButtonClickListener);
        }

        mCameraPreview = (SurfaceView) view.findViewById(R.id.preview_view);
        final SurfaceHolder surfaceHolder = mCameraPreview.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mCaptureImageButton = (Button) view.findViewById(R.id.capture_image_button);
        mCaptureImageButton.setOnClickListener(mCaptureImageButtonClickListener);

        mIsCapturing = true;

        mSaveImageButton = (Button) view.findViewById(R.id.save_image_button);
        mSaveImageButton.setOnClickListener(mSaveImageButtonClickListener);
        mSaveImageButton.setEnabled(false);

        return view;
    }


}
