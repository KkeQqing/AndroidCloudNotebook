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

/**
 * 笔记列表适配器
 * 作用：将 Room 数据库中的 Note 数据绑定到 RecyclerView 列表项
 * 支持：普通点击、长按多选、分享、主题切换
 */
public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {

    // 笔记数据集合
    public List<Note> notes = new ArrayList<>();

    // 是否处于多选删除模式
    private boolean isMultiSelectMode = false;

    // 存放被选中的笔记本地 ID（用于批量删除）
    private List<Integer> selectedIds = new ArrayList<>();

    // 列表项点击/长按事件回调接口
    private OnItemClickListener listener;

    /**
     * 列表项点击事件回调接口
     */
    public interface OnItemClickListener {
        // 短点击：进入编辑页面
        void onItemClick(Note note);

        // 长点击：进入多选模式
        void onLongClick(int position);
    }

    /**
     * 构造方法：传入点击事件监听
     */
    public NoteAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * 更新列表数据并刷新页面
     */
    public void setNotes(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    /**
     * 设置是否开启多选模式
     * 关闭时自动清空选中记录
     */
    public void setMultiSelectMode(boolean mode) {
        if (!mode) selectedIds.clear();
        isMultiSelectMode = mode;
        notifyDataSetChanged();
    }

    /**
     * 创建列表项视图（加载 item_note 布局）
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new ViewHolder(view);
    }

    /**
     * 绑定数据到列表项
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 获取当前位置的笔记
        Note note = notes.get(position);

        // 给控件设置数据
        holder.title.setText(note.getTitle());
        holder.content.setText(note.getContent());
        holder.category.setText(note.getCategory());

        // ===================== 主题颜色适配 =====================
        Context ctx = holder.itemView.getContext();
        if (ctx instanceof BaseActivity) {
            BaseActivity base = (BaseActivity) ctx;
            holder.cardView.setCardBackgroundColor(base.themeCardColor);
            holder.category.setTextColor(base.themeMainColor);
        }

        // ===================== 多选模式显示/隐藏 CheckBox =====================
        if (isMultiSelectMode) {
            holder.checkBox.setVisibility(View.VISIBLE);
            // 判断当前笔记是否被选中
            holder.checkBox.setChecked(selectedIds.contains(note.getLocalId()));
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }

        // ===================== 列表项点击事件 =====================
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
                // 普通模式：点击进入编辑
                listener.onItemClick(note);
            }
        });

        // ===================== 列表项长按事件 =====================
        holder.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(position);
            return true;
        });

        // ===================== 分享按钮点击 =====================
        holder.btnShare.setOnClickListener(v -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, note.getTitle() + "\n" + note.getContent());
            sendIntent.setType("text/plain");

            // 打开系统分享选择器
            Intent chooser = Intent.createChooser(sendIntent, "分享笔记");
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            v.getContext().startActivity(chooser);
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
     * 列表项控件缓存类（减少 findViewById 次数）
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        androidx.cardview.widget.CardView cardView;
        TextView title, content, category;
        CheckBox checkBox;
        TextView btnShare;

        public ViewHolder(View itemView) {
            super(itemView);
            // 绑定布局中的控件
            cardView = itemView.findViewById(R.id.card_note);
            title = itemView.findViewById(R.id.tv_title);
            content = itemView.findViewById(R.id.tv_content);
            category = itemView.findViewById(R.id.tv_category);
            checkBox = itemView.findViewById(R.id.checkbox);
            btnShare = itemView.findViewById(R.id.btn_share);
        }
    }

    /**
     * 获取所有被选中的笔记 ID（用于批量删除）
     */
    public List<Integer> getSelectedIds() {
        return selectedIds;
    }
}