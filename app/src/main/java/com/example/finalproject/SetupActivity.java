package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private CircleImageView setupImage;
    private Uri mainImageURI = null;
    private TextInputEditText setupName;
    private TextInputEditText setupContactNumber;
    private Button setupBtn;
    private ProgressBar setupProgress;

    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        setupImage = (CircleImageView) findViewById(R.id.setup_image);
        setupName = (TextInputEditText) findViewById(R.id.setup_name);
        setupContactNumber = (TextInputEditText) findViewById(R.id.setup_phone_no);
        setupBtn = (Button) findViewById(R.id.setup_btn);
        setupProgress = (ProgressBar) findViewById(R.id.setup_progress);
        user_id = FirebaseAuth.getInstance().getUid();

        setupProgress.setVisibility(View.VISIBLE);
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                //if the task to retreive the data from firestore is succesfull.
                if(task.isSuccessful()){
                    //If the data exists. Then we want to retrieve the user data
                    if(task.getResult().exists()){
                        String name = task.getResult().getString("name");
                        String contactNo = task.getResult().getString("contact_number");
                        String image = task.getResult().getString("image");

                        //Suppose during updating the settings, the image is shown, but it's value is null as it is set in beginning
                        //So, we changed it's value here for better user experience.
                        mainImageURI = Uri.parse(image);

                        // Setting up the fields. Displaying the user saved data.
                        setupName.setText(name);
                        setupContactNumber.setText(contactNo);

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.default_profile_image);

                        //Third party library glide to load the image in the image view. (Taking image in the form of string.
                        Glide.with(SetupActivity.this).load(image).into(setupImage);
                    }

                }else{
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "FireStore Retrieve Error : " + error, Toast.LENGTH_LONG).show();
                }

                setupProgress.setVisibility(View.INVISIBLE);
            }
        });

        // Uploading the account settings information to the firebase database.
        setupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String userName = setupName.getText().toString();
                final String userContactNumber = setupContactNumber.getText().toString();

                if(!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(userContactNumber) && (mainImageURI!=null)){

                    final String userId = firebaseAuth.getCurrentUser().getUid();
                    setupProgress.setVisibility(View.VISIBLE);

                    final StorageReference imagePath = storageReference.child("profile_images").child(userId + ".jpg");

                    //NEW VERSION OF CODE COMPATIBLE
                    imagePath.putFile(mainImageURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imagePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Uri download_uri = uri;
                                    Map<String, String> userMap = new HashMap<>();
                                    userMap.put("name",userName);
                                    userMap.put("contact_number", userContactNumber);
                                    userMap.put("image", download_uri.toString());

                                    firebaseFirestore.collection("Users").document(userId).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(SetupActivity.this, "Settings updated", Toast.LENGTH_LONG).show();
                                                Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                                                finish();
                                            }else{
                                                String error = task.getException().getMessage();
                                                Toast.makeText(SetupActivity.this, "FireStore Error : " + error, Toast.LENGTH_LONG).show();
                                            }

                                            setupProgress.setVisibility(View.INVISIBLE);
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    String setupError = e.getMessage();
                                    Toast.makeText(SetupActivity.this, "IMAGE Error : " + setupError, Toast.LENGTH_LONG).show();
                                    setupProgress.setVisibility(View.INVISIBLE);

                                }
                            });
                        }
                    });

                    // Old Version of the android code NOT COMPATIBLE.
                    /*imagePath.putFile(mainImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if(task.isSuccessful()){
                               Uri download_uri = task.getResult().getUploadSessionUri();

                               Map<String, String> userMap = new HashMap<>();
                               userMap.put("name",userName);
                               userMap.put("contact_number", userContactNumber);
                               userMap.put("image", download_uri.toString());
                               firebaseFirestore.collection("Users").document(userId).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                   @Override
                                   public void onComplete(@NonNull Task<Void> task) {
                                       if(task.isSuccessful()){
                                           Toast.makeText(SetupActivity.this, "Settings updated", Toast.LENGTH_LONG).show();
                                           Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                                           finish();
                                       }else{
                                           String error = task.getException().getMessage();
                                           Toast.makeText(SetupActivity.this, "FireStore Error : " + error, Toast.LENGTH_LONG).show();
                                       }

                                       setupProgress.setVisibility(View.INVISIBLE);
                                   }
                               });


                            }else{
                                String setupError = task.getException().getMessage();
                                Toast.makeText(SetupActivity.this, "IMAGE Error : " + setupError, Toast.LENGTH_LONG).show();
                                setupProgress.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
*/
                }
            }
        });

        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This means if the permission is not granted then we need to ask the user about the permission.
                if(ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(SetupActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }
                else{
                    // start picker to get image for cropping and then use the image in cropping activity
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1,1)
                            .start(SetupActivity.this);
                }

            }
        });
    }


    // Here the image result is returned
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                //Here we get the cropped returned image.
                mainImageURI = result.getUri();
                setupImage.setImageURI(mainImageURI);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }
}
