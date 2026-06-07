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

public class CategoryFragment extends Fragment {
    private FragmentCategoryBinding binding;
    private NoteViewModel viewModel;
    private NoteAdapter adapter;
    private String currentCategory = "工作";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        adapter = new NoteAdapter(new NoteAdapter.OnItemClickListener() {
            @Override public void onItemClick(Note note) {}
            @Override public void onLongClick(int position) {}
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);

        List<String> categories = Arrays.asList("工作", "学习", "生活", "其他");
        for (String cat : categories) { binding.chipGroup.addView(createChip(cat)); }

        viewModel.getNotesByCategory(currentCategory).observe(getViewLifecycleOwner(), notes -> {
            adapter.setNotes(notes);
            binding.tvCount.setText("共" + notes.size() + "条");
        });
    }

    private Chip createChip(String category) {
        Chip chip = new Chip(requireContext());
        chip.setText(category);
        chip.setCheckable(true);

        // ======================
        // 分类标签背景 + 文字 变色
        // ======================
        if (getActivity() instanceof BaseActivity) {
            BaseActivity base = (BaseActivity) getActivity();
            chip.setChipBackgroundColor(ColorStateList.valueOf(base.themeCardColor));
            chip.setTextColor(base.themeMainColor);
        }

        chip.setOnClickListener(v -> {
            currentCategory = category;
            viewModel.getNotesByCategory(category).observe(getViewLifecycleOwner(), notes -> {
                adapter.setNotes(notes);
                binding.tvCount.setText("共" + notes.size() + "条");
            });
        });
        return chip;
    }
}