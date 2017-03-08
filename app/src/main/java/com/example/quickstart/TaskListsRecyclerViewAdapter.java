package com.example.quickstart;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.api.services.tasks.model.TaskLists;

public class TaskListsRecyclerViewAdapter extends RecyclerView.Adapter<TaskListsRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = TaskListsRecyclerViewAdapter.class.getSimpleName();

    private Context context;
    private TaskLists mDataSet;

    public TaskListsRecyclerViewAdapter(Context context, TaskLists output) {
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
                    Log.d(TAG, "Element " + getAdapterPosition() + " clicked.");
                    String msg = textView.getText().toString() + " " + textView2.getText().toString();
                    Log.d(TAG, msg);
                    listener.onClick(textView2.getText().toString());
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
                .inflate(R.layout.task_list_item, viewGroup, false);
        OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(String id) {
                ((MainActivity) context).onTaskListSelected(id);
            }
        };
        return new ViewHolder(v, onClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Log.d(TAG, "Element " + position + " set.");
        viewHolder.getTextView().setText(mDataSet.getItems().get(position).getTitle());
        viewHolder.getTextView2().setText(mDataSet.getItems().get(position).getId());
    }

    @Override
    public int getItemCount() {
        return mDataSet.getItems().size();
    }
}