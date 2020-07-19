package hcmut.thesis.htr2020;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Stream;

import hcmut.thesis.htr2020.constants.RequestCode;

public class ResultActivity extends AppCompatActivity {
    private final String[] requiredPermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private EditText resultEditText;
    private EditText outFilenameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        resultEditText = findViewById(R.id.result_text);
        resultEditText.setText(getIntent().getStringExtra(Intent.EXTRA_RETURN_RESULT));
        outFilenameEditText = findViewById(R.id.out_filename_text);
        outFilenameEditText.setText(getIntent().getStringExtra(Intent.EXTRA_TITLE));
        findViewById(R.id.save_button).setOnClickListener(this::onSaveClicked);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle(R.string.result);
        }
    }

    private void onSaveClicked(View view) {
        if (outFilenameEditText.getText().length() == 0) {
            outFilenameEditText.setError("Enter a file name");
            return;
        }
        if (permissionGranted()) {
            saveTextFile();
        } else {
            ActivityCompat.requestPermissions(this,
                    requiredPermissions, RequestCode.PERMISSION_WRITE_EXTERNAL);
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
        if (requestCode == RequestCode.PERMISSION_WRITE_EXTERNAL) {
            if (permissionGranted()) {
                saveTextFile();
            } else {
                Toast.makeText(this,
                        "Permissions not granted by the user", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveTextFile() {
        File root = Environment.getExternalStorageDirectory();
        File directory = new File(root, "HTR");
        if (directory.mkdirs()) {
            Toast.makeText(this,
                    "Created directory " + directory.getPath(), Toast.LENGTH_SHORT).show();
        }
        String filename = outFilenameEditText.getText().toString();
        String extension = filename.endsWith(".txt") ? "" : ".txt";
        File file = new File(directory, filename + extension);
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            PrintWriter printWriter = new PrintWriter(fileOutputStream);
            printWriter.println(resultEditText.getText());
            printWriter.flush();
            printWriter.close();
            Toast.makeText(this,
                    "Saved TXT file at " + file.getPath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Unexpected Error", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}