package com.example.quickstart;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.tasks.model.TaskLists;

interface Contract {
    interface View {

        void showProgress();

        void setAdapter(RecyclerView.Adapter mAdapter);

        void hideProgress();

        void disableKeepScreenOn();

        void enableKeepScreenOn();
    }

    interface Presenter {
        int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

        GoogleAccountCredential getGoogleAccountCredential();

        boolean isGooglePlayServicesAvailable();

        void showGooglePlayServicesAvailabilityErrorDialog(int connectionStatusCode);

        void acquireGooglePlayServices();

        void onActivityResult(int requestCode, int resultCode, Intent intent);

        void chooseAccount();

        void cancelTask();

        void onPause();

        void onResume();

        void taskComplete();

        void onPreExecute();

        void onPostExecute(TaskLists taskLists);

        void onCancelled(Exception exception);
    }
}
