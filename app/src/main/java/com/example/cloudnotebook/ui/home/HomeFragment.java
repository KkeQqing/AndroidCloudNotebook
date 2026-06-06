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
import com.example.cloudnotebook.databinding.FragmentHomeBinding;
import com.example.cloudnotebook.room.entity.Note;
import com.example.cloudnotebook.ui.edit.EditNoteActivity;
import com.example.cloudnotebook.viewmodel.NoteViewModel;

import java.util.List;

/**
 * 首页 Fragment
 * 功能：展示所有笔记列表、搜索、下拉刷新、添加笔记、批量删除/同步
 */
public class HomeFragment extends Fragment {

    // 视图绑定，关联 fragment_home.xml 所有控件
    private FragmentHomeBinding binding;

    // 业务ViewModel：负责笔记数据的增删改查、同步、搜索
    private NoteViewModel viewModel;

    // 列表适配器：负责把笔记数据显示到RecyclerView
    private NoteAdapter adapter;

    // 标记是否处于【多选模式】（批量删除用）
    private boolean isMultiMode = false;

    /**
     * 创建Fragment视图
     * 加载布局，返回根视图
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 绑定布局
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * 视图创建完成后
     * 初始化数据、适配器、点击事件、观察数据变化
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化ViewModel
        viewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        // 初始化列表适配器，并设置点击/长按监听
        adapter = new NoteAdapter(new NoteAdapter.OnItemClickListener() {
            /**
             * 单击笔记：跳转到编辑页面，携带笔记ID
             */
            @Override
            public void onItemClick(Note note) {
                Intent intent = new Intent(requireContext(), EditNoteActivity.class);
                intent.putExtra("note_id", note.getLocalId());
                startActivity(intent);
            }

            /**
             * 长按笔记：进入多选模式，显示底部操作栏
             */
            @Override
            public void onLongClick(int position) {
                if (!isMultiMode) {
                    isMultiMode = true;
                    adapter.setMultiSelectMode(true);       // 开启多选
                    binding.bottomBar.setVisibility(View.VISIBLE); // 显示底部栏
                }
            }
        });

        // 设置列表布局管理器（垂直列表）
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        // 给列表设置适配器
        binding.recyclerView.setAdapter(adapter);

        // 观察ViewModel中的所有笔记数据，数据变化时自动刷新列表
        viewModel.getAllNotes().observe(getViewLifecycleOwner(), notes -> {
            adapter.setNotes(notes); // 更新列表数据

            // 空数据判断：没有笔记时显示空布局
            if (notes.isEmpty())
                binding.emptyView.setVisibility(View.VISIBLE);
            else
                binding.emptyView.setVisibility(View.GONE);
        });

        // 悬浮添加按钮：点击新建笔记
        binding.fabAdd.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), EditNoteActivity.class))
        );

        // 底部栏 - 删除按钮：批量删除选中的笔记
        binding.btnDelete.setOnClickListener(v -> {
            List<Integer> selected = adapter.getSelectedIds(); // 获取选中的笔记ID
            if (selected.isEmpty()) {
                Toast.makeText(requireContext(), "请选择笔记", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.softDelete(selected);  // 执行删除
            exitMultiMode();                 // 退出多选模式
        });

        // 底部栏 - 同步按钮：批量同步选中的笔记
        binding.btnSync.setOnClickListener(v -> {
            List<Integer> selected = adapter.getSelectedIds();
            if (selected.isEmpty()) {
                Toast.makeText(requireContext(), "请选择笔记", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.syncAllUnsyncedNotes(); // 执行同步
            Toast.makeText(requireContext(), "同步请求已发送", Toast.LENGTH_SHORT).show();
            exitMultiMode();
        });

        // 下拉刷新：从云端拉取最新数据
        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.pullFromCloud();
            binding.swipeRefresh.setRefreshing(false); // 关闭刷新动画
        });

        // 搜索框监听：文字变化时实时搜索
        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            // 搜索提交
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.searchNotes(query);
                return true;
            }

            // 搜索文字变化
            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.searchNotes(newText);
                return true;
            }
        });
    }

    /**
     * 退出多选模式
     * 重置状态、隐藏底部栏、清空选择
     */
    private void exitMultiMode() {
        isMultiMode = false;
        adapter.setMultiSelectMode(false);
        binding.bottomBar.setVisibility(View.GONE);
    }
}