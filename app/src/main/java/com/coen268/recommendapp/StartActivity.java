package com.coen268.recommendapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class StartActivity extends AppCompatActivity {

    Button loginButton;
    EditText email;
    EditText password;
    TextView createAccount;

    private FirebaseAuth mAuth;
    private FirebaseFunctions mFunctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        loginButton = (Button) findViewById(R.id.bt_login);
        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        createAccount = findViewById(R.id.tv_account);

//        BitmapDrawable bitDw = (BitmapDrawable) this.getDrawable(R.drawable.landmark);
//
//        Bitmap bitmap = bitDw.getBitmap();
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//        byte[] imageInByte = stream.toByteArray();
//        ByteArrayInputStream bis = new ByteArrayInputStream(imageInByte);
//        try {
//            DetectWebDetections.detectWebDetections(bis);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
//        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser();

//        mAuth.createUserWithEmailAndPassword("daozhewrr@gmail.com", "password")
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            // Sign in success, update UI with the signed-in user's information
//                            Log.d("TAG", "createUserWithEmail:success");
//                            FirebaseUser user = mAuth.getCurrentUser();
//                        } else {
//                            // If sign in fails, display a message to the user.
//                            Log.w("TAG", "createUserWithEmail:failure", task.getException());
//                            Toast.makeText(getBaseContext(), "Authentication failed.",Toast.LENGTH_SHORT).show();
//                        }
//
//                        // ...
//                    }
//                });

        if (mAuth.getCurrentUser() != null){
            Intent intent = new Intent(this, PicActivity.class);

            if (getIntent().getExtras()!=null) {
                intent.putExtras(getIntent().getExtras());
            }

            startActivity(intent);
        }

//        mAuth.signInWithEmailAndPassword("daozhewrr@gmail.com", "password")
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            // Sign in success, update UI with the signed-in user's information
//                            Log.d("TAG", "signInWithEmail:success");
//                            FirebaseUser user = mAuth.getCurrentUser();
////                            updateUI(user);
//                        } else {
//                            // If sign in fails, display a message to the user.
//                            Log.w("TAG", "signInWithEmail:failure", task.getException());
//                            Toast.makeText(getBaseContext(), "Authentication failed.",
//                                    Toast.LENGTH_SHORT).show();
////                            updateUI(null);
//                            // ...
//                        }
//
//                        // ...
//                    }
//                });


//        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(),
//                R.drawable.landmark);
//
//        // Scale down bitmap size
//        bitmap = scaleBitmapDown(bitmap, 640);
////        // Convert bitmap to base64 encoded string
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
//        byte[] imageBytes = byteArrayOutputStream.toByteArray();
//        String base64encoded = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
//
//        mFunctions = FirebaseFunctions.getInstance();
//        // Create json request to cloud vision
//        JsonObject request = new JsonObject();
//// Add image to request
//        JsonObject image = new JsonObject();
//        image.add("content", new JsonPrimitive(base64encoded));
//        request.add("image", image);
////Add features to the request
//        JsonObject feature = new JsonObject();
//        feature.add("maxResults", new JsonPrimitive(5));
//        feature.add("type", new JsonPrimitive("LANDMARK_DETECTION"));
//        JsonArray features = new JsonArray();
//        features.add(feature);
//        request.add("features", features);
//
//        annotateImage(request.toString())
//                .addOnCompleteListener(new OnCompleteListener<JsonElement>() {
//                    @Override
//                    public void onComplete(@NonNull Task<JsonElement> task) {
//                        if (!task.isSuccessful()) {
//                            // Task failed with an exception
//                            // ...
//                            Toast.makeText(getBaseContext(),"failed",Toast.LENGTH_LONG).show();
//                        } else {
//                            // Task completed successfully
//                            // ...
//                            Toast.makeText(getBaseContext(),"success",Toast.LENGTH_LONG).show();
//                        }
//                    }
//                });

    }

    public void login(View view) {

        String emailText = email.getText().toString().trim();
        String passwordText = password.getText().toString().trim();

        if (TextUtils.isEmpty(emailText)) {
            email.setError("Email is required.");
            return;
        }

        if (TextUtils.isEmpty(passwordText)) {
            password.setError("Password is required.");
            return;
        }

        Intent intent = new Intent(this, PicActivity.class);
        intent.putExtras(getIntent().getExtras());

        mAuth.signInWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithEmail:success");
                            Toast.makeText(StartActivity.this,
                                    "Logged in successfully!", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            startActivity(intent);
//                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithEmail:failure", task.getException());
                            Toast.makeText(getBaseContext(), "You may enter the wrong login name or password",
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                            // ...
                        }

                        // ...
                    }
                });
    }

    public void userRegister(View view){
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

}