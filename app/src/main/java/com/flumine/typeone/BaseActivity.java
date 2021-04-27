package com.flumine.typeone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseActivity extends AppCompatActivity {

    protected static final String BASE_URL = "http://192.168.0.191:8000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
    }

    protected Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            if (error instanceof AuthFailureError) {
                try {
                    refreshToken();
                } catch (Exception e) {
                    Log.e("REST", e.getLocalizedMessage());
                }
            }
        }
    };

    private void refreshToken() throws Exception {
        SharedPreferences pref =
                getApplicationContext().getSharedPreferences("JWT", MODE_PRIVATE);
        JSONObject object = new JSONObject();
        object.put("refresh", pref.getString("refresh", null));
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, BASE_URL.concat("/api/token/refresh/"), object,
                response -> {
                    try {
                        String newAccessToken = response.getString("access");
                        Log.d("REST", "New access token retrieved " + newAccessToken);
                        pref.edit().putString("access", newAccessToken).apply();
                    } catch (JSONException e) {
                        Log.e("REST", "Error " + e.getMessage());
                    }
                },
                error -> {
                    if (error instanceof AuthFailureError) {
                        Log.e("REST", "Failed to refresh token " + error.getMessage());
                        getNewTokens();
                    }
                }
        );
        Volley.newRequestQueue(getApplicationContext()).add(jsonObjectRequest);
    }

    private void getNewTokens() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

}