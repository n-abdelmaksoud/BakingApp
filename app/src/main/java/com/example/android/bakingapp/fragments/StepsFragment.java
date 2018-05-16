package com.example.android.bakingapp.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.bakingapp.R;
import com.example.android.bakingapp.adapters.StepTitleAdapter;
import com.example.android.bakingapp.databinding.FragmentStepsBinding;
import com.example.android.bakingapp.models.Ingredient;
import com.example.android.bakingapp.models.Step;
import com.example.android.bakingapp.activities.DetailsActivity;
import com.example.android.bakingapp.activities.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Little Princess on 4/15/2018.
 */

public class StepsFragment extends Fragment {

    private static final String TAG = StepsFragment.class.getSimpleName();
    private FragmentStepsBinding mBinding;
    private List<Step> stepList;
    private List<Ingredient> ingredientList;
    private boolean isTwoPane;
    StepTitleAdapter.OnStepClickListener listener;

    public StepsFragment(){

    }

    public static StepsFragment newInstance(List<Step> stepList, List<Ingredient> ingredientList) {
        StepsFragment stepsFragment = new StepsFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(MainActivity.BUNDLE_STEP_LIST, (ArrayList<? extends Parcelable>) stepList);
        args.putParcelableArrayList(MainActivity.BUNDLE_INGREDIENT_LIST, (ArrayList<? extends Parcelable>) ingredientList);
        Log.i(TAG,"stepFragment newInstance args: "+args.toString());
        stepsFragment.setArguments(args);
        return stepsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_steps, container, false);

        return mBinding.getRoot();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        isTwoPane = ((DetailsActivity)getActivity()).getIsTwoPane();
        Log.i(TAG ," isTwoPane :" +isTwoPane);

        setStepsListenerInstance();
        initializeRecyclerView();

        getLists();
        Log.i(TAG ," stepList size"+ stepList.size());
        Log.i(TAG , "ingredientList size :"+ingredientList.size());


        if(stepList!=null && stepList.size()>0) {
            populateStepsUI();
        }

        mBinding.ingredients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notifyListenerToDisplayIngredients();
            }
        });
    }

    private void setStepsListenerInstance(){
        try{
            listener=(StepTitleAdapter.OnStepClickListener)getActivity();
        } catch(ClassCastException exception){
            throw new ClassCastException("hosting activity must implement OnStepClickListener");
        }
    }


    public void getLists() {
        if(!isTwoPane){
            stepList = getArguments().getParcelableArrayList(MainActivity.BUNDLE_STEP_LIST);
            ingredientList = getArguments().getParcelableArrayList(MainActivity.BUNDLE_INGREDIENT_LIST);
        } else {
            if(getActivity().getIntent()!=null) {
                stepList = getActivity().getIntent().getParcelableArrayListExtra(MainActivity.BUNDLE_STEP_LIST);
                ingredientList = getActivity().getIntent().getParcelableArrayListExtra(MainActivity.BUNDLE_INGREDIENT_LIST);
            }
        }
    }

    private void initializeRecyclerView(){
        GridLayoutManager layoutManager= new GridLayoutManager(getActivity(), 1);
        mBinding.rvSteps.setLayoutManager(layoutManager);
        mBinding.rvSteps.setHasFixedSize(true);
        StepTitleAdapter stepTitleAdapter = new StepTitleAdapter(listener,new ArrayList<Step>());
        mBinding.rvSteps.setAdapter(stepTitleAdapter);
    }


    private void populateStepsUI() {
        StepTitleAdapter stepTitleAdapter = new StepTitleAdapter(listener,stepList);
        mBinding.rvSteps.setAdapter(stepTitleAdapter);
    }

    // position = -1 refers to ingredients details
    private void notifyListenerToDisplayIngredients() {
        listener.onStepClickListener(-1);
    }

}
