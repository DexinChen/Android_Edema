package com.example.dexin.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.http.HttpClient;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.mobileconnectors.dynamodbv2.document.Table;
import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.Document;
import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.Primitive;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.util.IOUtils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;


public class ShowActivity extends AppCompatActivity {
    String key = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        Intent myIntent = getIntent();
        final String username = myIntent.getStringExtra("username");
        final Uri myUri = Uri.parse(myIntent.getStringExtra("content"));
        //String path = myIntent.getStringExtra("content");
        //Log.e("uri",path);

        try {
            GetDetailsHandler getDetailsHandler = new GetDetailsHandler() {
                @Override
                public void onSuccess(CognitoUserDetails cognitoUserDetails) {
                    Log.e("cognitohhhhhh",cognitoUserDetails.getAttributes().getAttributes().toString());
                    String phone = cognitoUserDetails.getAttributes().getAttributes().get("phone_number").toString();
                    phone = phone.substring(2);
                    Log.e("cognitohhhhhh",phone);
                    try {
                        InputStream iStream = getApplicationContext().getContentResolver().openInputStream(myUri);
                        File tempFile = File.createTempFile("hello", "C");
                        tempFile.deleteOnExit();
                        FileOutputStream out = new FileOutputStream(tempFile);
                        IOUtils.copy(iStream, out);
                        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                                getApplicationContext(),
                                "us-east-1:db12e203-9a39-43fa-8dab-2309d4309d39", // Identity Pool ID
                                Regions.US_EAST_1 // Region
                        );
                        AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
                        s3.setRegion(Region.getRegion(Regions.US_EAST_1));
                        // Initialize AWSMobileClient if not initialized upon the app startup.
                        // AWSMobileClient.getInstance().initialize(this).execute();
                        TransferUtility transferUtility = TransferUtility.builder()
                                .context(getApplicationContext())
                                .s3Client(s3)
                                .build();
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                                Locale.getDefault()).format(new Date());
                        String key = "videos/"+username+"-"+phone+"-"+timeStamp+".mp4";
                        TransferObserver uploadObserver =
                                transferUtility.upload(
                                        "edema",
                                        key,
                                        tempFile);
                        uploadObserver.setTransferListener(new TransferListener() {

                            @Override
                            public void onStateChanged(int id, TransferState state) {
                                if (TransferState.COMPLETED == state) {
                                    // Handle a completed upload.
                                }
                            }

                            @Override
                            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                                float percentDonef = ((float)bytesCurrent/(float)bytesTotal) * 100;
                                int percentDone = (int)percentDonef;

                                Log.d("MainActivity", "   ID:" + id + "   bytesCurrent: " + bytesCurrent + "   bytesTotal: " + bytesTotal + " " + percentDone + "%");
                            }

                            @Override
                            public void onError(int id, Exception ex) {
                                // Handle errors
                            }

                        });

                        // If your upload does not trigger the onStateChanged method inside your
                        // TransferListener, you can directly check the transfer state as shown here.
                        if (TransferState.COMPLETED == uploadObserver.getState()) {
                            // Handle a completed upload.
                        }
                    }
                    catch (Exception e) {

                    }
                    // The user detail are in cognitoUserDetails
                }

                @Override
                public void onFailure(Exception exception) {
                    Log.e("cognitoeeeee",exception.toString());
                    // Fetch user details failed, check exception for the cause
                }
            };
            CognitoHelper.init(getApplicationContext());
            CognitoHelper.getPool().getUser(username).getDetailsInBackground(getDetailsHandler);
        }
        catch(Exception e) {
            Log.e("errorplace",String.valueOf(e));
        }
        //VideoView videoView = (VideoView) findViewById(R.id.videoView);
        //videoView.setVisibility(View.VISIBLE);
        //videoView.setVideoPath(myUri.getPath());
        // start playing
        //videoView.start();

        //getresult(key);


        /*
        Log.e("error","1");
        AmazonDynamoDBClient dbClient = new AmazonDynamoDBClient(credentialsProvider);

        Log.e("error","2");
        Table dbTable = Table.loadTable(dbClient, "edemaResult");

        Log.e("error","3");
        Document retrievedDoc = dbTable.getItem(new Primitive("iotedema-20180331_191111.mp4"));
        Log.e("error","4");
        String predict = retrievedDoc.get("predict_result").asString();
        Log.e("error","5");
        AlertDialog alertDialog = new AlertDialog.Builder(ShowActivity.this).create();
        alertDialog.setTitle("Predict Result");
        alertDialog.setMessage("Edema lever:"+predict);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
        */
    }
    /*
      private void getresult(String k) {

          try {
              Thread.sleep(60000);

          } catch (InterruptedException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
          }

        AsyncHttpClient client = new AsyncHttpClient();

        String Key = k;

        String apiUrl = "https://33dkp3luo3.execute-api.us-east-1.amazonaws.com/Test";

        String requestUrl = apiUrl + "/?videoKey=" + Key;
        Log.e("requestURL",String.valueOf(requestUrl));
        client.get(requestUrl, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                Log.e("start","onStart");
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                String pre = new String(response);
                Log.e("responsessssssssss", pre);
                pre = pre.substring(1, 2);
                AlertDialog alertDialog = new AlertDialog.Builder(ShowActivity.this).create();
                alertDialog.setTitle("predict result");
                alertDialog.setMessage(pre);
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();

                ImageView graph = (ImageView) findViewById(R.id.imageView5);
                if (pre.equals("0"))
                    graph.setImageResource(R.drawable.stage0);
                else if (pre.equals("1"))
                    graph.setImageResource(R.drawable.stage1);
                else if (pre.equals("2"))
                    graph.setImageResource(R.drawable.stage2);
                else if (pre.equals("3"))
                    graph.setImageResource(R.drawable.stage3);
                else if (pre.equals("4"))
                    graph.setImageResource(R.drawable.stage4);
                // called when response HTTP status is "200 OK"
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                Log.e("failure",String.valueOf(errorResponse));
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
            }

            @Override
            public void onRetry(int retryNo) {
                Log.e("retry",String.valueOf(retryNo));
                // called when request is retried
            }
        });

    }
    */
}
