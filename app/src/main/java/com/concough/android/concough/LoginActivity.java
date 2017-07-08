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
import com.concough.android.singletons.TokenHandlerSingleton;
import com.concough.android.structures.HTTPErrorType;
import com.concough.android.structures.NetworkErrorType;
import com.concough.android.utils.KeyChainAccessProxy;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

import com.concough.android.settings.ConstantsKt.*;

import static com.concough.android.settings.ConstantsKt.getPASSWORD_KEY;
import static com.concough.android.settings.ConstantsKt.getUSERNAME_KEY;


public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

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
                //Toast.makeText(getApplicationContext(), "Login Clicked", Toast.LENGTH_LONG).show();

                LoginActivity.this.login();
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

    private void login() {
        // get username and password
        String username = LoginActivity.this.usernameEdit.getText().toString().trim();
        final String password = LoginActivity.this.passwordEdit.getText().toString().trim();

        if (!"".equals(username) && !"".equals(password)) {
            // TODO: Show loading

            if (username.startsWith("0"))
                username = username.substring(1);
            username = "98" + username;

            TokenHandlerSingleton.getInstance(getApplicationContext()).setUsernameAndPassword(username, password);
            final String finalUsername = username;

            TokenHandlerSingleton.getInstance(getApplicationContext()).authorize(new Function1<HTTPErrorType, Unit>() {

                @Override
                public Unit invoke(HTTPErrorType httpErrorType) {
                    // TODO: hide loading
                    if (httpErrorType == HTTPErrorType.Success) {
                        if (TokenHandlerSingleton.getInstance(getApplicationContext()).isAuthorized()) {
                            KeyChainAccessProxy.getInstance(getApplicationContext()).setValueAsString(getUSERNAME_KEY(), finalUsername);
                            KeyChainAccessProxy.getInstance(getApplicationContext()).setValueAsString(getPASSWORD_KEY(), password);

                            LoginActivity.this.getProfile();
                        }


                    } else {
                        // TODO: show error with msgType = "HTTPError" and error
                    }
                    return null;
                }
            }, new Function1<NetworkErrorType, Unit>() {
                @Override
                public Unit invoke(NetworkErrorType networkErrorType) {
                    // TODO: hide loading

                    if (networkErrorType != null) {
                        switch (networkErrorType) {
                            case NoInternetAccess:
                            case HostUnreachable:
                            {
                                // TODO: Show error message "NetworkError" with type = "error"
                            }
                            default:
                                // TODO: Show error message "NetworkError" with type = ""

                        }
                    }
                    return null;
                }
            });

        } else {
            // TODO: show message with msgType = "Form" and msgSubType = "EmptyFields"
        }
    }

    private void getProfile() {
        // TODO: Show loading


    }
}
