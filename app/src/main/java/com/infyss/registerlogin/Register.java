package com.infyss.registerlogin;

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
import android.os.Handler;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class Register extends AppCompatActivity {

    MaterialEditText email, name, password;
    Button register;
    CircleImageView image;
    private FirebaseAuth mAuth;

    private KProgressHUD hud;


    static int PReqcode = 1;
    static int REQUESCODE = 1;

    Uri pickedImageUri;


    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^"+"(?=.*[a-zA-Z])"+"(?=.*[@#$%^&+=])"+"(?=\\S+$)"+".{4,}"+"$");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email = findViewById(R.id.user_email);
        name = findViewById(R.id.user_name);
        password = findViewById(R.id.user_password);
        register = findViewById(R.id.register);
        image = findViewById(R.id.user_image);


        mAuth = FirebaseAuth.getInstance();

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);



            image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>=22) {
                    checkAndRequestForPermission();
                }
                else {
                    openGallery();
                }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String user_email = email.getText().toString();
                String user_name = name.getText().toString();
                String user_password = password.getText().toString();

                if(!validateEmail(user_email) | !validateName(user_name) | !validatePassword(user_password)) {
                        return;
                }

                else {
                        progressBar();
                        createAccount(user_email, user_name, user_password);
                }

            }
        });




    }

    private void createAccount(final String email, final String name, final String password) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {

                                imageUploadMethod(pickedImageUri, name, mAuth.getCurrentUser());

                                String user_id = mAuth.getUid();
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(user_id);

                                Map map = new HashMap();
                                map.put("email", email);
                                map.put("name", name);
                                map.put("password", password);
                                map.put("imageurl", "https://firebasestorage.googleapis.com/v0/b/instagramtest-fcbef.appspot.com/o/placeholder.png?alt=media&token=b09b809d-a5f8-499b-9563-5252262e9a49");


                                reference.setValue(map);

                            } else {

                                stopProgressBar();
                                showMessage("Account Creation Failed " + task.getException().getMessage());


                            }

                        }
                    });
    }

    private void imageUploadMethod(Uri pickedImageUri, final String name, final FirebaseUser currentUser) {

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("User Photos");
        final StorageReference imageFilePath = storageReference.child(pickedImageUri.getLastPathSegment());
        imageFilePath.putFile(pickedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder().
                                setDisplayName(name).
                                setPhotoUri(uri).
                                build();

                        currentUser.updateProfile(profileUpdate).
                                addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            stopProgressBar();
                                            Intent intent = new Intent(Register.this, Home.class);
                                            startActivity(intent);
                                            showMessage("Welcome "+ name);
                                        }
                                    }
                                });
                    }
                });
            }
        });

    }

    private void openGallery() {

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUESCODE);
    }

    private void checkAndRequestForPermission() {

        if(ContextCompat.checkSelfPermission(Register.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(Register.this,Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(Register.this, "Please accept for Required Permission",Toast.LENGTH_SHORT).show();
            }

            else {
                ActivityCompat.requestPermissions(Register.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},PReqcode);
            }
        }
        else {
            openGallery();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode ==  REQUESCODE && data!= null) {
            pickedImageUri = data.getData();
            image.setImageURI(pickedImageUri);
        }
    }

    private boolean validatePassword(String user_password) {


        if(user_password.isEmpty()){

            password.setError("Field can't be empty");
            return false;
        }else  if(!PASSWORD_PATTERN.matcher(user_password).matches()){
            password.setError("Password too weak");
            return false;
        }else{
            password.setError(null);
            return true;
        }

    }

    private boolean validateName(String user_name) {

        String regex = "^[a-zA-z][a-zA-Z ]*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(user_name);

        if(user_name.isEmpty()){

            name.setError("Field can't be empty");
            return false;
        }else  if(user_name.length() > 35){
            name.setError("Username too long");
            return false;
        }else if(!matcher.find()){
            name.setError("Username cannot contain space or cannot start with space");
            return false;
        }
        else{
            return true;
        }

    }

    private boolean validateEmail(String user_email) {

        if(user_email.isEmpty()){

            email.setError("Field can't be empty");
            return false;
        }else  if(!Patterns.EMAIL_ADDRESS.matcher(user_email).matches()){
            email.setError("Please enter a valid email address");
            return false;
        }else{
            email.setError(null);
            return true;
        }

    }

    private void showMessage(String message) {

        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

    private void progressBar(){

        hud = KProgressHUD.create(Register.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setDimAmount(0.5f);

        hud.show();
    }

    private void stopProgressBar() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hud.dismiss();
            }
        }, 100);
    }



}