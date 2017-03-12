package com.example.quickstart;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import static com.example.quickstart.Contract.Presenter.REQUEST_PERMISSION_GET_ACCOUNTS;

public class ListOfTaskListsActivity extends Activity implements EasyPermissions.PermissionCallbacks, Contract.View, OnItemSelected {

    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    private Contract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_list_of_task_lists);
        presenter = new PresenterImpl(this, this, getAsyncTask());
        progressDialog = new MyProgressDialog(this, presenter, "Obtaining task lists...");
        initRecyclerView();
    }

    @NonNull
    private AsyncTask getAsyncTask() {
        return new GetTasksListsTask();
    }

    private void initRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, linearLayoutManager.getOrientation()));
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onResume();
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        presenter.chooseAccount();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        presenter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void disableKeepScreenOn() {
        getWindow().clearFlags(FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void enableKeepScreenOn() {
        getWindow().addFlags(FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onItemSelected(String id) {
        Intent intent = new Intent(this, ListOfTasksActivity.class);
        intent.putExtra(ListOfTasksActivity.EXTRA_TASK_LIST_ID, id);
        startActivity(intent);
    }

    @Override
    public void showProgress() {
        progressDialog.show();
    }

    @Override
    public void setAdapter(RecyclerView.Adapter adapter) {
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void hideProgress() {
        progressDialog.hide();
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

}