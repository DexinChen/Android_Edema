package com.example.dexin.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;

import java.io.Console;

public class SignUpActivity extends AppCompatActivity {
    private String userName;
    private String passWord;
    private String emailString;
    private String phoneString;
    EditText username;
    EditText email;
    EditText password;
    EditText phone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        username = findViewById(R.id.editText);
        email = findViewById(R.id.editText2);
        password = findViewById(R.id.editText3);
        phone = findViewById(R.id.editText6);

        final Button signup = (Button) findViewById(R.id.button4);
        final SignUpHandler signupCallback = new SignUpHandler() {
            @Override
            public void onSuccess(CognitoUser cognitoUser, boolean userConfirmed, CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
                // Sign-up was successful
                // Check if this user (cognitoUser) needs to be confirmed
                if(!userConfirmed) {
                    confirmSignUp(cognitoUserCodeDeliveryDetails);
                }
                else {
                    AlertDialog alertDialog = new AlertDialog.Builder(SignUpActivity.this).create();
                    alertDialog.setTitle("Success");
                    alertDialog.setMessage("Sign up successfully!");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                    // The user has already been confirmed
                }
            }
            @Override
            public void onFailure(Exception exception) {
                Log.d("Failure",exception.toString());
                AlertDialog alertDialog = new AlertDialog.Builder(SignUpActivity.this).create();
                alertDialog.setTitle("Alert");
                alertDialog.setMessage("Sign up failed!");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
                // Sign-up failed, check exception for the cause
            }
        };

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userName = username.getText().toString();
                emailString = email.getText().toString();
                passWord = password.getText().toString();
                phoneString = phone.getText().toString();
                // Create a CognitoUserAttributes object and add user attributes
                CognitoUserAttributes userAttributes = new CognitoUserAttributes();
                // Add the user attributes. Attributes are added as key-value pairs
                // Adding user's email address
                userAttributes.addAttribute("email", emailString);
                userAttributes.addAttribute("phone_number", phoneString);
                Log.d("test",userName+"hello");
                AlertDialog alertDialog = new AlertDialog.Builder(SignUpActivity.this).create();
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
                }
                if(emailString==null||emailString.isEmpty()) {
                    alertDialog.setTitle("Wrong");
                    alertDialog.setMessage("Email can't be empty!");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
                CognitoHelper.getPool().signUpInBackground(userName, passWord, userAttributes, null, signupCallback);
            }
        });

    }
    private void confirmSignUp(CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
        userName = username.getText().toString();
        Intent intent = new Intent(this, SignUpConfirm.class);
        intent.putExtra("source","signup");
        intent.putExtra("name", userName);
        intent.putExtra("destination", cognitoUserCodeDeliveryDetails.getDestination());
        intent.putExtra("deliveryMed", cognitoUserCodeDeliveryDetails.getDeliveryMedium());
        intent.putExtra("attribute", cognitoUserCodeDeliveryDetails.getAttributeName());
        startActivity(intent);
    }
}
