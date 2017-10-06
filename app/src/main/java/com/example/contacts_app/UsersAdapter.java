package com.example.contacts_app;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.contacts_app.Result;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    private List<Result> resultList;

    public UsersAdapter(List<Result> resultList){ this.resultList = resultList; }

    public void updateDataSet(List<Result> resultList){
        this.resultList = resultList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View userView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent, false);
        return new ViewHolder(userView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Result result = resultList.get(position);
        holder.userNameTV.setText(buildName(result));
    }

    @Override
    public int getItemCount() {
        return resultList.size();
    }

    private String buildName(Result result){
        StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append(result.getName().getFirst()).append(" ").append(result.getName().getLast());
        return nameBuilder.toString();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView userNameTV;
        public ViewHolder (View itemView){
            super(itemView);
            userNameTV = itemView.findViewById(R.id.tv_name_cardView);
        }
    }
}
