package org.tensorflow.lite.examples.detection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.tensorflow.lite.examples.detection.tflite.SimilarityClassifier;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = (Button)findViewById(R.id.checkIn);
        // Register the onClick listener with the implementation above
        button.setOnClickListener(this);

        Button syncFromServerBtn = (Button)findViewById(R.id.syncFromServer);
        // Register the onClick listener with the implementation above
        syncFromServerBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Log.d("TEST", "TEST");
        switch (v.getId()) {
            case R.id.checkIn:
                Toast.makeText(getApplicationContext(), "This is a message displayed in a Toast", Toast.LENGTH_SHORT).show();
                Intent activity2Intent = new Intent(getApplicationContext(), DetectorActivity.class);
                startActivity(activity2Intent);
//                Toast toast = Toast.makeText(getApplicationContext(),
//                        "This is a message displayed in a Toast",
//                        Toast.LENGTH_SHORT);
//                toast.show();
                break;
            case R.id.syncFromServer:
                try {
                    Log.d("TEST"," before starting thread ");
                    loadFromServer();
                    Log.d("TEST"," after starting thread");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void loadFromServer() throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("TEST"," before running server data ");
                    ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS).build();
                    OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
                    OkHttpClient client = clientBuilder.build();

                    Request.Builder requestBuilder = new Request.Builder().url("https://users-node-server.herokuapp.com/faces");

                    Request request = requestBuilder.build();
                    Response response = client.newCall(request).execute();
                    String responseString = response.body().string();
                    Log.d("TEST","server data "+responseString);


                    Gson gson = new Gson();
                    String jsonString = gson.toJson(responseString);
                    Type type1 = new TypeToken<HashMap<String, SimilarityClassifier.Recognition>>(){}.getType();
                    HashMap<String, SimilarityClassifier.Recognition> clonedMap = gson.fromJson(responseString, type1);
                    String jsonString1 = new Gson().toJson(clonedMap);

                    SharedPreferences sharedPreferences1 = getSharedPreferences("HashMap", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences1.edit();
                    editor.putString("map", jsonString1);
                    //System.out.println("Input josn"+jsonString.toString());
                    editor.apply();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}