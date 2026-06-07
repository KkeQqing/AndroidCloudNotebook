package com.example.cloudnotebook.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.cloudnotebook.adapter.NoteAdapter;
import com.example.cloudnotebook.base.BaseActivity;
import com.example.cloudnotebook.databinding.FragmentHomeBinding;
import com.example.cloudnotebook.room.entity.Note;
import com.example.cloudnotebook.ui.edit.EditNoteActivity;
import com.example.cloudnotebook.viewmodel.NoteViewModel;

import java.util.List;

/**
 * 首页 Fragment（主界面）
 * 功能：
 * 1. 展示所有笔记列表
 * 2. 搜索笔记
 * 3. 长按进入多选删除/同步模式
 * 4. 下拉刷新从云端拉取数据
 * 5. 点击跳转编辑页面
 * 6. 悬浮按钮添加笔记
 */
public class HomeFragment extends Fragment {
    // 视图绑定对象，替代 findViewById
    private FragmentHomeBinding binding;

    // 笔记 ViewModel，负责数据与业务逻辑
    private NoteViewModel viewModel;

    // 笔记列表适配器
    private NoteAdapter adapter;

    // 是否处于多选模式
    private boolean isMultiMode = false;

    /**
     * 创建 Fragment 视图
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * 视图创建完成，初始化所有功能
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 获取 ViewModel 实例
        viewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        // 搜索框卡片适配全局主题颜色
        if (getActivity() instanceof BaseActivity) {
            BaseActivity base = (BaseActivity) getActivity();
            binding.cardSearch.setCardBackgroundColor(base.themeCardColor);
        }

        // 初始化适配器，设置点击/长按事件
        adapter = new NoteAdapter(new NoteAdapter.OnItemClickListener() {
            // 点击笔记 → 进入编辑页面
            @Override
            public void onItemClick(Note note) {
                Intent intent = new Intent(requireContext(), EditNoteActivity.class);
                intent.putExtra("note_id", note.getLocalId());
                startActivity(intent);
            }

            // 长按笔记 → 开启多选模式
            @Override
            public void onLongClick(int position) {
                if (!isMultiMode) {
                    isMultiMode = true;
                    adapter.setMultiSelectMode(true);  // 显示复选框
                    binding.bottomBar.setVisibility(View.VISIBLE); // 显示底部操作栏
                }
            }
        });

        // 设置列表布局与适配器
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);

        // 加载所有笔记
        loadAllNotes();

        // 搜索框监听
        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            // 搜索提交
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchNotes(query);
                return true;
            }

            // 文字变化实时搜索
            @Override
            public boolean onQueryTextChange(String newText) {
                searchNotes(newText);
                return true;
            }
        });

        // 悬浮按钮 → 新建笔记
        binding.fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), EditNoteActivity.class));
        });

        // ======================
        // 【新增】顶部云同步按钮
        // ======================
        binding.btnCloudSync.setOnClickListener(v -> {
            viewModel.syncAllUnsyncedNotes();
            Toast.makeText(requireContext(), "已开始后台同步所有笔记", Toast.LENGTH_SHORT).show();
        });

        // 底部栏：删除选中笔记
        binding.btnDelete.setOnClickListener(v -> {
            List<Integer> selectedIds = adapter.getSelectedIds();
            if (selectedIds.isEmpty()) {
                Toast.makeText(requireContext(), "请选择笔记", Toast.LENGTH_SHORT).show();
                return;
            }

            // 从列表中移除
            for (int delId : selectedIds) {
                for (int i = 0; i < adapter.notes.size(); i++) {
                    if (adapter.notes.get(i).getLocalId() == delId) {
                        adapter.notes.remove(i);
                        adapter.notifyItemRemoved(i);
                        break;
                    }
                }
            }

            // 执行软删除
            viewModel.softDelete(selectedIds);
            binding.emptyView.setVisibility(adapter.notes.isEmpty() ? View.VISIBLE : View.GONE);
            exitMultiMode(); // 退出多选模式
        });

        // 底部栏：同步选中笔记到云端
        binding.btnSync.setOnClickListener(v -> {
            List<Integer> selected = adapter.getSelectedIds();
            if (selected.isEmpty()) {
                Toast.makeText(requireContext(), "请选择笔记", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.syncAllUnsyncedNotes();
            Toast.makeText(requireContext(), "同步请求已发送", Toast.LENGTH_SHORT).show();
            exitMultiMode();
        });

        // 下拉刷新 → 从云端拉取最新笔记
        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.pullFromCloud();
            binding.swipeRefresh.setRefreshing(false);
        });
    }

    /**
     * 搜索笔记
     */
    private void searchNotes(String keyword) {
        if (keyword.isEmpty()) {
            loadAllNotes();
            return;
        }
        viewModel.searchNotes(keyword).observe(getViewLifecycleOwner(), notes -> {
            adapter.setNotes(notes);
            binding.emptyView.setVisibility(notes.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    /**
     * 加载所有笔记
     */
    private void loadAllNotes() {
        viewModel.getAllNotes().observe(getViewLifecycleOwner(), notes -> {
            adapter.setNotes(notes);
            binding.emptyView.setVisibility(notes.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    /**
     * 退出多选模式，隐藏底部栏，取消选中
     */
    private void exitMultiMode() {
        isMultiMode = false;
        adapter.setMultiSelectMode(false);
        binding.bottomBar.setVisibility(View.GONE);
    }
}