package com.example.quickstart;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

interface Contract {
    interface View {

    }
    interface Presenter {
        int REQUEST_GOOGLE_PLAY_SERVICES = 1002;

        GoogleAccountCredential getGoogleAccountCredential();

        boolean isGooglePlayServicesAvailable();

        void showGooglePlayServicesAvailabilityErrorDialog(int connectionStatusCode);

        void acquireGooglePlayServices();
    }
}
