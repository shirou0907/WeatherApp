package huce.cntt.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import huce.cntt.weatherapp.Adapter.WeatherAdapter;
import huce.cntt.weatherapp.Models.Weather;

public class SubActivity extends AppCompatActivity {
    TextView txtCity;
    ListView listView;
    WeatherAdapter weatherAdapter;
    ArrayList<Weather> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);
        getSupportActionBar().hide();
        getWindow().setStatusBarColor(Color.parseColor("#000000"));
        init();
        Intent intent = getIntent();
        String city = intent.getStringExtra("city");
        get5Day3h(city);
    }

    private void init() {
        txtCity = findViewById(R.id.txtCity);
        listView = findViewById(R.id.listview);
        arrayList = new ArrayList<Weather>();
        weatherAdapter = new WeatherAdapter(SubActivity.this, arrayList);
        listView.setAdapter(weatherAdapter);
    }

    //API OpenWeatherMap
    public void get5Day3h(String city) {
        // Instantiate the RequestQueue.
        String myApiKey = "3b8ed2a9eb1a98ea5982544e82a394ad";
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://api.openweathermap.org/data/2.5/forecast?q="+city+"&appid="+myApiKey;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONArray("list");
                            String name = jsonObject.getJSONObject("city").getString("name");
                            txtCity.setText(name);
                            for(int i=0; i< jsonArray.length(); i++) {
                                Log.d("Ketqua: ", jsonArray.getJSONObject(i).getString("dt_txt"));
                                float minTemp = Float.parseFloat(jsonArray.getJSONObject(i).getJSONObject("main").getString("temp_min")) - 273;
                                float maxTemp = Float.parseFloat(jsonArray.getJSONObject(i).getJSONObject("main").getString("temp_max")) - 273;
                                String date = jsonArray.getJSONObject(i).getString("dt_txt");
                                String status = jsonArray.getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("description");
                                String icon = jsonArray.getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("icon");
//                              Log.d("Data", String.format("%.1f", minTemp) + String.format("%.1f", maxTemp) + date + status + icon);
                                arrayList.add(new Weather(date, status, icon, String.format("%.1f", maxTemp), String.format("%.1f", minTemp)));
                            }
                            weatherAdapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(SubActivity.this, "Có lỗi xảy ra, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                    }
                });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}