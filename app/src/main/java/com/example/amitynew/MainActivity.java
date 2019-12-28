package com.example.amitynew;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.amitynew.util.FileUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    TextView tvPath,tvType;
    private Uri filePath;
    Button btnSelect, btnUpload,btnShare;
    final int FILE_SELECT_CODE= 1;
    FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            mainCode();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mainCode();
        }
    }


    private void mainCode()
    {
        storageReference = FirebaseStorage.getInstance("gs://abhishekamity-83b1c.appspot.com").getReference();
        tvPath = findViewById(R.id.tvPath);
        tvType = findViewById(R.id.tvType);
        btnSelect= findViewById(R.id.btnSelect);
        btnUpload  = findViewById(R.id.btnUpload);
        btnShare = findViewById(R.id.btnShare);
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();

            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(filePath == null)
                    Toast.makeText(MainActivity.this, "Select a file first", Toast.LENGTH_SHORT).show();

//                else if(!getMimeType(filePath.toString()).equals("application/x-msdos-program"))
//                {
//                    String temp = null;
//                    try {
//                        temp = getStringFromFile(FileUtils.getPath(MainActivity.this,filePath));
//                        if(temp.charAt(0)=='M' && temp.charAt(1) =='Z')
//                        {
////                            uploadFile();
//                            Toast.makeText(MainActivity.this, "This exe file", Toast.LENGTH_SHORT).show();
//
//                        }
//                        else
//                        {
//                            Toast.makeText(MainActivity.this, "Not an exe file", Toast.LENGTH_SHORT).show();
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//
//                }

                else {
                    try {

                        String temp = getStringFromFile(FileUtils.getPath(MainActivity.this, filePath));
                       if(temp.charAt(0) == 'M' && temp.charAt(1) == 'Z')
                       {
//                           uploadFile();
                           Toast.makeText(MainActivity.this, "This is exe file", Toast.LENGTH_SHORT).show();

                       }

                       else
                           Toast.makeText(MainActivity.this, "Not an exe file", Toast.LENGTH_SHORT).show();


                    }catch (IOException e) {
                        e.printStackTrace();
                        Log.d("TAG", "getMimeType: "+e);
                    }
                }
            }

        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(filePath==null)
                    Toast.makeText(MainActivity.this, "Select a file first", Toast.LENGTH_SHORT).show();

                else if(getMimeType(filePath.toString()).equals("application/x-msdos-program"))
                {
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    Uri screenshotUri = filePath;
                    sharingIntent.setType("*/*");
                    sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
                    startActivity(Intent.createChooser(sharingIntent, "Share file using"));
                }

                else
                    Toast.makeText(MainActivity.this, "Not an exe file", Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_SELECT_CODE) {
            if (resultCode == RESULT_OK && data!= null && data.getData() != null) {
                filePath = data.getData();
                Log.d("FilePath", "onActivityResult: " + filePath);
                tvPath.setText(filePath.toString());
                tvType.setText(getMimeType(filePath.toString()));
            }
        }
    }

    private void showFileChooser() {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("application/exe");
        chooseFile = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(chooseFile, FILE_SELECT_CODE);
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    private void uploadFile()
    {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        StorageReference ref = storageReference.child("files/"+ UUID.randomUUID().toString());
        ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
            }
        })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })

                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                .getTotalByteCount());
                        progressDialog.setMessage("Uploaded "+(int)progress+"%");
                    }
                });
    }

    public static String convertStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        Boolean firstLine = true;
        while ((line = reader.readLine()) != null) {
            if(firstLine){
                sb.append(line);
                firstLine = false;
            } else {
                sb.append("\n").append(line);
            }
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile (String filePath) throws IOException {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }


}