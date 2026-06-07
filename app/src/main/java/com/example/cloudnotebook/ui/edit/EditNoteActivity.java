package com.example.cloudnotebook.ui.edit;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.cloudnotebook.R;
import com.example.cloudnotebook.base.BaseActivity;
import com.example.cloudnotebook.databinding.ActivityEditBinding;
import com.example.cloudnotebook.room.database.AppDatabase;
import com.example.cloudnotebook.room.entity.Note;
import com.example.cloudnotebook.viewmodel.NoteViewModel;
import com.example.cloudnotebook.utils.SharedPrefsHelper;

import java.util.concurrent.Executors;

public class EditNoteActivity extends BaseActivity {
    private ActivityEditBinding binding;
    private NoteViewModel viewModel;
    private Note currentNote;
    private int noteId = -1;

    // 使用主线程Handler，配合严格生命周期管理
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable autoSaveRunnable;

    // 标记：编辑页固定为false，绝不允许新建分支
    private boolean isNewNote = true;
    private SharedPrefsHelper prefsHelper;

    // 记录上一次保存的内容，用来判断是否有修改（核心防无效保存）
    private String lastTitle = "";
    private String lastContent = "";
    private String lastCategory = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        prefsHelper = new SharedPrefsHelper(this);
        noteId = getIntent().getIntExtra("note_id", -1);

        binding.btnSave.setOnClickListener(v -> saveNoteAndExit());
        binding.btnBack.setOnClickListener(v -> onBackPressed());

        // ========== 区分新建 / 编辑 ==========
        if (noteId != -1) {
            // 编辑模式：强制锁定为旧笔记，禁止走insert
            isNewNote = false;
            loadOldNoteData();
        } else {
            // 新建模式
            startAutoSaveTask();
        }
    }

    /**
     * 加载已有笔记数据
     */
    private void loadOldNoteData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Note note = AppDatabase.getInstance(this).noteDao().getNoteByLocalId(noteId);
            runOnUiThread(() -> {
                if (note != null) {
                    currentNote = note;
                    // 回显数据
                    binding.etTitle.setText(note.getTitle());
                    binding.etContent.setText(note.getContent());
                    setCategorySelection(note.getCategory());

                    // 初始化「上次保存值」，用于对比内容是否变化
                    lastTitle = note.getTitle();
                    lastContent = note.getContent();
                    lastCategory = note.getCategory();
                }
                // 数据加载完成后再启动自动保存
                startAutoSaveTask();
            });
        });
    }

    /**
     * 启动自动保存：启动前先清空旧任务，防止多任务叠加
     */
    private void startAutoSaveTask() {
        // 先移除已有任务，避免多个定时器并发
        handler.removeCallbacks(autoSaveRunnable);

        autoSaveRunnable = new Runnable() {
            @Override
            public void run() {
                saveLocal();
                // 循环执行
                handler.postDelayed(this, 3000);
            }
        };
        // 延迟3秒执行第一次自动保存
        handler.postDelayed(autoSaveRunnable, 3000);
    }

    /**
     * 本地保存核心逻辑
     * 增加：内容无变化 → 直接返回，不更新、不重置同步状态
     */
    private void saveLocal() {
        String title = binding.etTitle.getText().toString().trim();
        String content = binding.etContent.getText().toString().trim();
        String category = getSelectedCategory();

        // 1. 标题+内容全空，不保存
        if (title.isEmpty() && content.isEmpty()) {
            return;
        }

        // 2. 内容和上一次完全一致 → 无修改，直接退出，不执行更新
        if (title.equals(lastTitle)
                && content.equals(lastContent)
                && category.equals(lastCategory)) {
            return;
        }

        // 3. 更新缓存的上次内容
        lastTitle = title;
        lastContent = content;
        lastCategory = category;

        String realUserId = prefsHelper.getUserId();

        if (isNewNote) {
            // 新建笔记分支
            currentNote = new Note(title, content, category, realUserId);
            viewModel.insert(currentNote);
            isNewNote = false;
        } else {
            if (currentNote == null) return;

            // 强制重新赋值，绝对使用界面最新文字（防被清空）
            currentNote.setTitle(title);
            currentNote.setContent(content);  // 强保证正文不为空
            currentNote.setCategory(category);
            viewModel.update(currentNote);
        }
    }

    /**
     * 手动保存并退出
     */
    private void saveNoteAndExit() {
        // 第一步：停止自动保存
        handler.removeCallbacks(autoSaveRunnable);
        // 第二步：执行最后一次本地保存
        saveLocal();

        // 第三步：仅上传一次（唯一一次云端同步）
        if (currentNote != null) {
            viewModel.uploadNote(currentNote);
            Toast.makeText(this, "保存并同步成功", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private String getSelectedCategory() {
        int id = binding.radioGroup.getCheckedRadioButtonId();
        if (id == R.id.radio_work) return "工作";
        else if (id == R.id.radio_study) return "学习";
        else if (id == R.id.radio_life) return "生活";
        else return "其他";
    }

    private void setCategorySelection(String category) {
        switch (category) {
            case "工作":
                binding.radioGroup.check(R.id.radio_work);
                break;
            case "学习":
                binding.radioGroup.check(R.id.radio_study);
                break;
            case "生活":
                binding.radioGroup.check(R.id.radio_life);
                break;
            default:
                binding.radioGroup.check(R.id.radio_other);
                break;
        }
    }

    // 页面不可见时，停止定时任务（防后台跑任务）
    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(autoSaveRunnable);
    }

    // 页面销毁：彻底清空任务，【删除多余的上传逻辑】
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 只停定时器，不再额外上传！！关键修复
        handler.removeCallbacks(autoSaveRunnable);
        autoSaveRunnable = null;
    }
}