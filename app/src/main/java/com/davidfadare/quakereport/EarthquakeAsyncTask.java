package com.davidfadare.quakereport;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

class EarthquakeAsyncTask extends AsyncTaskLoader<List<Earthquake>> {

    private String[] urls;

    EarthquakeAsyncTask(Context context, String... url) {
        super(context);
        urls = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<Earthquake> loadInBackground() {
        if (urls.length < 1 || urls[0] == null) {
            return null;
        }

        List<Earthquake> result = QueryUtils.fetchEarthquakeData(urls[0]);
        return result;
    }

}
