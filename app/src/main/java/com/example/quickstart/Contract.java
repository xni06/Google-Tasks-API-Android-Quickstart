package com.example.quickstart;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

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

        void onActivity(int requestCode, int resultCode, Intent data);

        void chooseAccount();

        void cancelTask();

        void executeGetTasksListsTask();

        void onPause();

        void onResume();
    }
}
