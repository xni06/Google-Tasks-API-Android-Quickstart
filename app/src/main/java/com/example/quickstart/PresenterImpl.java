package com.example.quickstart;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.TasksScopes;
import com.google.api.services.tasks.model.TaskLists;

import java.util.Arrays;

import pub.devrel.easypermissions.EasyPermissions;

import static android.app.Activity.RESULT_OK;
import static pub.devrel.easypermissions.EasyPermissions.hasPermissions;

class PresenterImpl implements Contract.Presenter {

    private static final int REQUEST_AUTHORIZATION = 1001;
    private static final int REQUEST_ACCOUNT_PICKER = 1000;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private final Activity activity;
    private final GoogleAccountCredential credential;
    private Contract.View view;
    private InjectableAsyncTasks asyncTask;

    PresenterImpl(Activity activity, Contract.View view, InjectableAsyncTasks asyncTask) {
        this.activity = activity;
        this.view = view;
        this.asyncTask = asyncTask;
        credential = getGoogleAccountCredential();
        asyncTask.setPresenter(this);
        asyncTask.setTasksService(getTasksService());
    }

    @Override
    public GoogleAccountCredential getGoogleAccountCredential() {
        final String[] scopes = {TasksScopes.TASKS};
        return GoogleAccountCredential
                .usingOAuth2(activity, Arrays.asList(scopes))
                .setBackOff(new ExponentialBackOff());
    }

    @Override
    public boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(activity);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void executeAsyncTask() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (credential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            ((AsyncTask)asyncTask).execute();
        }
    }

    @Override
    public void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(activity);
        if (apiAvailability.isUserResolvableError(connectionStatusCode))
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
    }

    @Override
    public void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                activity,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    @Override
    public void chooseAccount() {
        if (hasPermissions(activity, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = activity.getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                credential.setSelectedAccountName(accountName);
                executeAsyncTask();//TODO inject so that we can reuse the presenter
            } else {
                activity.startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            }
        } else {
            EasyPermissions.requestPermissions(
                    activity,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == RESULT_OK)
                    executeAsyncTask(); //TODO inject so that we can reuse the presenter
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && intent != null && intent.getExtras() != null) {
                    String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = activity.getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        credential.setSelectedAccountName(accountName);
                        executeAsyncTask(); //TODO inject so that we can reuse the presenter
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK)
                    executeAsyncTask(); //TODO inject so that we can reuse the presenter
                break;
        }
    }

    @Override
    public void onResume() {
        executeAsyncTask();//TODO inject so that we can reuse the presenter
    }

    @Override
    public void onPause() {
        cancelTask();
    }

    @Override
    public void cancelTask() {
        if (asyncTask != null) ((AsyncTask) asyncTask).cancel(true);
    }

    @Override
    public void onPreExecute() {
        view.enableKeepScreenOn();
        view.showProgress();
    }

    @Override
    public void onPostExecute(TaskLists taskLists) {
        RecyclerView.Adapter mAdapter = new TaskListsRecyclerViewAdapter(activity, taskLists);//TODO inject so that we can reuse the presenter
        view.setAdapter(mAdapter);
        taskComplete();
    }

    @Override
    public void taskComplete() {
        view.hideProgress();
        view.disableKeepScreenOn();
    }

    @Override
    public void onCancelled(Exception exception) {
        if (exception != null) {
            if (exception instanceof GooglePlayServicesAvailabilityIOException) {
                showGooglePlayServicesAvailabilityErrorDialog(
                        ((GooglePlayServicesAvailabilityIOException) exception)
                                .getConnectionStatusCode());
            } else if (exception instanceof UserRecoverableAuthIOException) {
                activity.startActivityForResult(
                        ((UserRecoverableAuthIOException) exception).getIntent(),
                        REQUEST_AUTHORIZATION);
            }
        }
        taskComplete();
    }

    private com.google.api.services.tasks.Tasks getTasksService() {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        return new Tasks.Builder(
                transport, jsonFactory, this.credential)
                .setApplicationName("Google Tasks API Android Quickstart")
                .build();
    }

}
