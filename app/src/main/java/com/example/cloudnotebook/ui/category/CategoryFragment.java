package com.example.cloudnotebook.ui.category;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.cloudnotebook.adapter.NoteAdapter;
import com.example.cloudnotebook.base.BaseActivity;
import com.example.cloudnotebook.databinding.FragmentCategoryBinding;
import com.example.cloudnotebook.room.entity.Note;
import com.example.cloudnotebook.viewmodel.NoteViewModel;
import com.google.android.material.chip.Chip;
import java.util.Arrays;
import java.util.List;

/**
 * 分类 Fragment
 * 功能：展示不同分类（工作、学习、生活、其他）下的笔记列表
 * 特点：动态生成分类标签、切换分类自动刷新列表、支持主题切换
 */
public class CategoryFragment extends Fragment {

    // 视图绑定对象，替代 findViewById
    private FragmentCategoryBinding binding;

    // 笔记ViewModel，负责业务逻辑与数据获取
    private NoteViewModel viewModel;

    // 笔记列表适配器
    private NoteAdapter adapter;

    // 当前选中的分类，默认：工作
    private String currentCategory = "工作";

    /**
     * 创建Fragment视图
     * 使用ViewBinding加载布局
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * 视图创建完成后执行初始化
     * 初始化ViewModel、适配器、分类标签、观察者
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化ViewModel
        viewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        // 初始化笔记适配器（此处仅展示，不实现点击事件）
        adapter = new NoteAdapter(new NoteAdapter.OnItemClickListener() {
            @Override public void onItemClick(Note note) {}
            @Override public void onLongClick(int position) {}
        });

        // 设置RecyclerView布局管理器与适配器
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);

        // 定义所有分类：工作、学习、生活、其他
        List<String> categories = Arrays.asList("工作", "学习", "生活", "其他");

        // 循环动态创建 Chip 分类标签，并添加到 ChipGroup
        for (String cat : categories) {
            binding.chipGroup.addView(createChip(cat));
        }

        // 观察【当前分类】的笔记数据，数据变化自动刷新列表
        viewModel.getNotesByCategory(currentCategory).observe(getViewLifecycleOwner(), notes -> {
            adapter.setNotes(notes);
            binding.tvCount.setText("共" + notes.size() + "条");
        });
    }

    /**
     * 动态创建分类标签 Chip
     * @param category 分类名称
     * @return 配置好的 Chip 对象
     */
    private Chip createChip(String category) {
        Chip chip = new Chip(requireContext());
        chip.setText(category);       // 设置分类文字
        chip.setCheckable(true);      // 设置为可选中

        // ======================
        // 主题适配：跟随全局主题变色
        // ======================
        if (getActivity() instanceof BaseActivity) {
            BaseActivity base = (BaseActivity) getActivity();
            chip.setChipBackgroundColor(ColorStateList.valueOf(base.themeCardColor));
            chip.setTextColor(base.themeMainColor);
        }

        // 点击分类标签：切换当前分类，并刷新对应笔记列表
        chip.setOnClickListener(v -> {
            currentCategory = category;

            // 观察【选中分类】的笔记数据
            viewModel.getNotesByCategory(category).observe(getViewLifecycleOwner(), notes -> {
                adapter.setNotes(notes);
                binding.tvCount.setText("共" + notes.size() + "条");
            });
        });

        return chip;
    }
}