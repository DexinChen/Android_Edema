package com.example.dexin.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Comparator;

import cz.msebera.android.httpclient.Header;

class Pair {
    String time;
    String predict;
    public Pair(String time, String predict) {
        this.time = time;
        this.predict = predict;
    }
}

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

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
                    for(int i=0;i<info.length;i++) {
                        Log.e("------------------",info[i].predict);
                    }
                    GraphView graph = (GraphView) findViewById(R.id.graph);
                    int num = info.length;
                    DataPoint[] points = new DataPoint[num];
                    TextView tv = (TextView) findViewById(R.id.time);
                    String content = "Time:"+"\n";
                    for(int i=0;i<num;i++) {
                        points[i] = new DataPoint(i,Integer.parseInt(info[i].predict));
                        content+=(String.valueOf(i)+": "+info[i].time+"\n");
                    }
                    tv.setText(content);
                    LineGraphSeries<DataPoint> series = new LineGraphSeries<>(points);
                    graph.getViewport().setYAxisBoundsManual(true);
                    graph.getViewport().setMinY(0);
                    graph.getViewport().setMaxY(5);
                    series.setColor(Color.BLACK);

                    graph.addSeries(series);
                    graph.getGridLabelRenderer().setGridColor(Color.BLACK);
                    graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.BLACK);
                    graph.getGridLabelRenderer().setVerticalLabelsColor(Color.BLACK);
                    //graph.setBackgroundColor(Color.BLACK);
                    graph.setTitleColor(Color.BLACK);

                    StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
                    String[] xLables = new String[num];
                    for(int i=0;i<num;i++) {
                        String t = info[i].time;
                        String m_d = String.valueOf(i);
                        xLables[i] = m_d;
                    }

                    staticLabelsFormatter.setHorizontalLabels(xLables);
                    graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
                }
                catch (Exception e){
                    Log.e("-------------------",pre);
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


// you can directly pass Date objects to DataPoint-Constructor
// this will convert the Date to double via Date#getTime()

    }
}
