package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewRentalActivity extends AppCompatActivity {

    private ImageView newRentalImage;
    private TextInputEditText newRentalDesc;
    private TextInputEditText newRentalPrice;
    private Button newRentalBtn;
    private Uri rentalImageURI = null;
    private ProgressBar newRentalProgress;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String currentUserID;
    private Bitmap compressedImageFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_rental);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();

        newRentalImage = findViewById(R.id.new_rental_image);
        newRentalDesc = findViewById(R.id.new_rental_description);
        newRentalPrice = findViewById(R.id.new_rental_price);
        newRentalBtn = findViewById(R.id.rental_btn);
        newRentalProgress = findViewById(R.id.new_rental_progress);
        newRentalImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,512) // Min 500px * 500px image should be uploaded
                        .setAspectRatio(16,9)
                        .start(NewRentalActivity.this);

            }
        });

        newRentalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String desc = newRentalDesc.getText().toString();
                final String price = newRentalPrice.getText().toString();

                //Getting the random name for the rental image.
                final String randomName = UUID.randomUUID().toString();

                if(!TextUtils.isEmpty(desc) && !TextUtils.isEmpty(price) && (rentalImageURI !=null)){

                    newRentalProgress.setVisibility(View.VISIBLE);

                    final StorageReference filePath = storageReference.child("rental_images").child(randomName + ".jpg");

                    //NEW VERSION OF CODE
                    filePath.putFile(rentalImageURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(final Uri uri) {
                                    final Uri downloadURI = uri;

                                    //Compressing the images to load those images for devices with slow internet connection
                                    File newImageFile = new File(rentalImageURI.getPath());
                                    try {
                                        compressedImageFile = new Compressor(NewRentalActivity.this)
                                                .setMaxWidth(100)
                                                .setMaxHeight(200)
                                                .setQuality(2)
                                                .compressToBitmap(newImageFile);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                    byte[] thumbData = baos.toByteArray();

                                    //Storing the thumb files with same random name as the original file.
                                    UploadTask uploadTask = storageReference.child("rental_images/thumbs")
                                            .child(randomName + ".jpg").putBytes(thumbData);

                                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                    // From Firebase 16.0, the way to get the download uri has changed.
                                                    String downloadThumbURI = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
                                                    Map<String, Object> rentalMap = new HashMap<>();
                                                    rentalMap.put("image_url", downloadURI.toString());
                                                    rentalMap.put("image_thumb", downloadThumbURI.toString());
                                                    rentalMap.put("desc", desc);
                                                    rentalMap.put("price", price);
                                                    rentalMap.put("user_id", currentUserID);
                                                    rentalMap.put("timestamp", FieldValue.serverTimestamp());


                                                    firebaseFirestore.collection("Rentals").add(rentalMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentReference> task) {

                                                            if(task.isSuccessful()){
                                                                Toast.makeText(NewRentalActivity.this, "Post added", Toast.LENGTH_LONG).show();
                                                                Intent mainIntent = new Intent(NewRentalActivity.this, MainActivity.class);
                                                                startActivity(mainIntent);
                                                                finish();

                                                            }else{
                                                                String error = task.getException().getMessage();
                                                                Toast.makeText(NewRentalActivity.this, "FireStore Error : " + error, Toast.LENGTH_LONG).show();
                                                            }
                                                            newRentalProgress.setVisibility(View.INVISIBLE);
                                                        }
                                                    });

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // Error Handling
                                                    String error = e.getMessage();
                                                    Toast.makeText(NewRentalActivity.this, "FireStore Error : " + error, Toast.LENGTH_LONG).show();

                                                }
                                            });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    newRentalProgress.setVisibility(View.INVISIBLE);
                                    String setupError = e.getMessage();
                                    Toast.makeText(NewRentalActivity.this, "IMAGE Error : " + setupError, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });

                }

            }
        });

    }

    // Here the image result is returned. If it returns the crop image, then only it proceeds
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                rentalImageURI = result.getUri();
                newRentalImage.setImageURI(rentalImageURI);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }
}
