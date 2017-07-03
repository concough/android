package com.concough.android.concough;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.StringDef;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.concough.android.rest.AuthRestAPIClass;
import com.concough.android.singletons.UrlMakerSingleton;
import com.concough.android.structures.HTTPErrorType;
import com.concough.android.structures.NetworkErrorType;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.HashMap;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final String USERNAME_KEY = "Username";

    public interface LoginService {
        @Headers({
                "Content-Type: application/json; charset=utf-8",
        })
        @POST("pre_signup/")
        Call<ResponseBody> pre_signup(@Body HashMap<String, Object> body);
    }

    public static Intent newIntent(Context packageContext) {
        Intent i = new Intent(packageContext, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return i;
    }

    public interface CCC {
        void method1(JsonObject j, HTTPErrorType err);
    }
    public interface CCC2 {
        void method1(HTTPErrorType err);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.d(TAG, "onCreate: Hiiii");
        String d = getIntent().getStringExtra(USERNAME_KEY);
        Log.d(TAG, "onCreate: " + d);

        login();

        AuthRestAPIClass.checkUsername("91233333", new Function2<JsonObject, HTTPErrorType, Unit>() {
            @Override

            public Unit invoke(JsonObject jsonObject, HTTPErrorType httpErrorType) {
                return null;
            }
        }, new Function1<NetworkErrorType, Unit>() {
            @Override
            public Unit invoke(NetworkErrorType networkErrorType) {
                return null;
            }
        });
    }

    protected void login() {
        String url = "http://192.168.1.15:8000/api/v1/j/auth/";
//        String url = "http://www.filltext.ir/";

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(url)
                .build();

        HashMap<String, Object> d = new HashMap<String, Object>();
        d.put("username", "989124444444");

        LoginActivity.LoginService ss = retrofit.create(LoginActivity.LoginService.class);
        Call<ResponseBody> data = ss.pre_signup(d);
        data.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    String res = response.body().string();
                    Log.d(TAG, "onResponse: " + res);

                    JsonObject jobj = new Gson().fromJson(res, JsonObject.class);
                    int result = jobj.get("id").getAsInt();
                    Log.d(TAG, "onResponse: id = " + result);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }
}
