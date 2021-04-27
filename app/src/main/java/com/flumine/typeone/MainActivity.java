package com.flumine.typeone;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        SharedPreferences preferences = getApplicationContext()
                .getSharedPreferences("JWT", MODE_PRIVATE);
        if (preferences.getString("access", null) == null) {
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(i);
        } else {
            Intent i = new Intent(this, RecordsActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(i);
        }
    }

    public void connect(View view) throws Exception {

        String url = "http://192.168.0.191:8000/api/token/";

        JSONObject object = new JSONObject();
        object.put("username", "admin");
        object.put("password", "admin");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
            Request.Method.POST, url, object,
            response -> {
                try {
                    SharedPreferences pref =
                            getApplicationContext().getSharedPreferences("JWT", MODE_PRIVATE);
                    pref.edit().putString("access", response.getString("access")).apply();
                    pref.edit().putString("refresh", response.getString("refresh")).apply();
                } catch (Exception e) {
                    Log.e("REST", e.getMessage());
                }
            },
            error -> ((TextView)findViewById(R.id.text))
                    .setText(String.format("Error getting response: %s", error.toString()))
        );
        Volley.newRequestQueue(getApplicationContext()).add(jsonObjectRequest);

    }

}
