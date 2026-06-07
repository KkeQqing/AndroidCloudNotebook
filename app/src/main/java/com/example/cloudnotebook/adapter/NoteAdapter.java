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
import com.example.cloudnotebook.room.entity.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * 笔记列表适配器
 * 作用：将 Room 数据库中的 Note 数据，显示到 RecyclerView 列表
 * 功能：普通展示、多选删除、点击事件、【新增：分享笔记功能】
 */
public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {

    // 笔记数据源：列表要展示的所有笔记
    public List<Note> notes = new ArrayList<>();

    // 是否开启【多选模式】（用于批量删除）
    private boolean isMultiSelectMode = false;

    // 多选模式下，保存用户选中的笔记 ID
    private List<Integer> selectedIds = new ArrayList<>();

    // 列表点击事件监听器，将事件回调给 Activity/Fragment
    private OnItemClickListener listener;

    // ======================
    // 分享笔记功能（系统分享）
    // ======================
    /**
     * 调用系统分享，分享当前笔记的标题 + 内容
     * @param note 要分享的笔记
     * @param context 上下文（用于跳转分享界面）
     */
    private void shareNote(Note note, Context context) {
        // 创建分享意图
        Intent sendIntent = new Intent();
        // 设置动作为：发送文本
        sendIntent.setAction(Intent.ACTION_SEND);
        // 放入分享内容：标题 + 换行 + 内容
        sendIntent.putExtra(Intent.EXTRA_TEXT, note.getTitle() + "\n" + note.getContent());
        // 设置分享类型：纯文本
        sendIntent.setType("text/plain");

        // 创建系统分享选择器
        Intent chooser = Intent.createChooser(sendIntent, "分享笔记");
        // 必须添加：在非Activity环境启动界面需要NEW_TASK标记
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // 启动分享界面
        context.startActivity(chooser);
    }

    /**
     * 列表点击事件接口
     * 让 Activity / Fragment 实现，接收点击、长按事件
     */
    public interface OnItemClickListener {
        void onItemClick(Note note);  // 单击条目
        void onLongClick(int position); // 长按条目
    }

    /**
     * 构造方法：传入事件监听器
     */
    public NoteAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * 更新列表数据，并刷新列表
     */
    public void setNotes(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    /**
     * 开启/关闭多选模式
     * 关闭时自动清空选中列表
     */
    public void setMultiSelectMode(boolean mode) {
        if (!mode) selectedIds.clear();
        isMultiSelectMode = mode;
        notifyDataSetChanged();
    }

    /**
     * 创建列表条目（加载 item_note.xml 布局）
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new ViewHolder(view);
    }

    /**
     * 绑定数据：将笔记数据设置到控件上
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 获取当前位置的笔记
        Note note = notes.get(position);

        // 给控件赋值
        holder.title.setText(note.getTitle());
        holder.content.setText(note.getContent());
        holder.category.setText(note.getCategory());

        // ======================
        // 多选模式逻辑
        // ======================
        if (isMultiSelectMode) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(selectedIds.contains(note.getLocalId()));
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }

        // ======================
        // 条目点击事件
        // ======================
        holder.itemView.setOnClickListener(v -> {
            if (isMultiSelectMode) {
                // 多选模式：切换选中状态
                int id = note.getLocalId();
                if (selectedIds.contains(id)) {
                    selectedIds.remove(Integer.valueOf(id));
                } else {
                    selectedIds.add(id);
                }
                notifyItemChanged(position);
            } else {
                // 普通模式：回调点击事件
                listener.onItemClick(note);
            }
        });

        // ======================
        // 条目长按事件
        // ======================
        holder.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(position);
            return true;
        });

        // ======================
        // 分享按钮点击事件
        // 调用分享方法
        // ======================
        holder.btnShare.setOnClickListener(v -> {
            shareNote(note, v.getContext());
        });
    }

    /**
     * 返回列表总数量
     */
    @Override
    public int getItemCount() {
        return notes.size();
    }

    /**
     * ViewHolder 内部类：缓存条目控件，避免重复 findViewById
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, content, category;
        CheckBox checkBox;
        TextView btnShare; // 分享按钮

        public ViewHolder(View itemView) {
            super(itemView);
            // 绑定控件
            title = itemView.findViewById(R.id.tv_title);
            content = itemView.findViewById(R.id.tv_content);
            category = itemView.findViewById(R.id.tv_category);
            checkBox = itemView.findViewById(R.id.checkbox);
            btnShare = itemView.findViewById(R.id.btn_share); // 绑定分享按钮
        }
    }

    /**
     * 获取多选模式下，所有选中的笔记 ID
     */
    public List<Integer> getSelectedIds() {
        return selectedIds;
    }
}