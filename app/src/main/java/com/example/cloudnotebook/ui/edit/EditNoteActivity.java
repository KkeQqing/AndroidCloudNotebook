package com.example.cloudnotebook.ui.edit;

import android.os.Bundle;
import android.os.Handler;
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

/**
 * 笔记编辑页面
 * 功能：新建笔记 / 编辑已有笔记
 * 包含：标题、内容、分类选择、自动保存、手动保存、同步上传
 */
public class EditNoteActivity extends BaseActivity {
    // ViewBinding 绑定布局，自动关联XML里的所有控件
    private ActivityEditBinding binding;

    // 笔记ViewModel，负责数据库操作、同步逻辑
    private NoteViewModel viewModel;

    // 当前正在编辑的笔记对象
    private Note currentNote;

    // 笔记ID，-1表示新建，大于0表示编辑
    private int noteId = -1;

    // 定时器相关，用于实现自动保存
    private Handler handler = new Handler();
    private Runnable autoSaveRunnable;

    // 标记：true = 新建笔记，false = 编辑旧笔记
    private boolean isNewNote = true;

    // SP工具类，获取当前登录的用户ID
    private SharedPrefsHelper prefsHelper;

    /**
     * 页面创建
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 绑定布局
        binding = ActivityEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 初始化ViewModel
        viewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        // 初始化SP工具类
        prefsHelper = new SharedPrefsHelper(this);

        // 获取从首页传递过来的笔记ID（编辑模式才会有）
        noteId = getIntent().getIntExtra("note_id", -1);

        // 如果ID != -1，说明是编辑模式，加载原有笔记数据
        if (noteId != -1) {
            isNewNote = false;

            // 子线程查询数据库（Room不允许主线程操作）
            Executors.newSingleThreadExecutor().execute(() -> {
                Note note = AppDatabase.getInstance(this).noteDao().getNoteByLocalId(noteId);

                // 切回主线程更新UI
                runOnUiThread(() -> {
                    if (note != null) {
                        currentNote = note;
                        // 给输入框赋值
                        binding.etTitle.setText(note.getTitle());
                        binding.etContent.setText(note.getContent());
                        // 自动选中对应的分类
                        setCategorySelection(note.getCategory());
                    }
                });
            });
        }

        // 保存按钮点击事件
        binding.btnSave.setOnClickListener(v -> saveNoteAndExit());

        // 自动保存：每3秒自动保存一次到本地
        autoSaveRunnable = () -> {
            saveLocal();
            handler.postDelayed(autoSaveRunnable, 3000);
        };
        handler.postDelayed(autoSaveRunnable, 3000);

        // 返回按钮，关闭当前页面
        binding.btnBack.setOnClickListener(v -> onBackPressed());
    }

    /**
     * 本地自动保存逻辑
     * 作用：实时保存内容，防止丢失
     */
    private void saveLocal() {
        // 获取输入框内容并去空格
        String title = binding.etTitle.getText().toString().trim();
        String content = binding.etContent.getText().toString().trim();
        String category = getSelectedCategory();

        // 标题和内容都为空，不保存
        if (title.isEmpty() && content.isEmpty()) return;

        // 获取真实的用户ID
        String realUserId = prefsHelper.getUserId();

        if (isNewNote) {
            // 新建笔记：创建Note对象，插入数据库 —— 这里用真实ID
            currentNote = new Note(title, content, category, realUserId);
            viewModel.insert(currentNote);
            isNewNote = false;
        } else {
            // 编辑笔记：更新已有内容
            currentNote.setTitle(title);
            currentNote.setContent(content);
            currentNote.setCategory(category);
            viewModel.update(currentNote);
        }
    }

    /**
     * 手动保存并退出
     * 停止自动保存 → 保存一次 → 同步 → 关闭页面
     */
    private void saveNoteAndExit() {
        // 停止自动保存
        handler.removeCallbacks(autoSaveRunnable);
        // 保存一次
        saveLocal();

        // 上传云端并提示
        if (currentNote != null) {
            viewModel.uploadNote(currentNote);
            Toast.makeText(this, "保存并同步成功", Toast.LENGTH_SHORT).show();
        }

        // 关闭页面
        finish();
    }

    /**
     * 获取当前选中的分类（工作/学习/生活/其他）
     */
    private String getSelectedCategory() {
        int id = binding.radioGroup.getCheckedRadioButtonId();
        if (id == R.id.radio_work) return "工作";
        else if (id == R.id.radio_study) return "学习";
        else if (id == R.id.radio_life) return "生活";
        else return "其他";
    }

    /**
     * 根据笔记的分类，自动勾选对应的单选按钮
     * 编辑笔记时回显分类用
     */
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

    /**
     * 页面销毁时
     * 停止自动保存，避免内存泄漏
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(autoSaveRunnable);

        // 退出前最后同步一次
        if (currentNote != null) {
            viewModel.uploadNote(currentNote);
        }
    }
}