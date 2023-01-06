package com.example.citibiketracker;

import androidx.annotation.LongDef;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.citibiketracker.RecyclerView.MyAdapter;
import com.example.citibiketracker.RecyclerView.Station;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    NotificationManagerCompat notificationManagerCompat;
    Notification notification;
    Button btn_fetch;
    TextView textView;
    RecyclerView recyclerView;
    Timer timer = new Timer();
    ArrayList<Station> EbikeStations = new ArrayList<>();
    ArrayList<Station> FavoriteStations = new ArrayList<>();
    ToggleButton switchButton;
    Set<String> favoriteStationIds = new HashSet<>();
    private Set<String> savedFavoriteStationIds = new HashSet<>();
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String TEXT = "text";

    public ArrayList<Station> getFavoriteStations(ArrayList<Station> Stations) {
        ArrayList<Station> favStations = new ArrayList<>();
        for (Station s : Stations) {
            if (s.getFavorite()) {
                favStations.add(s);
            }
        }
        return favStations;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Enter station name:");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ArrayList<Station> filteredStations = new ArrayList<>();
                for (Station station : EbikeStations) {
                    if (station.getName().toLowerCase(Locale.ROOT).contains(newText.toLowerCase())) {
                        filteredStations.add(station);
                    }
                }
                recyclerView.setAdapter(new MyAdapter(getApplicationContext(), filteredStations));
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    public void handleResponseOfStationStatus(JSONObject response) {
        try {
            JSONObject data = response.getJSONObject("data");
            JSONArray stations = data.getJSONArray("stations");
            for (int i = 0; i < stations.length(); i++) {
                JSONObject station = stations.getJSONObject(i);
                String stationId = station.getString("station_id");
                int numBikesAvailable = station.getInt("num_bikes_available");
                int numEBikesAvailable = station.getInt("num_ebikes_available");
                String activity = station.getString("station_status");
                if (numBikesAvailable == numEBikesAvailable) {
                    EbikeStations.add(new Station(activity, stationId));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void handleResponseOfStationInfo(JSONObject response) {
        try {
            JSONObject data = response.getJSONObject("data");
            JSONArray stations = data.getJSONArray("stations");
            for (int i = 0; i < stations.length(); i++) {
                JSONObject station = stations.getJSONObject(i);
                String stationdId = station.getString("station_id");
                String stationName = station.getString("name");
                for (Station s : EbikeStations) {
                    if (s.getID().equals(stationdId)) {
                        s.setName(stationName);
                    }
                }

            }
            recyclerView.setAdapter(new MyAdapter(getApplicationContext(), EbikeStations));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void saveData() {
        FavoriteStations = getFavoriteStations(EbikeStations);
        for (Station station : FavoriteStations) {
            favoriteStationIds.add(station.getID());
        }
        Log.d("SaveData", favoriteStationIds.toString());
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(TEXT, favoriteStationIds);

        editor.apply();
        Toast.makeText(this, "Favorites Saved", Toast.LENGTH_SHORT).show();
    }

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        savedFavoriteStationIds = sharedPreferences.getStringSet(TEXT, new HashSet<>());
        Log.d("LoadData",savedFavoriteStationIds.toString());
    }

    public void updateFavStations() {
        for (String stationId : savedFavoriteStationIds) {
            for (Station station : EbikeStations) {
                if (station.getID().equals(stationId)) {
                    FavoriteStations.add(station);
                    break;
                }
            }
        }
        Log.d("FavSTationsUpdate", savedFavoriteStationIds.toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        loadData();
        updateFavStations();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        switchButton = findViewById(R.id.sw_favorites);
        btn_fetch = findViewById(R.id.btn_Fetch);
        recyclerView = findViewById(R.id.rv_listOfStations);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        String stationStatusUrl = "https://gbfs.citibikenyc.com/gbfs/en/station_status.json";
        String stationInformationUrl = "https://gbfs.citibikenyc.com/gbfs/en/station_information.json";
//NOTIFICATION CODE
        //Make requests on load

        NetworkUtils.makeRequest(MainActivity.this, stationStatusUrl, new NetworkUtils.OnResponseListener() {
            @Override
            public void onSuccess(JSONObject response) {
                handleResponseOfStationStatus(response);
            }

            @Override
            public void onError(VolleyError error) {
                Log.e("error", error.toString());
            }
        });
        NetworkUtils.makeRequest(MainActivity.this, stationInformationUrl, new NetworkUtils.OnResponseListener() {
            @Override
            public void onSuccess(JSONObject response) {
                handleResponseOfStationInfo(response);
            }

            @Override
            public void onError(VolleyError error) {
                Log.e("error", error.toString());
            }
        });
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override

            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    recyclerView.setAdapter(new MyAdapter(getApplicationContext(), getFavoriteStations(EbikeStations)));
                } else {
                    recyclerView.setAdapter(new MyAdapter(getApplicationContext(), EbikeStations));
                }
            }
        });

        btn_fetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                NetworkUtils.makeRequest(MainActivity.this, stationStatusUrl, new NetworkUtils.OnResponseListener() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        handleResponseOfStationInfo(response);
                    }

                    @Override
                    public void onError(VolleyError error) {
                        Log.e("VolleyError", error.toString());
                    }
                });
                NetworkUtils.makeRequest(MainActivity.this, stationInformationUrl, new NetworkUtils.OnResponseListener() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        handleResponseOfStationInfo(response);
                    }

                    @Override
                    public void onError(VolleyError error) {
                        Log.e("VolleyError", error.toString());
                    }
                });
                recyclerView.setAdapter(new MyAdapter(getApplicationContext(), EbikeStations));
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveData();
        // Convert the list of stations to a list of station IDs
    }
}