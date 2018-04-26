package com.example.dexin.myapplication;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.util.IOUtils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class MainPageActivity extends AppCompatActivity {
    private static final String IMAGE_DIRECTORY_NAME = "HelloCamera";
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static String globalUri = "";
    public static String username = "";
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private Uri fileUri; // file url to store image/video
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent myIntent = getIntent();
        username = myIntent.getStringExtra("username");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        final Button record = (Button) findViewById(R.id.Button);
        final Button upload = (Button) findViewById(R.id.Button2);
        final Button result = (Button) findViewById(R.id.Button3);
        final Button history = (Button) findViewById(R.id.Button4);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e("TEST", "no permission");
            // Permission is not granted
        }
        else {
            Log.e("TEST", "hava permission");
        }
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordVideo();
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // record video
                //Intent.ACTION_PICK,MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                Intent galleryIntent = new Intent();
                galleryIntent.setType("video/*");
                // Start the Intent
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"Select Video"), MEDIA_TYPE_VIDEO);
            }
        });
        result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(MainPageActivity.this, LastResult.class);
                newIntent.putExtra("user",username);
                MainPageActivity.this.startActivity(newIntent);
            }
        });
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(MainPageActivity.this, ResultActivity.class);
                newIntent.putExtra("user",username);
                MainPageActivity.this.startActivity(newIntent);
            }
        });
        if (!isDeviceSupportCamera()) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Your device doesn't support camera",
                    Toast.LENGTH_LONG).show();
            // will close the app if the device does't have camera
            finish();
        }
    }
    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }
    private void recordVideo() {
        Intent myIntent = getIntent();
        String username = myIntent.getStringExtra("username");
        /*
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
        globalUri = fileUri.toString();
        Log.e("chendexintest",fileUri.toString());
        // set video quality
        //intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        //intent.putExtra("username",username);
        //intent.putExtra("url",fileUri.toString());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file
        intent.putExtra("return-data", true);
        // name

        // start the video capture Intent
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
        }
        else {
            Log.e("dexinChen","error");
        }
        */
        Uri myUri = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new ContentValues());
        globalUri = myUri.toString();
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, myUri);
        startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
    }
    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    private void jumpToShow(String pre) {
        Intent newIntent = new Intent(this, UploadShow.class);
        newIntent.putExtra("predict",pre);
        startActivity(newIntent);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }
    /**
     * Receiving activity result method will be called after closing the camera
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
          Intent myIntent = getIntent();
          final String username = myIntent.getStringExtra("username");

          if(requestCode == MEDIA_TYPE_VIDEO && resultCode == RESULT_OK && null != data) {
              final Uri selectedVideo = data.getData();

              try {


                  GetDetailsHandler getDetailsHandler = new GetDetailsHandler() {
                      @Override
                      public void onSuccess(CognitoUserDetails cognitoUserDetails) {
                          Log.e("cognitohhhhhh",cognitoUserDetails.getAttributes().getAttributes().toString());
                          String phone = cognitoUserDetails.getAttributes().getAttributes().get("phone_number").toString();
                          phone = phone.substring(2);
                          Log.e("cognitohhhhhh",phone);
                          try {
                              InputStream iStream = getApplicationContext().getContentResolver().openInputStream(selectedVideo);
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
                  Intent newIntent = new Intent(this, UploadShow.class);
                  startActivity(newIntent);
/*
                  String Key = "iotedema-20180331_191111.mp4";
                  AsyncHttpClient client = new AsyncHttpClient();
                  String apiUrl = "https://33dkp3luo3.execute-api.us-east-1.amazonaws.com/Test";
                  String requestUrl = apiUrl+"?videoKey="+Key;
                  client.get(requestUrl, new AsyncHttpResponseHandler() {

                      @Override
                      public void onStart() {
                          // called before request is started
                      }

                      @Override
                      public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                          String pre = new String(response);

                          AlertDialog alertDialog = new AlertDialog.Builder(MainPageActivity.this).create();
                          alertDialog.setTitle("predict result");
                          alertDialog.setMessage(pre);
                          alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                  new DialogInterface.OnClickListener() {
                                      public void onClick(DialogInterface dialog, int which) {
                                          dialog.dismiss();
                                      }
                                  });
                          alertDialog.show();

                          jumpToShow(pre);
                          // called when response HTTP status is "200 OK"
                      }

                      @Override
                      public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                          // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                      }

                      @Override
                      public void onRetry(int retryNo) {
                          // called when request is retried
                      }
                  });

*/
              }
              catch (Exception e) {
                  Log.e("error","file not found!");
              }

              //Intent newIntent = new Intent(this, ShowActivity.class);
              //newIntent.putExtra("content",filePath);
              //Log.e("ahhhhhhhhhhhhhhhhhhhh","I am here 222222222");
              //startActivity(newIntent);
        }
        if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE && null != data) {
            if (resultCode == RESULT_OK) {

                Uri videoUri = data.getData();
                Log.e("testtttttttttt",videoUri.getPath());
                Intent newIntent = new Intent(this, ShowActivity.class);
                newIntent.putExtra("content",videoUri.getPath());
                newIntent.putExtra("username",username);
                startActivity(newIntent);
                // video successfully recorded
                // launching upload activity
                ////launchUploadActivity(false);

            } else if (resultCode == RESULT_CANCELED) {

                // user cancelled recording
                Toast.makeText(getApplicationContext(),
                        "User cancelled video recording", Toast.LENGTH_SHORT)
                        .show();

            } else {
                // failed to record video
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to record video", Toast.LENGTH_SHORT)
                        .show();
            }
        }
        else if(requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Intent newIntent = new Intent(this, ShowActivity.class);
                newIntent.putExtra("content",globalUri);
                newIntent.putExtra("username",username);
                startActivity(newIntent);
                // video successfully recorded
                // launching upload activity
                ////launchUploadActivity(false);

            } else if (resultCode == RESULT_CANCELED) {

                // user cancelled recording
                Toast.makeText(getApplicationContext(),
                        "User cancelled video recording", Toast.LENGTH_SHORT)
                        .show();

            } else {
                // failed to record video
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to record video", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }
    private File getOutputMediaFile(int type) {
        // External sdcard location
        Log.e("pre","11111111111111111111111111111111111");
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);
        Log.e("mid","11111111111111111111111111111111111");
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }
        Log.e("late",mediaStorageDir.toString());
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        } else {
            return null;

        }
        Log.e("fileInfo","hhhhhhhhhhhhhhhhhhhh");
        Log.e("fileInfo",mediaFile.getPath());
        return mediaFile;
    }
    /*private void launchUploadActivity(boolean isImage){
        Intent i = new Intent(MainPageActivity.this, UploadActivity.class);
        i.putExtra("filePath", fileUri.getPath());
        i.putExtra("isImage", isImage);
        startActivity(i);
    }*/

    /**
     * Creating file uri to store image/video
     */
    public void uploadData() {

    }

}
