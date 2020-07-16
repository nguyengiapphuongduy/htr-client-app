package hcmut.thesis.htr2020.states;

import android.graphics.Bitmap;

import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.lifecycle.ViewModel;

import hcmut.thesis.htr2020.constants.PreviewState;

public class CameraViewModel extends ViewModel {
    private PreviewState previewState = PreviewState.PREVIEW;
    private Bitmap bitmap;
    private String filename;
    private ProcessCameraProvider cameraProvider;

    public PreviewState getPreviewState() {
        return previewState;
    }

    public void setPreviewState(PreviewState previewState) {
        this.previewState = previewState;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public ProcessCameraProvider getCameraProvider() {
        return cameraProvider;
    }

    public void setCameraProvider(ProcessCameraProvider cameraProvider) {
        this.cameraProvider = cameraProvider;
    }
}
