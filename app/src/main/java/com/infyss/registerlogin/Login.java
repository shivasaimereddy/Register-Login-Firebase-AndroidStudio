package com.infyss.registerlogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;

public class Login extends AppCompatActivity {

    private KProgressHUD hud;

    MaterialEditText email, password;
    Button login;

    FirebaseAuth mAuth;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^"+"(?=.*[a-zA-Z])"+"(?=.*[@#$%^&+=])"+"(?=\\S+$)"+".{4,}"+"$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.login_email);
        password = findViewById(R.id.login_password);
        login = findViewById(R.id.login);


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String user_email = email.getText().toString();
                String user_password = password.getText().toString();

                if(!validateEmail(user_email) | !validatePassword(user_password)) {
                    return;
                }

                else {
                    progressBar();

                    mAuth.signInWithEmailAndPassword(user_email, user_password).
                            addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()) {
                                        Toast.makeText(Login.this,"Login Successful",Toast.LENGTH_SHORT).show();
                                        stopProgressBar();
                                        startActivity(new Intent(Login.this, Home.class));
                                    }
                                    else{
                                        Toast.makeText(Login.this, "Authentication Failed",Toast.LENGTH_SHORT).show();
                                        stopProgressBar();

                                    }
                                }
                            });

                }

            }
        });


    }

    private boolean validatePassword(String user_password) {


        if(user_password.isEmpty()){

            password.setError("Field can't be empty");
            return false;

        }else{
            password.setError(null);
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

        hud = KProgressHUD.create(Login.this)
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