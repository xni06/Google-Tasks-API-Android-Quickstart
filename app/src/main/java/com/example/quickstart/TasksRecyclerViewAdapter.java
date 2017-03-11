package com.example.quickstart;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.api.services.tasks.model.Task;

import java.util.List;

public class TasksRecyclerViewAdapter extends RecyclerView.Adapter<TasksRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = TasksRecyclerViewAdapter.class.getSimpleName();

    private Context context;
    private List<Task> mDataSet;

    public TasksRecyclerViewAdapter(Context context, List<Task> output) {
        this.context = context;
        mDataSet = output;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final TextView textView2;

        public ViewHolder(View v, final OnClickListener listener) {
            super(v);
            textView = (TextView) v.findViewById(R.id.textView);
            textView2 = (TextView) v.findViewById(R.id.textView2);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(String.valueOf(getAdapterPosition()));
                }
            });
        }

        public TextView getTextView() {
            return textView;
        }
        public TextView getTextView2() {
            return textView2;
        }
    }

    interface OnClickListener {
        void onClick(String id);
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
        Log.d(TAG, "Element " + position + " set.");
        viewHolder.getTextView().setText(mDataSet.get(position).getTitle());
        viewHolder.getTextView2().setText(mDataSet.get(position).getNotes());
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}
