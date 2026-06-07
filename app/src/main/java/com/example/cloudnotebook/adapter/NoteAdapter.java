package com.example.cloudnotebook.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cloudnotebook.R;
import com.example.cloudnotebook.base.BaseActivity;
import com.example.cloudnotebook.room.entity.Note;
import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {

    public List<Note> notes = new ArrayList<>();
    private boolean isMultiSelectMode = false;
    private List<Integer> selectedIds = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Note note);
        void onLongClick(int position);
    }

    public NoteAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    public void setMultiSelectMode(boolean mode) {
        if (!mode) selectedIds.clear();
        isMultiSelectMode = mode;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Note note = notes.get(position);

        holder.title.setText(note.getTitle());
        holder.content.setText(note.getContent());
        holder.category.setText(note.getCategory());

        // 主题
        Context ctx = holder.itemView.getContext();
        if (ctx instanceof BaseActivity) {
            BaseActivity base = (BaseActivity) ctx;
            holder.cardView.setCardBackgroundColor(base.themeCardColor);
            holder.category.setTextColor(base.themeMainColor);
        }

        if (isMultiSelectMode) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(selectedIds.contains(note.getLocalId()));
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (isMultiSelectMode) {
                int id = note.getLocalId();
                if (selectedIds.contains(id)) {
                    selectedIds.remove(Integer.valueOf(id));
                } else {
                    selectedIds.add(id);
                }
                notifyItemChanged(position);
            } else {
                listener.onItemClick(note);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(position);
            return true;
        });

        holder.btnShare.setOnClickListener(v -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, note.getTitle() + "\n" + note.getContent());
            sendIntent.setType("text/plain");
            Intent chooser = Intent.createChooser(sendIntent, "分享笔记");
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            v.getContext().startActivity(chooser);
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        androidx.cardview.widget.CardView cardView;
        TextView title, content, category;
        CheckBox checkBox;
        TextView btnShare;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_note);
            title = itemView.findViewById(R.id.tv_title);
            content = itemView.findViewById(R.id.tv_content);
            category = itemView.findViewById(R.id.tv_category);
            checkBox = itemView.findViewById(R.id.checkbox);
            btnShare = itemView.findViewById(R.id.btn_share);
        }
    }

    public List<Integer> getSelectedIds() {
        return selectedIds;
    }
}