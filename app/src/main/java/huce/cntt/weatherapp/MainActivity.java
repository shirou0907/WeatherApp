package huce.cntt.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    ImageButton btnLocation, btnSearch, btnOption;
    AutoCompleteTextView txtLocal;
    TextView txtDate, txtCity, txtC, txtHumidity, txtCloud, txtWind, txtSunset, txtSunrise;
    ImageView imgMain;
    FusedLocationProviderClient fusedLocationProvinderClient;
    ProgressDialog dialog;
    String[] test = {"Thai Binh", "Ha Noi", "Hai Duong", "Ho Chi Minh City", "Hung Yen", "Thai Nguyen", "Da Lat"};
    List<String> listCity = new ArrayList<String>(Arrays.asList(test));
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        getWindow().setStatusBarColor(Color.parseColor("#000000"));
        init();
        Event();
        getCurrentWeather("Hanoi");
    }

    private void init() {
        btnLocation = findViewById(R.id.btnLocation);
        btnSearch = findViewById(R.id.btnSearch);
        btnOption = findViewById(R.id.btnOption);
        txtLocal = findViewById(R.id.txtLocal);
        txtLocal.setThreshold(1);
        txtLocal.setDropDownHeight(600);
        txtDate = findViewById(R.id.txtDate);
        txtC = findViewById(R.id.txtC);
        txtCity = findViewById(R.id.txtCity);
        txtHumidity = findViewById(R.id.txtHumidity);
        txtCloud = findViewById(R.id.txtCloud);
        txtWind = findViewById(R.id.txtWind);
        txtSunrise = findViewById(R.id.txtSunrise);
        txtSunset = findViewById(R.id.txtSunset);
        imgMain = findViewById(R.id.imgMain);
        fusedLocationProvinderClient = LocationServices.getFusedLocationProviderClient(this);
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading ... ");
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        setupUI(findViewById(R.id.activity_main));
    }

    public void autoCompleteText() {
        adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.select_dialog_item, listCity);
        txtLocal.setAdapter(adapter);
    }

    //set Event for button
    public void Event() {
        btnSearch.setOnClickListener(view -> {
            String city = txtLocal.getText().toString().trim();
            if (city.matches("")) {
                Toast.makeText(MainActivity.this, "Bạn chưa nhập tên thành phố, hãy thử lại!", Toast.LENGTH_SHORT).show();
            } else {
                dialog.show();
                getCurrentWeather(city);
            }
        });

        txtLocal.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Set<String> listWithoutDuplicates = new LinkedHashSet<String>(listCity);
                listCity.clear();
                listCity.addAll(listWithoutDuplicates);
                autoCompleteText();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 2) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getAllCity(txtLocal.getText().toString());
                        }
                    }, 700);
                }
            }
        });

        btnOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = txtLocal.getText().toString();
                Intent intent = new Intent(MainActivity.this, SubActivity.class);
                if(!city.matches("")) {
                    intent.putExtra("city", city);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(MainActivity.this, "Bạn chưa nhập tên thành phố, hãy thử lại!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Check permission
                if (ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    // When permission granted
                    getLocation();
                } else {
                    // When permission denied
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                }
            }
        });
    }

    //Get current location
    private void getLocation() {
        statusCheck();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProvinderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(
                                location.getLatitude(), location.getLongitude(), 1
                        );
                        txtLocal.setText(addresses.get(0).getAdminArea());
                        getCurrentWeather(txtLocal.getText().toString());
                        dialog.dismiss();
//                        Log.d("Ketqua", addresses.get(0).getAddressLine(0).toString());
                        Toast.makeText(MainActivity.this, addresses.get(0).getAddressLine(0).toString(),
                                Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }

    //Close keyboard when click outside
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if(inputMethodManager.isAcceptingText()){
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(),
                    0
            );
        }
    }
    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(MainActivity.this);
                    return false;
                }
            });
        }
        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    //API OpenWeatherMap
    public void getCurrentWeather(String city) {
        // Instantiate the RequestQueue.
        String myApiKey = "3b8ed2a9eb1a98ea5982544e82a394ad";
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://api.openweathermap.org/data/2.5/weather?q="+city+"&appid="+myApiKey;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String cityName = jsonObject.getString("name");
                            String country = jsonObject.getJSONObject("sys").getString("country");
                            txtCity.setText(cityName+", "+country);

                            String day = jsonObject.getString("dt");
                            Date date = new Date(Long.valueOf(day)*1000L);
                            String getDay = new SimpleDateFormat("EE, yyyy-MM-dd").format(date);
                            txtDate.setText(getDay);

                            JSONArray jsonArrayWeather = jsonObject.getJSONArray("weather");
                            JSONObject jsonObjectWeather = jsonArrayWeather.getJSONObject(0);
                            String mainWeather = jsonObjectWeather.getString("main");
                            String icon = jsonObjectWeather.getString("icon");
                            int drawableResourceId = MainActivity.this.getResources().getIdentifier("img"+icon, "drawable", MainActivity.this.getPackageName());
                            imgMain.setBackgroundResource(drawableResourceId);

                            JSONObject jsonObjectMain = jsonObject.getJSONObject("main");
                            float temp = Float.parseFloat(jsonObjectMain.getString("temp")) - 273;
                            txtC.setText(String.format("%.0f", temp)+"°C - "+mainWeather);

                            int humidity = jsonObjectMain.getInt("humidity");
                            txtHumidity.setText(humidity + "%");

                            float windSpeed = Float.parseFloat(jsonObject.getJSONObject("wind").getString("speed"));
                            txtWind.setText(String.format("%.1f", windSpeed)+" m/s");

                            float cloud = Float.parseFloat(jsonObject.getJSONObject("clouds").getString("all"));
                            txtCloud.setText(String.format("%.0f", cloud)+"%");

                            String sunrise = jsonObject.getJSONObject("sys").getString("sunrise");
                            Date date1 = new Date(Long.valueOf(sunrise)*1000L);
                            String getDay1 = new SimpleDateFormat("HH:mm").format(date1);
                            txtSunrise.setText(getDay1);

                            String sunset = jsonObject.getJSONObject("sys").getString("sunset");
                            Date date2 = new Date(Long.valueOf(sunset)*1000L);
                            String getDay2 = new SimpleDateFormat("HH:mm").format(date2);
                            txtSunset.setText(getDay2);

                            dialog.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error == null || error.networkResponse == null) {
                            return;
                        }
                        try {
                            Toast.makeText(MainActivity.this, new String(error.networkResponse.data,"UTF-8"), Toast.LENGTH_SHORT).show();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
        });

    // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    //API AccuWeather
    public void getAllCity(String text) {
        // Instantiate the RequestQueue.
        String myApiKey = "3jpV1z0s7GJQhFGewNhs5Im3tDuEV56M";
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://dataservice.accuweather.com/locations/v1/cities/autocomplete?apikey="+myApiKey+"&q="+text;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            for(int i=0; i< jsonArray.length(); i++) {
                                listCity.add(jsonArray.getJSONObject(i).getString("LocalizedName"));
                                Log.d("Reslut:", jsonArray.getJSONObject(i).getString("LocalizedName"));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    //API GeoCity
    public void getAllCity2(String text) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://geodb-free-service.wirefreethought.com/v1/geo/cities?limit=5&offset=0&namePrefix="+text;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                            for(int i=1; i<jsonArray.length(); i++) {
                                Log.d("Result", jsonArray.getJSONObject(i).getString("city"));
                                listCity.add(jsonArray.getJSONObject(i).getString("city"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    //Check Open GPS
    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
        else {
            dialog.show();
        }
    }
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

}