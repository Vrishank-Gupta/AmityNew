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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    TextView tvPath,tvType;
    private Uri filePath;
    Button btnSelect, btnUpload,btnShare;
    final int FILE_SELECT_CODE= 1;
    FirebaseStorage storage;
    private StorageReference storageReference;
    private static final int[] SIGNATURE_PNG = {77,90};
    private static final int[] SIGNATURE_JPEG = {45,22};
    private static final int[] SIGNATURE_GIF = {34,22};

    public static final int SIGNATURE_ID_JPEG = 0;
    public static final int SIGNATURE_ID_PNG = 1;
    public static final int SIGNATURE_ID_GIF = 2;
    private static final int[][] SIGNATURES = new int[3][];

    static {
        SIGNATURES[SIGNATURE_ID_JPEG] = SIGNATURE_JPEG;
        SIGNATURES[SIGNATURE_ID_PNG] = SIGNATURE_PNG;
        SIGNATURES[SIGNATURE_ID_GIF] = SIGNATURE_GIF;
    }

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

                else if(getMimeType(filePath.toString()).equals("application/x-msdos-program"))
                {
//                    uploadFile();
                    try {
                        String temp = convertMediaUriToPath(filePath);
                        Log.d("Signature ", "onClick: " + temp);
                        InputStream inputStream = new FileInputStream(filePath.getPath());

//                        Log.d("Signature Final ", "onClick: " + getSignatureIdFromHeader(inputStream));

                        File file = new File(filePath.toString());
                        int size = (int) file.length();
                        byte[] bytes = new byte[size];
                        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                        buf.read(bytes, 0, bytes.length);
                        buf.close();
                        InputStream is = new BufferedInputStream(new ByteArrayInputStream(bytes));
                        String mimeType = URLConnection.guessContentTypeFromStream(is);
                        Log.d("TAG", "getMimeType: "+mimeType);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Log.d("TAG", "getMimeType: "+e);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d("TAG", "getMimeType: "+e);
                    }
                }

                else
                    Toast.makeText(MainActivity.this, "Not an exe file", Toast.LENGTH_SHORT).show();
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


    public static int getSignatureIdFromHeader(InputStream is) throws IOException {
        // read signature from head of source and compare with known signatures
        int signatureId = -1;
        int sigCount = SIGNATURES.length;
        int[] byteArray = new int[8];
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            byteArray[i] = is.read();
            builder.append(Integer.toHexString(byteArray[i]));
        }
            Log.d("Signature ", "head bytes=" + builder.toString());

        for (int i = 0; i < 8; i++) {

            // check each bytes with known signatures
            int bytes = byteArray[i];
            int lastSigId = -1;
            int coincidences = 0;

            for (int j = 0; j < sigCount; j++) {
                int[] sig = SIGNATURES[j];

                Log.d("Signature " , "compare" + i + ": " + Integer.toHexString(bytes) + " with " + sig[i]);

                if (bytes == sig[i]) {
                    lastSigId = j;
                    coincidences++;
                }
            }

            // signature is unknown
            if (coincidences == 0) {
                break;
            }
            // if first bytes of signature is known we check signature for full coincidence
            if (coincidences == 1) {
                int[] sig = SIGNATURES[lastSigId];
                int sigLength = sig.length;
                boolean isSigKnown = true;
                for (; i < 8 && i < sigLength; i++) {
                    bytes = byteArray[i];
                    if (bytes != sig[i]) {
                        isSigKnown = false;
                        break;
                    }
                }
                if (isSigKnown) {
                    signatureId = lastSigId;
                }
                break;
            }
        }
        return signatureId;
    }

    public String convertMediaUriToPath(Uri uri) {
        String [] proj={MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, proj,  null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }

}