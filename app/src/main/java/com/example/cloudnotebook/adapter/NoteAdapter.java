package com.example.cloudnotebook.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cloudnotebook.R;
import com.example.cloudnotebook.room.entity.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * 笔记列表适配器
 * 作用：将 Note 数据列表 显示在 RecyclerView 上
 * 支持：普通点击、长按、多选批量操作
 */
public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {

    // 笔记数据源（列表要显示的所有笔记）
    private List<Note> notes = new ArrayList<>();

    // 是否开启多选模式（批量删除用）
    private boolean isMultiSelectMode = false;

    // 多选模式下，保存用户选中的笔记ID
    private List<Integer> selectedIds = new ArrayList<>();

    // 点击事件回调接口，把事件传给Activity/Fragment
    private OnItemClickListener listener;

    /**
     * 列表点击事件接口
     * 页面实现后可收到：单击、长按事件
     */
    public interface OnItemClickListener {
        void onItemClick(Note note);  // 单击条目
        void onLongClick(int position); // 长按条目
    }

    /**
     * 构造方法：传入点击监听器
     */
    public NoteAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * 更新列表数据
     */
    public void setNotes(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged(); // 刷新列表
    }

    /**
     * 设置是否开启多选模式
     * 关闭时自动清空选中项
     */
    public void setMultiSelectMode(boolean mode) {
        if (!mode) selectedIds.clear();
        isMultiSelectMode = mode;
        notifyDataSetChanged();
    }

    /**
     * 创建条目视图（加载item_note布局）
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new ViewHolder(view);
    }

    /**
     * 绑定数据到条目（给控件赋值）
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 获取当前位置的笔记
        Note note = notes.get(position);

        // 给控件设置内容
        holder.title.setText(note.getTitle());
        holder.content.setText(note.getContent());
        holder.category.setText(note.getCategory());

        // 多选模式：显示CheckBox，并设置选中状态
        if (isMultiSelectMode) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(selectedIds.contains(note.getLocalId()));
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }

        // 条目点击事件
        holder.itemView.setOnClickListener(v -> {
            if (isMultiSelectMode) {
                // 多选模式：切换选中状态
                int id = note.getLocalId();
                if (selectedIds.contains(id)) {
                    selectedIds.remove(Integer.valueOf(id));
                } else {
                    selectedIds.add(id);
                }
                notifyItemChanged(position); // 刷新当前条目
            } else {
                // 普通模式：回调点击事件
                listener.onItemClick(note);
            }
        });

        // 条目长按事件
        holder.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(position);
            return true;
        });
    }

    /**
     * 返回列表总条数
     */
    @Override
    public int getItemCount() {
        return notes.size();
    }

    /**
     * 内部类：缓存条目控件（避免重复findViewById）
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, content, category;
        CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);
            // 绑定控件
            title = itemView.findViewById(R.id.tv_title);
            content = itemView.findViewById(R.id.tv_content);
            category = itemView.findViewById(R.id.tv_category);
            checkBox = itemView.findViewById(R.id.checkbox);
        }
    }

    /**
     * 获取当前选中的所有笔记ID列表
     * @return 选中的笔记ID集合
     */
    public List<Integer> getSelectedIds() {
        return selectedIds;
    }
}