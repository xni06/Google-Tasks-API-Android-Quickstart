package com.example.quickstart;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.api.services.tasks.model.Task;

import java.util.List;

class TasksRecyclerViewAdapter extends RecyclerView.Adapter<TasksRecyclerViewAdapter.ViewHolder> {

    private List<Task> mDataSet;

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView note;

        ViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
            note = (TextView) v.findViewById(R.id.note);
        }

        TextView getTitle() {
            return title;
        }

        TextView getNote() {
            return note;
        }

    }

    TasksRecyclerViewAdapter(List<Task> output) {
        mDataSet = output;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_of_tasks_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.getTitle().setText(mDataSet.get(position).getTitle());
        viewHolder.getNote().setText(mDataSet.get(position).getNotes());
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}
