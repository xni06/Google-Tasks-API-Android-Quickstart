package com.example.quickstart;

import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.TaskLists;

import java.io.IOException;

class GetTasksListsTask extends AsyncTask<Object, Object, TaskLists> {
    private Contract.Presenter presenter;
    private Tasks service = null;
    private Exception exception;

    GetTasksListsTask(PresenterImpl presenter, GoogleAccountCredential credential) {
        this.presenter = presenter;
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        service = new Tasks.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("Google Tasks API Android Quickstart")
                .build();
    }

    @Override
    protected void onPreExecute() {
        presenter.onPreExecute();
    }

    @Override
    protected TaskLists doInBackground(Object... params) {
        TaskLists taskLists = null;
        try {
            taskLists = service.tasklists().list().execute();
        } catch (IOException e) {
            exception = e;
            cancel(true);
        }
        return taskLists;
    }

    @Override
    protected void onPostExecute(TaskLists output) {
        presenter.onPostExecute(output);
    }

    @Override
    protected void onCancelled() {
        presenter.onCancelled(exception);
    }

}
