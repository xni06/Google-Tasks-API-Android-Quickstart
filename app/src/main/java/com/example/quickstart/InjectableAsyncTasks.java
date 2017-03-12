package com.example.quickstart;

import com.google.api.services.tasks.Tasks;

interface InjectableAsyncTasks {
    void setTasksService(Tasks tasksService);
    void setPresenter(Contract.Presenter presenter);
}
