package com.example.quickstart;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.api.services.tasks.model.Task;

import java.util.List;

class TasksRecyclerViewAdapter extends RecyclerView.Adapter<TasksRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private List<Task> mDataSet;

    interface OnClickListener {
        void onClick(String id);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView note;

        ViewHolder(View v, final OnClickListener listener) {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
            note = (TextView) v.findViewById(R.id.note);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(String.valueOf(getAdapterPosition()));
                }
            });
        }

        TextView getTitle() {
            return title;
        }

        TextView getNote() {
            return note;
        }

    }

    TasksRecyclerViewAdapter(Context context, List<Task> output) {
        this.context = context;
        mDataSet = output;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_of_tasks_item, viewGroup, false);
        OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(String id) {
                Task task = mDataSet.get(Integer.parseInt(id));
                ((ListOfTasksActivity) context).onTaskSelected(task.getId());
            }
        };
        return new ViewHolder(v, onClickListener);
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
