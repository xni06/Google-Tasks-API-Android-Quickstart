package com.example.quickstart;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.tasks.model.TaskLists;

import java.io.IOException;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import static pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks;
import static pub.devrel.easypermissions.EasyPermissions.hasPermissions;

public class ListOfTaskListsActivity extends Activity implements PermissionCallbacks, Contract.View {

    private GoogleAccountCredential mCredential;
    private GetTasksListsTask mMakeRequestTask;
    private ProgressDialog mProgress;

    private static final int REQUEST_ACCOUNT_PICKER = 1000;
    private static final int REQUEST_AUTHORIZATION = 1001;
    private static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String DEFAULT_PROGRESS_TEXT = "Obtaining task lists...";
    private static final String PREF_ACCOUNT_NAME = "accountName";

    private RecyclerView mRecyclerView;
    private Contract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_list_of_task_lists);

        presenter = new PresenterImpl(this, this);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, linearLayoutManager.getOrientation()));

        mProgress = new ProgressDialog(this);
        mProgress.setMessage(DEFAULT_PROGRESS_TEXT);
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                cancelTask();
            }
        });

        mCredential = presenter.getGoogleAccountCredential();
    }

    @Override
    protected void onPause() {
        cancelTask();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        executeGetTasksListsTask();
    }

    private void cancelTask() {
        if (mMakeRequestTask != null) mMakeRequestTask.cancel(true);
    }

    private void executeGetTasksListsTask() {
        if (!presenter.isGooglePlayServicesAvailable()) {
            presenter.acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            mMakeRequestTask = new GetTasksListsTask(mCredential);
            mMakeRequestTask.execute();
        }
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                executeGetTasksListsTask();
            } else {
                startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            }
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Contract.Presenter.REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == RESULT_OK)
                    executeGetTasksListsTask();
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        executeGetTasksListsTask();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK)
                    executeGetTasksListsTask();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
    }

    private void enableKeepScreenOn() {
        getWindow().addFlags(FLAG_KEEP_SCREEN_ON);
    }

    private void disableKeepScreenOn() {
        getWindow().clearFlags(FLAG_KEEP_SCREEN_ON);
    }

    public void onTaskListSelected(String id) {
        Intent intent = new Intent(this, ListOfTasksActivity.class);
        intent.putExtra(ListOfTasksActivity.EXTRA_TASK_LIST_ID, id);
        startActivity(intent);
    }

    private class GetTasksListsTask extends AsyncTask<Object, Object, TaskLists> {
        private com.google.api.services.tasks.Tasks mService = null;
        private Exception mLastError = null;

        GetTasksListsTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.tasks.Tasks.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Tasks API Android Quickstart")
                    .build();
        }

        @Override
        protected void onPreExecute() {
            enableKeepScreenOn();
            mProgress.show();
        }

        @Override
        protected TaskLists doInBackground(Object... params) {
            try {
                return mService.tasklists().list().execute();
            } catch (IOException e) {
                mLastError = e;
                cancel(true);
            }
            return null;
        }

        @Override
        protected void onPostExecute(TaskLists output) {
            RecyclerView.Adapter mAdapter = new TaskListsRecyclerViewAdapter(ListOfTaskListsActivity.this, output);
            mRecyclerView.setAdapter(mAdapter);
            taskComplete();
        }

        @Override
        protected void onCancelled() {
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    presenter.showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            ListOfTaskListsActivity.REQUEST_AUTHORIZATION);
                }
            }
            taskComplete();
        }

        private void taskComplete() {
            mProgress.hide();
            mProgress.setMessage(DEFAULT_PROGRESS_TEXT);
            disableKeepScreenOn();
        }

    }
}