package com.example.cloudnotebook.ui.category;

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
import com.example.cloudnotebook.databinding.FragmentCategoryBinding;
import com.example.cloudnotebook.room.entity.Note;
import com.example.cloudnotebook.viewmodel.NoteViewModel;
import com.google.android.material.chip.Chip;

import java.util.Arrays;
import java.util.List;

/**
 * 笔记分类Fragment
 * 功能：通过Chip标签筛选【工作/学习/生活/其他】四类笔记，展示对应分类列表与笔记总数
 */
public class CategoryFragment extends Fragment {
    // ViewBinding布局绑定对象，对应fragment_category.xml
    private FragmentCategoryBinding binding;
    // 笔记数据ViewModel，负责ROOM数据库查询分类数据
    private NoteViewModel viewModel;
    // 笔记列表适配器，RecyclerView数据填充
    private NoteAdapter adapter;
    // 当前选中的分类，默认初始为【工作】
    private String currentCategory = "工作";

    /**
     * 加载Fragment布局，使用ViewBinding填充布局
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * 视图创建完成后初始化控件、适配器、分类标签、数据监听
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 实例化ViewModel
        viewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        // 初始化列表点击监听：分类页面无需编辑跳转、无需长按多选，方法空实现
        adapter = new NoteAdapter(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Note note) {
                // 分类页点击条目暂不跳转编辑页，空实现
            }
            @Override
            public void onLongClick(int position) {
                // 分类页无批量删除多选功能，长按空实现
            }
        });

        // RecyclerView设置线性布局管理器 + 绑定适配器
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);

        // 定义全部分类集合
        List<String> categories = Arrays.asList("工作", "学习", "生活", "其他");
        // 循环动态创建分类Chip标签并添加到ChipGroup容器
        for (String cat : categories) {
            binding.chipGroup.addView(createChip(cat));
        }

        // 默认加载【工作】分类笔记，监听数据变化刷新列表与数量文本
        viewModel.getNotesByCategory(currentCategory).observe(getViewLifecycleOwner(), notes -> {
            adapter.setNotes(notes);
            binding.tvCount.setText("共" + notes.size() + "条");
        });
    }

    /**
     * 动态生成单个分类Chip标签
     * @param category 分类名称：工作/学习/生活/其他
     * @return 生成后的Chip控件
     */
    private Chip createChip(String category) {
        Chip chip = new Chip(requireContext());
        // 设置标签文字
        chip.setText(category);
        // 设置标签可选中
        chip.setCheckable(true);
        // 标签点击切换分类，重新查询对应分类笔记
        chip.setOnClickListener(v -> {
            currentCategory = category;
            // 根据选中分类查询数据，更新列表与统计条数
            viewModel.getNotesByCategory(category).observe(getViewLifecycleOwner(), notes -> {
                adapter.setNotes(notes);
                binding.tvCount.setText("共" + notes.size() + "条");
            });
        });
        return chip;
    }
}