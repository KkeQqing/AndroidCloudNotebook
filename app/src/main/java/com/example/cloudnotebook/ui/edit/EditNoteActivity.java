package com.example.cloudnotebook.ui.edit;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.cloudnotebook.R;
import com.example.cloudnotebook.base.BaseActivity;
import com.example.cloudnotebook.databinding.ActivityEditBinding;
import com.example.cloudnotebook.repository.NoteRepository;
import com.example.cloudnotebook.room.entity.Note;
import com.example.cloudnotebook.utils.SharedPrefsHelper;

/**
 * 笔记编辑 / 新增页面
 * 功能：
 * 1. 新建笔记
 * 2. 修改已有笔记
 * 3. 选择分类（工作 / 学习 / 生活 / 其他）
 * 4. 保存到本地 + 自动同步云端
 * 5. 全局主题适配
 */
public class EditNoteActivity extends BaseActivity {

    // 视图绑定：替代 findViewById
    private ActivityEditBinding binding;

    // 数据仓库：负责本地 + 云端数据操作
    private NoteRepository repository;

    // 笔记本地ID，-1 表示新建笔记，大于0表示编辑笔记
    private int localId = -1;

    // 当前登录用户ID
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 加载视图绑定
        binding = ActivityEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 获取当前登录用户ID
        userId = new SharedPrefsHelper(this).getUserId();

        // 初始化数据仓库
        repository = new NoteRepository(this, userId);

        // ====================== 主题颜色适配 ======================
        // 设置卡片背景色
        binding.cardTopBar.setCardBackgroundColor(themeCardColor);
        binding.cardTitle.setCardBackgroundColor(themeCardColor);
        binding.cardCategory.setCardBackgroundColor(themeCardColor);
        binding.cardContent.setCardBackgroundColor(themeCardColor);

        // 设置保存按钮背景色
        binding.btnSave.setBackgroundTintList(ColorStateList.valueOf(themeMainColor));

        // ====================== 按钮点击事件 ======================
        // 返回按钮：关闭当前页面
        binding.btnBack.setOnClickListener(v -> finish());

        // 保存按钮：执行保存逻辑
        binding.btnSave.setOnClickListener(v -> saveNote());

        // 获取从列表页传递过来的笔记ID（用于编辑）
        localId = getIntent().getIntExtra("note_id", -1);
    }

    /**
     * 保存笔记（核心方法）
     * 逻辑：
     * 1. 获取输入框内容
     * 2. 校验标题不能为空
     * 3. 获取选中的分类
     * 4. 封装成 Note 对象
     * 5. 判断是【新增】还是【更新】
     * 6. 保存本地 + 同步云端
     */
    private void saveNote() {
        // 获取输入框内容并去空格
        String title = binding.etTitle.getText().toString().trim();
        String content = binding.etContent.getText().toString().trim();

        // 标题非空校验
        if (title.isEmpty()) {
            Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取选中的分类（工作 / 学习 / 生活 / 其他）
        int radioId = binding.radioGroup.getCheckedRadioButtonId();
        String category = "工作";
        if (radioId == R.id.radio_work) category = "工作";
        else if (radioId == R.id.radio_study) category = "学习";
        else if (radioId == R.id.radio_life) category = "生活";
        else if (radioId == R.id.radio_other) category = "其他";

        // 构建笔记对象
        Note note = new Note();
        note.setTitle(title);             // 标题
        note.setContent(content);         // 内容
        note.setCategory(category);       // 分类
        note.setUserId(userId);           // 所属用户
        note.setSync(false);              // 未同步（保存后自动上传）
        note.setDeleted(false);           // 未删除
        note.setCreateTime(System.currentTimeMillis());  // 创建时间
        note.setUpdateTime(System.currentTimeMillis());  // 更新时间

        // 如果是编辑模式，设置本地ID
        if (localId != -1) {
            note.setLocalId(localId);
        }

        // ====================== 新增笔记 ======================
        if (localId == -1) {
            repository.insertLocal(note, () -> {
                // 本地保存成功后，上传云端
                repository.uploadNote(note, new NoteRepository.OnCloudCallback() {
                    @Override
                    public void onSuccess() {
                        // 云端同步成功，切主线程提示
                        runOnUiThread(() -> {
                            Toast.makeText(EditNoteActivity.this, "保存并同步成功", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        // 同步失败，提示用户
                        runOnUiThread(() -> {
                            Toast.makeText(EditNoteActivity.this, "保存成功，但同步失败：" + error, Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }
                });
            });
        }
        // ====================== 更新笔记 ======================
        else {
            repository.updateLocal(note, () -> {
                // 本地更新成功后，上传云端
                repository.uploadNote(note, new NoteRepository.OnCloudCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            Toast.makeText(EditNoteActivity.this, "保存并同步成功", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(EditNoteActivity.this, "保存成功，但同步失败：" + error, Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }
                });
            });
        }
    }
}