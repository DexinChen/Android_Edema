package com.example.dexin.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Comparator;

import cz.msebera.android.httpclient.Header;

public class LastResult extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_result);
        Intent myIntent = getIntent();
        String username = myIntent.getStringExtra("user");

        AsyncHttpClient client = new AsyncHttpClient();
        String apiUrl = "https://qdzlfely0e.execute-api.us-east-1.amazonaws.com/getResult";
        String requestUrl = apiUrl+"?user="+username;
        client.get(requestUrl, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                String pre = new String(response);
                try {
                    JSONObject json = new JSONObject(pre);
                    JSONArray msg = (JSONArray) json.get("Items");
                    Pair[] info = new Pair[msg.length()];
                    Log.e("place1","hh");
                    for (int i = 0; i < msg.length(); i++) {
                        JSONObject one = msg.getJSONObject(i);
                        String time = (String) one.get("time");
                        String predict = (String) one.get("predict_result");
                        info[i] = new Pair(time,predict);
                        Log.e("place2","hh");
                    }
                    Arrays.sort(info, new Comparator<Pair>() {
                        public int compare(Pair a, Pair b) {
                            return a.time.compareTo(b.time);
                        }
                    });
                    Log.e("place3","hh");

                    int num = info.length;
                    int res = Integer.parseInt(info[num-1].predict);
                    String time = info[num-1].time;
                    TextView text = (TextView) findViewById(R.id.textView4);
                    text.setText(time);
                    ImageView graph = (ImageView) findViewById(R.id.imageView5);
                    if (res==0)
                        graph.setImageResource(R.drawable.stage0);
                    else if (res==1)
                        graph.setImageResource(R.drawable.stage1);
                    else if (res==2)
                        graph.setImageResource(R.drawable.stage2);
                    else if (res==3)
                        graph.setImageResource(R.drawable.stage3);
                    else if (res==4)
                        graph.setImageResource(R.drawable.stage4);
                }
                catch (Exception e){
                    Log.e("-------------------","error");
                }
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
    }
}
