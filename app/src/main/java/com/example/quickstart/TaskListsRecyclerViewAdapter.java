package com.example.quickstart;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;

class TaskListsRecyclerViewAdapter extends RecyclerView.Adapter<TaskListsRecyclerViewAdapter.ViewHolder> {

    private final Context context;
    private final TaskLists mDataSet;

    interface OnClickListener {
        void onClick(String id);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;

        ViewHolder(View v, final OnClickListener listener) {
            super(v);
            title = (TextView) v.findViewById(android.R.id.text1);
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

    }

    TaskListsRecyclerViewAdapter(Context context, TaskLists output) {
        this.context = context;
        mDataSet = output;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(android.R.layout.simple_list_item_1, viewGroup, false);
        OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(String id) {
                TaskList taskList = mDataSet.getItems().get(Integer.parseInt(id));
                ((OnItemSelected) context).onItemSelected(taskList.getId());
            }
        };
        return new ViewHolder(v, onClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.getTitle().setText(mDataSet.getItems().get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return mDataSet.getItems().size();
    }
}
