package com.talmir.mickinet.helpers;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.talmir.mickinet.R;

import java.util.ArrayList;

public class MimeTypeAdapter extends RecyclerView.Adapter<MimeTypeAdapter.MimeTypeViewHolder> {

    private ArrayList<String> mimeTypeList;

    class MimeTypeViewHolder extends RecyclerView.ViewHolder {
        private TextView holder_item_name, holder_item_extension, holder_item_id;

        MimeTypeViewHolder(View itemView) {
            super(itemView);
            holder_item_name = (TextView) itemView.findViewById(R.id.name);
            holder_item_extension = (TextView) itemView.findViewById(R.id.extension);
            holder_item_id = (TextView) itemView.findViewById(R.id.id);
        }
    }

    public MimeTypeAdapter(ArrayList<String> mimeTypeList) {
        this.mimeTypeList = mimeTypeList;
    }

    @Override
    public MimeTypeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout_mime_types, parent, false);
        return new MimeTypeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MimeTypeViewHolder holder, int position) {
        String currentItem = mimeTypeList.get(position);
        String name = currentItem.substring(currentItem.indexOf(' '), currentItem.lastIndexOf(' '));
        String extension = currentItem.substring(currentItem.indexOf('(') + 1, currentItem.lastIndexOf(')'));
        String id = currentItem.substring(0, currentItem.indexOf(' '));
        holder.holder_item_name.setText(name);
        holder.holder_item_extension.setText(extension);
        holder.holder_item_id.setText(id);
    }

    @Override
    public int getItemCount() {
        return mimeTypeList.size();
    }
}
