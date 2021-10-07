/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.davidfadare.quakereport;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class EarthquakeActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<List<Earthquake>> {

    public static final String LOG_TAG = EarthquakeActivity.class.getName();

    private ArrayAdapter<Earthquake> adapter;

    private ListView earthquakeListView;

    private TextView textView;

    private ProgressBar progressView;

    private static final String USGS_REQUEST_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);

        adapter = new EarthquakeAdapter(this, new ArrayList<Earthquake>());

        textView = (TextView) findViewById(R.id.empty_view);
        earthquakeListView = (ListView) findViewById(R.id.list);
        progressView = (ProgressBar) findViewById(R.id.progress_view);

        earthquakeListView.setAdapter(adapter);

        earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Earthquake currentEarthquake = adapter.getItem(i);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri earthquakeUri = Uri.parse(currentEarthquake.getUrl());

                // Create a new intent to view the earthquake URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, earthquakeUri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });

        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            getLoaderManager().initLoader(0, null, this);
        } else {
            textView.setText("No internet connection.");

            progressView.setVisibility(View.GONE);

            earthquakeListView.setEmptyView(textView);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            ConnectivityManager cm =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();

            if (isConnected) {
                getLoaderManager().restartLoader(0, null, this);
            } else {
                textView.setText("No internet connection.");

                progressView.setVisibility(View.GONE);

                earthquakeListView.setEmptyView(textView);
            }
        }
    }

    @Override
    public Loader<List<Earthquake>> onCreateLoader(int i, Bundle bundle) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        String minMagnitude = sharedPrefs.getString(
                getString(R.string.settings_min_magnitude_key),
                getString(R.string.settings_min_magnitude_default));

        String radius = sharedPrefs.getString(
                getString(R.string.settings_radius_key),
                getString(R.string.settings_radius_default));

        String startDate = sharedPrefs.getString(
                getString(R.string.settings_date_key),
                getString(R.string.settings_date_default));

        String location = sharedPrefs.getString(
                getString(R.string.settings_locations_key),
                getString(R.string.settings_locations_default));

        Uri baseUri = Uri.parse(USGS_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("format", "geojson");
        uriBuilder.appendQueryParameter("orderby", "time");
        uriBuilder.appendQueryParameter("limit", "30");

        uriBuilder.appendQueryParameter("starttime", startDate);

        uriBuilder.appendQueryParameter("minmag", minMagnitude);
        uriBuilder.appendQueryParameter("maxradiuskm", radius);

        String latitude;
        String longitude;
        switch (location) {
            case "Nigeria":
                latitude = "9";
                longitude = "9";
                break;
            case "Egypt":
                latitude = "27";
                longitude = "31";
                break;
            case "South Africa":
                latitude = "30";
                longitude = "23";
                break;
            default:
                latitude = "9";
                longitude = "20";
        }

        uriBuilder.appendQueryParameter("latitude", latitude);
        uriBuilder.appendQueryParameter("longitude", longitude);

        return new EarthquakeAsyncTask(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Earthquake>> loader, List<Earthquake> earthquakes) {
        adapter.clear();

        if (earthquakes != null && !earthquakes.isEmpty()) {
            adapter.addAll(earthquakes);
        }

        textView.setText("No earthquakes found or bad internet connection.");

        progressView.setVisibility(View.GONE);

        earthquakeListView.setEmptyView(textView);
    }

    @Override
    public void onLoaderReset(Loader<List<Earthquake>> loader) {
        adapter.clear();
        loader.reset();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivityForResult(settingsIntent, 1);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
