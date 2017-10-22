package com.example.appmodel.ridematcher;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONObject;

import java.util.Arrays;

public class SocialSignUpPage extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    GoogleApiClient googleApiClient;
    static int GOOGLE_SIGNIN = 1000;

    public final static String SOCIAL_TO_INFORMATION_EMAIL = "com.example.appmodel.ridematcher_SOCIAL_TO_INFORMATION_EMAIL";
    public final static String SOCIAL_TO_INFORMATION_FIRST = "com.example.appmodel.ridematcher_SOCIAL_TO_INFORMATION_FIRST";
    public final static String SOCIAL_TO_INFORMATION_LAST= "com.example.appmodel.ridematcher_SOCIAL_TO_INFORMATION_LAST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_sign_up_page);

        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().requestProfile().build();

        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, options)
                .build();


        LoginButton facebook = (LoginButton)findViewById(R.id.FacebookLoginButton);
        facebook.setReadPermissions(Arrays.asList("email", "public_profile"));
        CallbackManager cm = CallbackManager.Factory.create();

        facebook.registerCallback(cm, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try {
                            String email = object.getString("email");
                            String first = object.getString("first_name");
                            String last = object.getString("last_name");
                            startActivity(new Intent(getApplicationContext(), InformationPage.class)
                            .putExtra(SOCIAL_TO_INFORMATION_EMAIL, email).putExtra(SOCIAL_TO_INFORMATION_FIRST, first)
                            .putExtra(SOCIAL_TO_INFORMATION_LAST, last));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onCancel() {
                Toast.makeText(SocialSignUpPage.this, "Cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(SocialSignUpPage.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        (findViewById(R.id.GoogleLoginButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent googleSignIn = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(googleSignIn, GOOGLE_SIGNIN);
            }
        });
    }

    @Override
    public void onActivityResult(int responseCode, int requestCode, Intent data){
        if (responseCode == GOOGLE_SIGNIN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleResult(result);
        }
    }

    public void handleResult(GoogleSignInResult result){
        if (result.isSuccess()){
            GoogleSignInAccount account = result.getSignInAccount();
            if (account != null){
                String email = account.getEmail();
                String name = account.getDisplayName();
                if (name != null) {
                    String first = name.substring(0, name.indexOf(" "));
                    String last = name.substring(name.indexOf(" ") + 1);
                    startActivity(new Intent(getApplicationContext(), InformationPage.class)
                            .putExtra(SOCIAL_TO_INFORMATION_EMAIL, email).putExtra(SOCIAL_TO_INFORMATION_FIRST, first)
                            .putExtra(SOCIAL_TO_INFORMATION_LAST, last));
                }
                else {
                    startActivity(new Intent(getApplicationContext(), InformationPage.class)
                            .putExtra(SOCIAL_TO_INFORMATION_EMAIL, email).putExtra(SOCIAL_TO_INFORMATION_FIRST, "")
                            .putExtra(SOCIAL_TO_INFORMATION_LAST, ""));
                }
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
