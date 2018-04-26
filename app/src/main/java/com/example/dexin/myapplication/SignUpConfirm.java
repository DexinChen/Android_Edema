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
import android.widget.TextView;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;

public class SignUpConfirm extends AppCompatActivity {
    String userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_confirm);
        final Button confirm = (Button) findViewById(R.id.button);
        EditText username = findViewById(R.id.editText4);
        userName = username.getText().toString();
        Bundle extras = getIntent().getExtras();
        if (extras !=null) {
            if (extras.containsKey("name")) {
                userName = extras.getString("name");
                username.setText(userName);
            }
        }
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText confcode = findViewById(R.id.editText5);
                String confirmCode = confcode.getText().toString();
                CognitoHelper.getPool().getUser(userName).confirmSignUpInBackground(confirmCode, true, confHandler);
            }
        });
    }
    GenericHandler confHandler = new GenericHandler() {
        @Override
        public void onSuccess() {
            AlertDialog alertDialog = new AlertDialog.Builder(SignUpConfirm.this).create();
            alertDialog.setTitle("Success");
            alertDialog.setMessage("Sign up successfully!");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
            Intent newIntent = new Intent(SignUpConfirm.this, MainActivity.class);
            startActivity(newIntent);
        }

        @Override
        public void onFailure(Exception exception) {
            AlertDialog alertDialog = new AlertDialog.Builder(SignUpConfirm.this).create();
            alertDialog.setTitle("Failed");
            alertDialog.setMessage("Wrong Confirmation Code!");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    };
}
