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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.tasks.TasksScopes;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.Tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

public class ListOfTasksActivity extends Activity implements EasyPermissions.PermissionCallbacks {

    public static final String EXTRA_TASK_LIST_ID = "task_list_id";
    private static final String TAG = ListOfTasksActivity.class.getSimpleName();
    private GoogleAccountCredential mCredential;
    private UpdateTasks mUpdateTasks;
    private GetTasksTask mGetTasksTask;
    private ProgressDialog mProgress;

    private static final int REQUEST_ACCOUNT_PICKER = 1000;
    private static final int REQUEST_AUTHORIZATION = 1001;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String DEFAULT_PROGRESS_TEXT = "Obtaining tasks...";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { TasksScopes.TASKS };

    private RecyclerView mRecyclerView;
    private String taskListId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        taskListId = getIntent().getStringExtra(EXTRA_TASK_LIST_ID);
        Log.d(TAG, "taskListId=" + taskListId);

        setContentView(R.layout.activity_list_of_tasks);

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

        mCredential = getGoogleAccountCredential();
    }

    private GoogleAccountCredential getGoogleAccountCredential() {
        return GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
    }

    @Override
    protected void onPause() {
        cancelTask();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        executeGetTasksTask();
    }

    private void cancelTask() {
        if (mGetTasksTask != null) mGetTasksTask.cancel(true);
        if (mUpdateTasks != null) mUpdateTasks.cancel(true);
    }

    private void executeGetTasksTask() {
        if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            mGetTasksTask = new GetTasksTask(mCredential);
            mGetTasksTask.execute();
        }
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                executeGetTasksTask();
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
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == RESULT_OK)
                    executeGetTasksTask();
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
                        executeGetTasksTask();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK)
                    executeGetTasksTask();
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

    private class UpdateTasks extends AsyncTask<Void, String, Void> {
        private final boolean scrub;
        private com.google.api.services.tasks.Tasks mService = null;
        private Exception mLastError = null;
        private String msg = "There are no tasks to move!";
        private int taskCount = 0;

        UpdateTasks(GoogleAccountCredential credential, boolean scrub) {
            this.scrub = scrub;
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
        protected Void doInBackground(Void... params) {
            try {
                updateTasks();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            mProgress.setMessage(values[0]);
        }

        @Override
        protected void onPostExecute(Void output) {
            taskComplete();
        }

        @Override
        protected void onCancelled() {
            if (mLastError != null) {
                if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            ListOfTasksActivity.REQUEST_AUTHORIZATION);
                }
            }
            taskComplete();
        }

        private void updateTasks() throws IOException {
            Tasks tasks = getFirstPageOfAllTasksIncludingDeleted();
            do {
                for (Task task : tasks.getItems()) {
                    if (isCandidateForUpdating(task)) {

                        updateTask(createNew(task));

                        msg = "Number of tasks processed: " + ++taskCount;
                        Log.d(TAG, "updateTasks: " + msg);
                        publishProgress(msg);
                    } else {
                        Log.d(TAG, "skipping " + task.getTitle());
                    }

                    if (isCancelled())
                        return;

                }
                tasks = getAllTasksIncludingDeletedUsing(tasks.getNextPageToken());
            }
            while (!TextUtils.isEmpty(tasks.getNextPageToken()));
        }

        private boolean isCandidateForUpdating(Task task) {
            return task.getStatus().equalsIgnoreCase("completed") || isTaskDeleted(task);
        }

        private boolean isTaskDeleted(Task task) {
            Boolean deleted = (Boolean) task.get("deleted");
            return Objects.equals(deleted, Boolean.TRUE);
        }

        private Tasks getFirstPageOfAllTasksIncludingDeleted() throws IOException {
            return getAllTasksIncludingDeletedUsing(null);
        }

        private Tasks getAllTasksIncludingDeletedUsing(String pageToken) throws IOException {
            Log.d(TAG, "getAllTasksIncludingDeletedUsing() called with: pageToken = [" + pageToken + "]");
            com.google.api.services.tasks.Tasks.TasksOperations.List l =
                    mService.tasks()
                            .list(taskListId)
                            .setShowDeleted(true)
                            .setShowCompleted(true)
                            .setShowHidden(true);

            if (!TextUtils.isEmpty(pageToken))
                l.setPageToken(pageToken);
            Tasks tasks = l.execute();
            Log.d(TAG, "getAllTasksIncludingDeletedUsing: #tasks=" + tasks.getItems().size());
            return tasks;
        }

        private Task createNew(Task task) {
            Task t = new Task();
            t.setId(task.getId());

            if (scrub) {
                t.setTitle("scrubbed title");
                t.setNotes("scrubbed notes");
            }

            t.setDeleted(true);
            t.setStatus("needsAction");
            t.setDue(null);
            return t;
        }

        private void updateTask(Task t) throws IOException {
            mService.tasks().update(taskListId, t.getId(), t).execute();
        }

        private void taskComplete() {
            mProgress.hide();
            mProgress.setMessage(DEFAULT_PROGRESS_TEXT);
            disableKeepScreenOn();
        }
    }

    private class GetTasksTask extends AsyncTask<Object, Object, List<Task>> {
        private com.google.api.services.tasks.Tasks mService = null;
        private Exception mLastError = null;
        private final String msg = "There are no tasks to move!";

        GetTasksTask(GoogleAccountCredential credential) {
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
        protected List<Task> doInBackground(Object... params) {
            List<Task> tasks = null;
            try {
                tasks = getTasks();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
            }
            return tasks;
        }

        @Override
        protected void onPostExecute(List<Task> output) {
            RecyclerView.Adapter mAdapter = new TasksRecyclerViewAdapter(output);
            mRecyclerView.setAdapter(mAdapter);
            taskComplete();
        }

        @Override
        protected void onCancelled() {
            if (mLastError != null) {
                if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            ListOfTasksActivity.REQUEST_AUTHORIZATION);
                }
            }
            taskComplete();
        }

        private List<Task> getTasks() throws IOException {
            List<Task> t = new ArrayList<>();

            Tasks tasks = getFirstPageOfAllTasksIncludingDeleted();
            do {
                t.addAll(tasks.getItems());
                    publishProgress(msg);

                if (isCancelled())
                    return t;
                tasks = getAllTasksIncludingDeletedUsing(tasks.getNextPageToken());
            }
            while (!TextUtils.isEmpty(tasks.getNextPageToken()));

            return t;
        }


        private Tasks getFirstPageOfAllTasksIncludingDeleted() throws IOException {
            return getAllTasksIncludingDeletedUsing(null);
        }

        private Tasks getAllTasksIncludingDeletedUsing(String pageToken) throws IOException {
            com.google.api.services.tasks.Tasks.TasksOperations.List l =
                    mService.tasks()
                            .list(taskListId)
                            .setShowDeleted(true)
                            .setShowCompleted(true)
                            .setShowHidden(true);

            if (!TextUtils.isEmpty(pageToken))
                l.setPageToken(pageToken);
            return l.execute();
        }

        private void taskComplete() {
            mProgress.hide();
            mProgress.setMessage(DEFAULT_PROGRESS_TEXT);
            disableKeepScreenOn();
        }

    }

    public void onMoveCompetedToBin(View view) {
        Log.d(TAG, "onMoveCompetedToBin");
        updateTasks(false);
    }

    public void onMoveCompetedToBinAndScrub(View view) {
        Log.d(TAG, "onMoveCompetedToBinAndScrub");
        updateTasks(true);
    }

    private void updateTasks(boolean scrub) {
        mUpdateTasks = new UpdateTasks(mCredential, scrub);
        mUpdateTasks.execute();
    }

}