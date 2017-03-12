package com.example.quickstart;

import android.os.AsyncTask;

import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.TaskLists;

import java.io.IOException;

class GetTasksListsTask extends AsyncTask<Object, Object, TaskLists> implements InjectableAsyncTasks {
    private Contract.Presenter presenter;
    private Exception exception;
    private Tasks tasksService;

    GetTasksListsTask() {
    }

    @Override
    public void setTasksService(Tasks tasksService) {
        this.tasksService = tasksService;
    }

    @Override
    public void setPresenter(Contract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    protected void onPreExecute() {
        presenter.onPreExecute();
    }

    @Override
    protected TaskLists doInBackground(Object... params) {
        TaskLists taskLists = null;
        try {
            taskLists = tasksService.tasklists().list().execute();
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
