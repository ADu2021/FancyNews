package com.java.duyiyang.ui.slideshow;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.java.duyiyang.ApiAdapter;
import com.java.duyiyang.MainActivity;
import com.java.duyiyang.R;
import com.java.duyiyang.databinding.FragmentSlideshowBinding;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

// search
public class SlideshowFragment extends Fragment {

    private FragmentSlideshowBinding binding;

    private TextInputEditText keyword;
    private MaterialDatePicker materialDatePicker = null;
    private Button setDate;
    private Spinner category;
    private Button search;

    private String[] categories;

    private String startD; // initialized
    private String endD; // initialized

    private ApiAdapter api;
    public ApiAdapter getApi() {return api;}


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SlideshowViewModel slideshowViewModel =
                new ViewModelProvider(this).get(SlideshowViewModel.class);

        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        keyword = binding.searchKeywords;
        setDate = binding.searchDate;
        category = binding.searchCategory;
        search = binding.searchButton;

        final TextView textView = binding.textSlideshow;
        slideshowViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    private void setDateRangePicker() {
        MaterialDatePicker.Builder<Pair<Long, Long>> materialDateBuilder
                = MaterialDatePicker.Builder.dateRangePicker();
        materialDateBuilder.setTitleText("设置日期范围");
        materialDatePicker = materialDateBuilder.build();

        setDate.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(materialDatePicker.isAdded()) return;
                        materialDatePicker.show(((FragmentActivity) getContext())
                                .getSupportFragmentManager(), "设置开始与截止日期");
                    }
                });

        materialDatePicker.addOnPositiveButtonClickListener(
                new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
                    @Override
                    public void onPositiveButtonClick(Pair<Long, Long> selection) {
                        Long startDate = selection.first;
                        Long endDate = selection.second;
                        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
                        Date dateD=new Date();
                        dateD.setTime(startDate);
                        startD = dateFormat.format(dateD);
                        dateD.setTime(endDate);
                        endD = dateFormat.format(dateD);
                        setDate.setText("搜索范围："+startD+"至"+endD);
                    }
                });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("SlideshowFragment","onClick");
                api.setDefault();
                api.setStartDate(startD);
                api.setEndDate(endD);
                api.setWords(keyword.getText().toString());
                if(!category.getSelectedItem().toString().equals(categories[0]))
                    api.setCategories(category.getSelectedItem().toString());

                Bundle bundle = new Bundle();
                api.packBundle(bundle);
                Navigation.findNavController(view).navigate(R.id.listFragment, bundle);
            }
        });
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        MainActivity mainActivity = (MainActivity) getContext();
        mainActivity.setBarTitle("搜索");

        categories = getResources().getStringArray(R.array.categories_default_array);
        startD = LocalDate.now().plusDays(-7).toString();
        endD = LocalDate.now().toString();
        setDate.setText("搜索范围："+startD+"至"+endD);

        setDateRangePicker();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.categories_default_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category.setAdapter(adapter);

        api = new ApiAdapter();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}