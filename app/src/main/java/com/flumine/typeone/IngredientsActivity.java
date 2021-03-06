package com.flumine.typeone;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class IngredientsActivity extends BaseActivity {

    private int recordId;

    private ListView ingredientsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recordId = getIntent().getIntExtra("RECORD_ID", 0);
        setContentView(R.layout.activity_ingredients);
        ingredientsList = findViewById(R.id.list);
        ingredientsList.setOnItemClickListener((parent, view, position, id) -> {
            try {
                JSONObject ingredientUnit = (JSONObject) ingredientsList.getAdapter().getItem(position);
                JSONObject ingredient = ingredientUnit.getJSONObject("ingredient");
                Intent intent = new Intent(this, MealActivity.class);
                intent.putExtra("RECORD_ID", (int)recordId);
                intent.putExtra("INGREDIENT_UNIT_ID", (int)id);
                intent.putExtra("INGREDIENT_ID", ingredient.getInt("id"));
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            } catch (Exception e) {
                Log.e("REST", "Fail to forward to record details", e);
            }
        });
        getIngredients();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        recordId = intent.getIntExtra("RECORD_ID", -1);
        Log.d("REST", "New intent record id is " + intent.getIntExtra("RECORD_ID", -1));
        getIngredients();
    }

    private void getIngredients() {
        findViewById(R.id.timer).setVisibility(View.VISIBLE);
        findViewById(R.id.list).setVisibility(View.GONE);

        Log.d("REST", "Getting meals");
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(
            Request.Method.GET, BASE_URL.concat("/api/ingredients/"), null,
            response -> {
                try {
                    Log.d("REST", "Inredients retrieved");
                    JSONArray jarray = new JSONArray(response.toString());
                    ingredientsList.setAdapter(
                            new IngredientsActivity.JSONAdapter(IngredientsActivity.this, jarray));
                } catch (Exception e) {
                    Log.e("REST", e.getMessage());
                } finally {
                    findViewById(R.id.timer).setVisibility(View.GONE);
                    findViewById(R.id.list).setVisibility(View.VISIBLE);
                }
            },
            errorListener
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                String accessToken = getApplicationContext()
                        .getSharedPreferences("JWT", MODE_PRIVATE)
                        .getString("access", null);
                params.put("Authorization", "Bearer " + accessToken);
                return params;
            }
        };
        int socketTimeout = 300000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        Volley.newRequestQueue(IngredientsActivity.this).add(jsonObjectRequest);
    }

    public void cancel(View view) {
        Intent intent = new Intent(this, MealsActivity.class);
        intent.putExtra("RECORD_ID", recordId);
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        startActivity(intent);
    }

    class JSONAdapter extends BaseAdapter implements ListAdapter {

        JSONArray array;
        Activity activity;

        public JSONAdapter(Activity activity, JSONArray array) {
            this.array = array;
            this.activity = activity;
        }

        @Override
        public int getCount() {
            return array.length();
        }

        @Override
        public Object getItem(int position) {
            return array.optJSONObject(position);
        }

        @Override
        public long getItemId(int position) {
            JSONObject jsonObject = (JSONObject)getItem(position);
            return jsonObject.optLong("id");
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String title;
            try {
                JSONObject record = (JSONObject) getItem(position);
                JSONObject ingredient = record.getJSONObject("ingredient");
                String ingredientName = ingredient.getString("name");
                double breadUnitsPer100g = ingredient.getDouble("bread_units_per_100g");
                int gramsInUnit = record.getInt("grams_in_unit");
                String unitName = record.getJSONObject("unit").getString("name");
                String ingredientNameTranslated =  getStringResource(ingredientName);
                String unitNameTranslated =  getStringResource(unitName);
                title = String.format(Locale.getDefault(), "%s, %s",
                        ingredientNameTranslated, unitNameTranslated);
            } catch (Exception e) {
                title = "ERROR";
                Log.e("REST", "Fails to parse json", e);
            }
            TextView view = (TextView) activity.getLayoutInflater()
                    .inflate(R.layout.ingredient_item, null);
            view.setText(title);
            return view;
        }

    }
}