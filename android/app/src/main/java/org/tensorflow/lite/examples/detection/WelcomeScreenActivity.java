package org.tensorflow.lite.examples.detection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.tensorflow.lite.examples.detection.tflite.SimilarityClassifier;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;

import okhttp3.ConnectionSpec;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http2.Header;

public class WelcomeScreenActivity extends AppCompatActivity  implements
        View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);
        TextView userName = findViewById(R.id.user_name_txtview);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String userNameStr = extras.getString("user");
            userName.setText("Hi, "+userNameStr +"!");
        }

        Button button = (Button)findViewById(R.id.updateServer);
        // Register the onClick listener with the implementation above
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Log.d("TEST", "TEST click in welcome screen");
        switch (v.getId()) {
            case R.id.updateServer:
                try {
                    Log.d("TEST"," before starting thread ");
                    updateServer();
                    Log.d("TEST"," after starting thread");
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
    private void updateServer() throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("TEST"," before running server data ");
                    ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS).build();
                    OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
                    OkHttpClient client = clientBuilder.build();

                    Request.Builder requestBuilder = new Request.Builder().url("https://users-node-server.herokuapp.com/faces");

                    SharedPreferences sharedPreferences = getSharedPreferences("HashMap", MODE_PRIVATE);
                    String defValue = new Gson().toJson(new HashMap<String, SimilarityClassifier.Recognition>());
                    String content=sharedPreferences.getString("map",defValue);
                    //String content = "{\"name\":\"test\",\"version\":\"2\"}";
                    RequestBody requestBody = RequestBody.create(MediaType.parse("application/json,charset=utf-8"),content);

                    requestBuilder.method("POST",requestBody);
                    requestBuilder.addHeader("Content-Type","application/json");

                    Log.d("TEST","1.TEST POST request body as jsonString \n "+content);
                    //Log.d("TEST","2.TEST POST request body as jsonString \n "+requestBody);
                    Request request = requestBuilder.build();
                    Response response = client.newCall(request).execute();
                    String responseString = response.body().string();
                    Log.d("TEST","update server data "+responseString);

                    Gson gson = new Gson();
                    Type type = new TypeToken<HashMap<String, SimilarityClassifier.Recognition>>(){}.getType();
                    HashMap<String, SimilarityClassifier.Recognition> registeredFaces = gson.fromJson(content, type);

                    Gson gson1 = new Gson();
                    String jsonString = gson1.toJson(registeredFaces);

                    Type type1 = new TypeToken<HashMap<String, SimilarityClassifier.Recognition>>(){}.getType();
                    HashMap<String, SimilarityClassifier.Recognition> clonedMap = gson.fromJson(jsonString, type1);
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