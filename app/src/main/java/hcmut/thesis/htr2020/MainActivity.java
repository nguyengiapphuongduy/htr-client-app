package hcmut.thesis.htr2020;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Size;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import hcmut.thesis.htr2020.constants.ErrorCode;
import hcmut.thesis.htr2020.constants.PreviewState;
import hcmut.thesis.htr2020.constants.RequestCode;
import hcmut.thesis.htr2020.service.ApiService;
import hcmut.thesis.htr2020.states.CameraViewModel;
import hcmut.thesis.htr2020.utils.BitmapUtil;

public class MainActivity extends AppCompatActivity {
    private final String[] requiredPermissions = new String[]{Manifest.permission.CAMERA};
    private CameraViewModel viewModel;
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ImageCapture.OnImageCapturedCallback onImageCapturedCallback;
    private FloatingActionButton undoButton;
    private FloatingActionButton captureButton;
    private FloatingActionButton nextButton;
    private FloatingActionButton galleryButton;
    private ImageView imageView;
    private FrameLayout progressBarHolder;
    private AlphaAnimation inAnimation;
    private AlphaAnimation outAnimation;
    private ApiTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        configureStatusAndActionBar();
        previewView = findViewById(R.id.preview_view);
        undoButton = findViewById(R.id.undo_button);
        undoButton.setOnClickListener(view -> onBackPressed());
        captureButton = findViewById(R.id.capture_button);
        captureButton.setOnClickListener(view -> captureImage());
        nextButton = findViewById(R.id.next_button);
        nextButton.setOnClickListener(view -> startRecognition());
        galleryButton = findViewById(R.id.gallery_button);
        galleryButton.setOnClickListener(view -> openGallery());
        imageView = findViewById(R.id.captured_image_view);
        progressBarHolder = findViewById(R.id.progress_bar_holder);
        onImageCapturedCallback = new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                viewModel.setBitmap(BitmapUtil.fromImageProxy(image));
                switchPreviewState(PreviewState.CAPTURED);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                super.onError(exception);
                Toast.makeText(MainActivity.this, "Capture Failed", Toast.LENGTH_SHORT).show();
            }
        };
        inAnimation = new AlphaAnimation(0f, 1f);
        inAnimation.setDuration(200);
        outAnimation = new AlphaAnimation(1f, 0f);
        outAnimation.setDuration(200);
    }

    private void configureStatusAndActionBar() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            supportActionBar.setCustomView(R.layout.action_bar_gradient);
        }
    }

    private void captureImage() {
        imageCapture.takePicture(ContextCompat.getMainExecutor(this), onImageCapturedCallback);
    }

    private void openGallery() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickIntent, RequestCode.PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCode.PICK_IMAGE && data != null && data.getData() != null) {
            try (Cursor cursor = getContentResolver().query(data.getData(),
                    null,
                    null,
                    null,
                    null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    String filename = cursor.getString(columnIndex);
                    viewModel.setFilename(filename.substring(0, filename.lastIndexOf(".")));
                }
            }
            try (InputStream inputStream = getContentResolver().openInputStream(data.getData())) {
                viewModel.setBitmap(BitmapUtil.decodeWithExif(inputStream));
                switchPreviewState(PreviewState.PICKED);
            } catch (IOException e) {
                Toast.makeText(this, "Error loading file", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!permissionGranted()) {
            ActivityCompat.requestPermissions(this, requiredPermissions, RequestCode.PERMISSION);
        }
    }

    private boolean permissionGranted() {
        return Stream.of(requiredPermissions).allMatch(permission ->
                PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, permission));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RequestCode.PERMISSION) {
            if (permissionGranted()) {
                onResume();
            } else {
                Toast.makeText(this, "Permissions not granted by the user",
                        Toast.LENGTH_LONG).show();
                this.finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (permissionGranted()) {
            viewModel = ViewModelProviders.of(MainActivity.this).get(CameraViewModel.class);
            switchPreviewState(viewModel.getPreviewState());
        }
    }

    @Override
    public void onBackPressed() {
        if (viewModel.getBitmap() == null) {
            super.onBackPressed();
        } else {
            viewModel.setBitmap(null);
            switchPreviewState(PreviewState.PREVIEW);
            if (task != null) {
                task.cancel(true);
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture
                = ProcessCameraProvider.getInstance(this);
        cameraProviderListenableFuture.addListener(() -> {
            try {
                viewModel.setCameraProvider(cameraProviderListenableFuture.get());
                viewModel.getCameraProvider().unbindAll();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.createSurfaceProvider());
                Size previewViewSize = new Size(previewView.getWidth(), previewView.getHeight());
                imageCapture = new ImageCapture.Builder()
                        .setTargetResolution(previewViewSize)
                        .setTargetRotation(previewView.getDisplay().getRotation())
                        .build();
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();
                viewModel.getCameraProvider()
                        .bindToLifecycle(this, cameraSelector, preview, imageCapture);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void switchPreviewState(PreviewState previewState) {
        viewModel.setPreviewState(previewState);
        imageView.setImageBitmap(viewModel.getBitmap());
        switch (previewState) {
            case PICKED:
            case CAPTURED:
                viewModel.getCameraProvider().unbindAll();
                previewView.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                undoButton.setVisibility(View.VISIBLE);
                nextButton.setVisibility(View.VISIBLE);
                captureButton.setVisibility(View.GONE);
                break;
            case PREVIEW:
            default:
                startCamera();
                previewView.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);
                undoButton.setVisibility(View.GONE);
                nextButton.setVisibility(View.GONE);
                captureButton.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void startRecognition() {
        task = new ApiTask();
        task.execute(viewModel.getBitmap());
    }

    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("deprecation")
    private class ApiTask extends AsyncTask<Bitmap, Integer, String> {
        private ErrorCode errorCode;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressOverlay();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            hideProgressOverlay();
            if (errorCode == ErrorCode.CONNECTION_ERROR) {
                Toast.makeText(MainActivity.this, "Connection Error", Toast.LENGTH_LONG).show();
            } else if (errorCode == ErrorCode.MAPPING_ERROR) {
                Toast.makeText(MainActivity.this, "Missing fields in response", Toast.LENGTH_LONG).show();
            } else if (errorCode == ErrorCode.RESPONSE_FAILED) {
                Toast.makeText(MainActivity.this, "Predict failed", Toast.LENGTH_LONG).show();
            } else {
                sendToNextActivity(result);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            hideProgressOverlay();
            switchPreviewState(PreviewState.PREVIEW);
        }

        @Override
        protected String doInBackground(Bitmap... params) {
            try {
                String predicted = ApiService.getInstance().htrPredict(params[0]);
                if (predicted != null) {
                    return predicted;
                }
                errorCode = ErrorCode.RESPONSE_FAILED;
            } catch (NoSuchFieldException e) {
                errorCode = ErrorCode.MAPPING_ERROR;
            } catch (IOException e) {
                errorCode = ErrorCode.CONNECTION_ERROR;
            }
            return "";
        }

        private void hideProgressOverlay() {
            undoButton.setClickable(true);
            nextButton.setClickable(true);
            captureButton.setClickable(true);
            galleryButton.setClickable(true);
            progressBarHolder.setAnimation(outAnimation);
            progressBarHolder.setVisibility(View.GONE);
        }

        private void showProgressOverlay() {
            undoButton.setClickable(false);
            nextButton.setClickable(false);
            captureButton.setClickable(false);
            galleryButton.setClickable(false);
            progressBarHolder.setAnimation(inAnimation);
            progressBarHolder.setVisibility(View.VISIBLE);
        }

        private void sendToNextActivity(String result) {
            Intent intent = new Intent(MainActivity.this, ResultActivity.class);
            intent.putExtra(Intent.EXTRA_RETURN_RESULT, result);
            if (viewModel.getFilename() == null || viewModel.getFilename().isEmpty()) {
                String filename = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                        .format(System.currentTimeMillis());
                viewModel.setFilename(filename);
            }
            String extension = viewModel.getFilename().endsWith(".txt") ? "" : ".txt";
            intent.putExtra(Intent.EXTRA_TITLE, viewModel.getFilename() + extension);
            startActivity(intent);
        }
    }
}