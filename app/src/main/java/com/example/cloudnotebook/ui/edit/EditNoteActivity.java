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

public class EditNoteActivity extends BaseActivity {
    private ActivityEditBinding binding;
    private NoteRepository repository;
    private int localId = -1;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userId = new SharedPrefsHelper(this).getUserId();
        repository = new NoteRepository(this, userId);

        binding.cardTopBar.setCardBackgroundColor(themeCardColor);
        binding.cardTitle.setCardBackgroundColor(themeCardColor);
        binding.cardCategory.setCardBackgroundColor(themeCardColor);
        binding.cardContent.setCardBackgroundColor(themeCardColor);

        binding.btnSave.setBackgroundTintList(ColorStateList.valueOf(themeMainColor));

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> saveNote());

        localId = getIntent().getIntExtra("note_id", -1);
    }

    private void saveNote() {
        String title = binding.etTitle.getText().toString().trim();
        String content = binding.etContent.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show();
            return;
        }

        int radioId = binding.radioGroup.getCheckedRadioButtonId();
        String category = "工作";
        if (radioId == R.id.radio_work) category = "工作";
        else if (radioId == R.id.radio_study) category = "学习";
        else if (radioId == R.id.radio_life) category = "生活";
        else if (radioId == R.id.radio_other) category = "其他";

        Note note = new Note();
        note.setTitle(title);
        note.setContent(content);
        note.setCategory(category);
        note.setUserId(userId);
        note.setSync(false);
        note.setDeleted(false);
        note.setCreateTime(System.currentTimeMillis());
        note.setUpdateTime(System.currentTimeMillis());

        if (localId != -1) {
            note.setLocalId(localId);
        }

        if (localId == -1) {
            repository.insertLocal(note, () -> {
                // 上传云端：改用匿名内部类，实现所有抽象方法
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
        } else {
            repository.updateLocal(note, () -> {
                // 上传云端：改用匿名内部类，实现所有抽象方法
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