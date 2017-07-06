package com.concough.android.concough;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.concough.android.singletons.FontCacheSingleton;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final String USERNAME_KEY = "Username";

    private TextView signupTextView;
    private Button loginButton;
    private EditText usernameEdit;
    private EditText passwordEdit;
    private TextView loginHintTextView;
    private Button registerButton;


    public static Intent newIntent(Context packageContext) {
        Intent i = new Intent(packageContext, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return i;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        View.OnClickListener registerButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = SignupActivity.newIntent(LoginActivity.this);
                startActivity(i);
                finish();
            }
        };
        registerButton = (Button) findViewById(R.id.loginA_registerButton);
        registerButton.setOnClickListener(registerButtonListener);


        View.OnClickListener loginButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Login Clicked",
                        Toast.LENGTH_LONG).show();

            }
        };
        loginButton = (Button) findViewById(R.id.loginA_loginButton);
        loginButton.setOnClickListener(loginButtonListener);

        signupTextView = (TextView) findViewById(R.id.loginA_signupTextView);
        usernameEdit = (EditText) findViewById(R.id.loginA_usernameEdit);
        passwordEdit = (EditText) findViewById(R.id.loginA_passwordEdit);
        loginHintTextView = (TextView) findViewById(R.id.loginA_loginHintTextView);
        registerButton = (Button) findViewById(R.id.loginA_registerButton);

        usernameEdit.requestFocus();

        signupTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());
        usernameEdit.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
        passwordEdit.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
        loginHintTextView.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
        registerButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());


    }
}
