package com.example.dexin.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final EditText username = (EditText) findViewById(R.id.editText);
        final EditText password = (EditText) findViewById(R.id.editText2);
        final Button login = (Button) findViewById(R.id.button3);
        final Button signup = (Button) findViewById(R.id.button4);
        CognitoHelper.init(getApplicationContext());
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String userName = username.getText().toString();
                final String passWord = password.getText().toString();
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                if(userName==null||userName.isEmpty()) {
                    alertDialog.setTitle("Wrong");
                    alertDialog.setMessage("Username can't be empty!");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                    return;
                }
                if(passWord==null||passWord.length()<6) {
                    alertDialog.setTitle("Wrong");
                    alertDialog.setMessage("Illegal password!");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                    return;
                }
                CognitoHelper.getPool().getUser(userName).getSessionInBackground(authenticationHandler);
            }
        });
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(MainActivity.this, SignUpActivity.class);
                MainActivity.this.startActivity(newIntent);
            }
        });

    }
    AuthenticationHandler authenticationHandler = new AuthenticationHandler() {

        @Override
        public void onSuccess(CognitoUserSession cognitoUserSession, CognitoDevice device) {
            final EditText username = (EditText) findViewById(R.id.editText);
            Intent newIntent = new Intent(MainActivity.this, MainPageActivity.class);
            newIntent.putExtra("username",username.getText().toString());
            startActivity(newIntent);
            // Sign-in was successful, cognitoUserSession will contain tokens for the user
        }

        @Override
        public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
            EditText password = (EditText) findViewById(R.id.editText2);
            // The API needs user sign-in credentials to continue

            AuthenticationDetails authenticationDetails = new AuthenticationDetails(userId, password.getText().toString(), null);

            // Pass the user sign-in credentials to the continuation
            authenticationContinuation.setAuthenticationDetails(authenticationDetails);

            // Allow the sign-in to continue
            authenticationContinuation.continueTask();
        }

        @Override
        public void getMFACode(MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation) {
            // Multi-factor authentication is required; get the verification code from user
            // Allow the sign-in process to continue
            multiFactorAuthenticationContinuation.continueTask();
        }

        @Override
        public void onFailure(Exception exception) {
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("Wrong");
            alertDialog.setMessage("Sign in failed!");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
            // Sign-in failed, check exception for the cause
        }

        @Override
        public void authenticationChallenge(ChallengeContinuation continuation) {
            // to be implemented
        }
    };
}
