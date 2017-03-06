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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

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

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

public class TaskListActivity extends Activity
        implements EasyPermissions.PermissionCallbacks {
    public static final String EXTRA_TASK_LIST_ID = "task_list_id";
    private static final String TAG = TaskListActivity.class.getSimpleName();
//    private static final String EXTRA_TASK_LIST_ID = "@default";
//    private static final String EXTRA_TASK_LIST_ID = "MTQwNTcwNjU5NDk3NjE4NDI0ODE6MTQ2NDM4MDcxODow";

    // MTQwNTcwNjU5NDk3NjE4NDI0ODE6MDow - default
    // MTQwNTcwNjU5NDk3NjE4NDI0ODE6MTQ2NDM4MDcxODow - test
    GoogleAccountCredential mCredential;
    private TextView mOutputText;
    private Button mCallApiButton;
//    private MakeRequestTask mMakeRequestTask;
    private GetTasksTask mMakeRequestTask;
    ProgressDialog mProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String BUTTON_TEXT = "Scrub all tasks";
    private static final String DEFAULT_PROGRESS_TEXT = "Scrubbing...";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { TasksScopes.TASKS };


    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private String[] myDataset = {"one", "two", "three", "four"};
    private String taskListId;

    /**
     * Create the main activity.
     * @param savedInstanceState previously saved instance data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        taskListId = getIntent().getStringExtra(EXTRA_TASK_LIST_ID);
        Log.d(TAG, "taskListId=" + taskListId);

////////////////
        setContentView(R.layout.activity_main3);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage(DEFAULT_PROGRESS_TEXT);
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                cancelTask();
            }
        });

        mCredential = GoogleAccountCredential.usingOAuth2(
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
        getResultsFromApi();
    }

    private void cancelTask() {
        if (mMakeRequestTask != null) mMakeRequestTask.cancel(true);
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            mOutputText.setText("No network connection available.");
        } else {
            mMakeRequestTask = new GetTasksTask(mCredential);
            mMakeRequestTask.execute();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
//                    mOutputText.setText(
//                            "This app requires Google Play Services. Please install " +
//                                    "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private void enableKeepScreenOn() {
        getWindow().addFlags(FLAG_KEEP_SCREEN_ON);
    }

    private void disableKeepScreenOn() {
        getWindow().clearFlags(FLAG_KEEP_SCREEN_ON);
    }

    private class MakeRequestTask extends AsyncTask<Void, String, Void> {
        private com.google.api.services.tasks.Tasks mService = null;
        private Exception mLastError = null;
        private String msg = "There are no tasks to move!";
        private int taskCount = 0;

        MakeRequestTask(GoogleAccountCredential credential) {
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
//            mOutputText.setText("");
            mProgress.show();
        }

        /**
         * Background task to call Google Tasks API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected Void doInBackground(Void... params) {
            try {
                scrubTasks();
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
//            mOutputText.setText(msg);
            taskComplete();
        }

        @Override
        protected void onCancelled() {
            if (mLastError != null) {
                if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            TaskListActivity.REQUEST_AUTHORIZATION);
                } else {
//                    mOutputText.setText("The following error occurred:\n"
//                            + mLastError.getMessage() + "\n" + msg);
                }
            } else {
//                mOutputText.setText("Request cancelled\n" + msg);
            }
            taskComplete();
        }

        private void scrubTasks() throws IOException {
            Tasks tasks = getFirstPageOfAllTasksIncludingDeleted();
            do {
                for (Task task : tasks.getItems()) {
                    task = scrub(task);
                    updateTask(task);

                    msg = "Number of tasks scrubbed: " + ++taskCount;
                    publishProgress(msg);

                    if (isCancelled())
                        return;

                }
                tasks = getAllTasksIncludingDeletedUsing(tasks.getNextPageToken());
            }
            while (!TextUtils.isEmpty(tasks.getNextPageToken()));
        }


        private Tasks getFirstPageOfAllTasksIncludingDeleted() throws IOException {
            return getAllTasksIncludingDeletedUsing(null);
        }

        private Tasks getAllTasksIncludingDeletedUsing(String pageToken) throws IOException {
//            String taskListId = getIntent().getStringExtra(EXTRA_TASK_LIST_ID);
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

        private Task scrub(Task task) {
            Task t = new Task();
            t.setId(task.getId());
            t.setTitle("scrubbed title");
            t.setNotes("scrubbed notes");
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
        private String msg = "There are no tasks to move!";
        private int taskCount = 0;

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
//            mOutputText.setText("");
            mProgress.show();
        }

        /**
         * Background task to call Google Tasks API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<Task> doInBackground(Object... params) {
            List<Task> tasks = null;
            try {
                tasks = scrubTasks();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
            }
            return tasks;
        }

//        @Override
//        protected void onProgressUpdate(Object... values) {
//            mProgress.setMessage(values[0]);
//        }

        @Override
        protected void onPostExecute(List<Task> output) {
//            mOutputText.setText(msg);
//            List<TaskList> items = output.getItems();
            mAdapter =  new TasksRecyclerViewAdapter(TaskListActivity.this, output);
            mRecyclerView.setAdapter(mAdapter);
            taskComplete();
        }

        @Override
        protected void onCancelled() {
            if (mLastError != null) {
                if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            TaskListActivity.REQUEST_AUTHORIZATION);
                } else {
//                    mOutputText.setText("The following error occurred:\n"
//                            + mLastError.getMessage() + "\n" + msg);
                }
            } else {
//                mOutputText.setText("Request cancelled\n" + msg);
            }
            taskComplete();
        }

        private List<Task> scrubTasks() throws IOException {
            com.google.api.services.tasks.Tasks.Tasklists.List list = mService.tasklists().list();

            List<Task> t = new ArrayList<>();

            Tasks tasks = getFirstPageOfAllTasksIncludingDeleted();
            do {
                t.addAll(tasks.getItems());
//                for (Task task : tasks.getItems()) {
//                    task = scrub(task);
//                    updateTask(task);
//
//                    msg = "Number of tasks scrubbed: " + ++taskCount;
                    publishProgress(msg);

                    if (isCancelled())
                        return t;

//                }
                tasks = getAllTasksIncludingDeletedUsing(tasks.getNextPageToken());
            }
            while (!TextUtils.isEmpty(tasks.getNextPageToken()));

            return t;
        }


        private Tasks getFirstPageOfAllTasksIncludingDeleted() throws IOException {
            return getAllTasksIncludingDeletedUsing(null);
        }

        private Tasks getAllTasksIncludingDeletedUsing(String pageToken) throws IOException {
//            String taskListId = getIntent().getStringExtra(EXTRA_TASK_LIST_ID);
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

        private Task scrub(Task task) {
            Task t = new Task();
            t.setId(task.getId());
            t.setTitle("scrubbed title");
            t.setNotes("scrubbed notes");
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

}