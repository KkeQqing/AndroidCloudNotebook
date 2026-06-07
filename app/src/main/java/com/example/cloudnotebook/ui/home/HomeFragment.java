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

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private NoteViewModel viewModel;
    private NoteAdapter adapter;
    private boolean isMultiMode = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        // 搜索卡片主题
        if (getActivity() instanceof BaseActivity) {
            BaseActivity base = (BaseActivity) getActivity();
            binding.cardSearch.setCardBackgroundColor(base.themeCardColor);
        }

        adapter = new NoteAdapter(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Note note) {
                Intent intent = new Intent(requireContext(), EditNoteActivity.class);
                intent.putExtra("note_id", note.getLocalId());
                startActivity(intent);
            }

            @Override
            public void onLongClick(int position) {
                if (!isMultiMode) {
                    isMultiMode = true;
                    adapter.setMultiSelectMode(true);
                    binding.bottomBar.setVisibility(View.VISIBLE);
                }
            }
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
        loadAllNotes();

        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchNotes(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchNotes(newText);
                return true;
            }
        });

        binding.fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), EditNoteActivity.class));
        });

        binding.btnDelete.setOnClickListener(v -> {
            List<Integer> selectedIds = adapter.getSelectedIds();
            if (selectedIds.isEmpty()) {
                Toast.makeText(requireContext(), "请选择笔记", Toast.LENGTH_SHORT).show();
                return;
            }

            for (int delId : selectedIds) {
                for (int i = 0; i < adapter.notes.size(); i++) {
                    if (adapter.notes.get(i).getLocalId() == delId) {
                        adapter.notes.remove(i);
                        adapter.notifyItemRemoved(i);
                        break;
                    }
                }
            }

            viewModel.softDelete(selectedIds);
            binding.emptyView.setVisibility(adapter.notes.isEmpty() ? View.VISIBLE : View.GONE);
            exitMultiMode();
        });

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

        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.pullFromCloud();
            binding.swipeRefresh.setRefreshing(false);
        });
    }

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

    private void loadAllNotes() {
        viewModel.getAllNotes().observe(getViewLifecycleOwner(), notes -> {
            adapter.setNotes(notes);
            binding.emptyView.setVisibility(notes.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void exitMultiMode() {
        isMultiMode = false;
        adapter.setMultiSelectMode(false);
        binding.bottomBar.setVisibility(View.GONE);
    }
}