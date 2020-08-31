package com.example.khel;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    EditText mFullName,mEmail,mPassword,mRetypePassword;
    Button mRegisterButton;
    TextView mLoginBtn;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    FirebaseFirestore fStore;
    String userID;
    int strengthPoints = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_register);



        mFullName = findViewById(R.id.fullName);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mRetypePassword = findViewById(R.id.reTypePassword);
        mRegisterButton = findViewById(R.id.loginBtn);
        mLoginBtn = findViewById(R.id.redirectCreateText);


        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progressBar);

        if(fAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getText().toString().trim();
                String myFullname = mFullName.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                // when a user is  registering.
                String myRetypedPass = mRetypePassword.getText().toString().trim();
                final String fullName = mFullName.getText().toString();

                if(TextUtils.isEmpty(myFullname) || myFullname.length() < 6){
                    mPassword.setBackgroundColor(Color.rgb(67,70,75));
                    mFullName.setError("Please enter a username of 6 characters or more");
                    return;
                }

                if(TextUtils.isEmpty(email)) {
                    mPassword.setBackgroundColor(Color.rgb(67,70,75));
                    mEmail.setError("Email is required!");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    mPassword.setError("Password is required!");
                    return;
                }

                if(password.length() < 9) {
                    mPassword.setError("Password must be 9 characters or longer");
                    return;
                }

                if(!password.matches(myRetypedPass)){
                    mPassword.setBackgroundColor(Color.rgb(67,70,75));
                    mRetypePassword.setError("Passwords do not Match. Try again.");
                    return;
                }

                calculateStrength(password);

                if(strengthPoints < 7 ){
                    return;
                }
                //Register user in firebase
                else {
                    progressBar.setVisibility(View.VISIBLE);
                    fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Send verification link
                                FirebaseUser user = fAuth.getCurrentUser();
                                user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(Register.this, "Verification email has been sent.", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("tag", "onFailure: Email not sent " + e.getMessage());
                                    }
                                });

                                Toast.makeText(Register.this, "User Created.", Toast.LENGTH_SHORT).show();
                                userID = fAuth.getCurrentUser().getUid();
                                DocumentReference documentReference = fStore.collection("users").document(userID);


                                //stores data into the firebase firestore
                                Map<String, Object> allUser = new HashMap<>();
                                allUser.put("fName", fullName);
                                allUser.put("email", email);
                                documentReference.set(allUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("TAG", "onSuccess: user profile is created for" + userID);
                                    }
                                });

                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            } else {
                                Toast.makeText(Register.this, "Error !" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Login.class));
            }
        });
    }
    public void calculateStrength(String passwordText) {
        int upperChars = 0, lowerChars = 0, numbers = 0,
                specialChars = 0, otherChars = 0;
        strengthPoints = 0;
        char c;

        int passwordLength = passwordText.length();

        if (passwordLength ==0)
        {
            mPassword.setError("Please enter a password!");
            mPassword.setBackgroundColor(Color.rgb(178,34,34));
            return;
        }

        //If password length is <= 5 set strengthPoints=1
        if (passwordLength <= 5) {
            strengthPoints =1;
        }
        //If password length is >5 and <= 10 set strengthPoints=2
        else if (passwordLength <= 10) {
            strengthPoints = 2;
        }
        //If password length is >10 set strengthPoints=3
        else
            strengthPoints = 3;
        // Loop through the characters of the password
        for (int i = 0; i < passwordLength; i++) {
            c = passwordText.charAt(i);
            // If password contains lowercase letters
            // then increase strengthPoints by 1
            if (c >= 'a' && c <= 'z') {
                if (lowerChars == 0) strengthPoints++;
                lowerChars = 1;
            }
            // If password contains uppercase letters
            // then increase strengthPoints by 1
            else if (c >= 'A' && c <= 'Z') {
                if (upperChars == 0) strengthPoints++;
                upperChars = 1;
            }
            // If password contains numbers
            // then increase strengthPoints by 1
            else if (c >= '0' && c <= '9') {
                if (numbers == 0) strengthPoints++;
                numbers = 1;
            }
            // If password contains _ or @
            // then increase strengthPoints by 1
            else if (c == '_' || c == '@') {
                if (specialChars == 0) strengthPoints += 1;
                specialChars = 1;
            }
            // If password contains any other special chars
            // then increase strengthPoints by 1
            else {
                if (otherChars == 0) strengthPoints += 2;
                otherChars = 1;
            }
        }

        if (strengthPoints <= 3) {
            mPassword.setError("Password Strength : LOW, please include uppercase, lowercase, numbers, symbols, and 9 or more characters");
            mPassword.setBackgroundColor(Color.rgb(178,34,34));
        }
        else if (strengthPoints <= 6) {
            mPassword.setError("Password Strength : MEDIUM, please include uppercase, lowercase, numbers, symbols, and 9 or more characters");
            mPassword.setBackgroundColor(Color.YELLOW);
        }
        else if (strengthPoints <= 9){
            mPassword.setError("Password Strength : HIGH");
            mPassword.setBackgroundColor(Color.GREEN);
        }
    }
}
