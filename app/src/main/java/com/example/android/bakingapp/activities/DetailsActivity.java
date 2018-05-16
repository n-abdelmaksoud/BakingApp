package com.example.android.bakingapp.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.FrameLayout;

import com.example.android.bakingapp.R;
import com.example.android.bakingapp.adapters.StepTitleAdapter;
import com.example.android.bakingapp.fragments.InstructionsFragment;
import com.example.android.bakingapp.fragments.StepsFragment;
import com.example.android.bakingapp.models.Ingredient;
import com.example.android.bakingapp.models.Step;

import java.util.List;

/**
 * Created by Little Princess on 4/15/2018.
 */

public class DetailsActivity extends AppCompatActivity implements StepTitleAdapter.OnStepClickListener {

    private static final String TAG = DetailsActivity.class.getSimpleName();

    private boolean isTwoPane;
    private FragmentManager fragmentManager;
    private StepsFragment stepsFragment;
    private InstructionsFragment instructionsFragment;
    private List<Step> stepList;
    private List<Ingredient> ingredientList;
    private String recipeTitle;
    private FrameLayout container;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        container = findViewById(R.id.fragment_container);
        fragmentManager= getSupportFragmentManager();

        getLists();
        getRecipeTitle();
        setActivityTitle(recipeTitle);
        isTwoPane = (container == null);
        Log.i(TAG ," isTwoPane :" +isTwoPane);
        Log.i(TAG ," stepList size"+ stepList.size());
        Log.i(TAG , "ingredientList size :"+ingredientList.size());

        getInnerFragmentInstances();
        startAttachingFragments();
    }

    private void getLists(){
        stepList= getIntent().getParcelableArrayListExtra(MainActivity.BUNDLE_STEP_LIST);
        ingredientList= getIntent().getParcelableArrayListExtra(MainActivity.BUNDLE_INGREDIENT_LIST);
    }

    private void getRecipeTitle(){
        recipeTitle= getIntent().getStringExtra(MainActivity.BUNDLE_RECIPE_TITLE);
    }


    private void getInnerFragmentInstances(){
        if(!isTwoPane){
            stepsFragment = StepsFragment.newInstance(stepList , ingredientList);
            instructionsFragment = InstructionsFragment.newInstance(stepList, ingredientList);
        } else {
            stepsFragment =(StepsFragment) fragmentManager.findFragmentById(R.id.fragment_steps);
            instructionsFragment = (InstructionsFragment) fragmentManager.findFragmentById(R.id.fragment_instructions);
        }
    }

    private void startAttachingFragments() {
        if (!isTwoPane) {
            if (fragmentManager.findFragmentById(R.id.fragment_container) == null) {
                fragmentManager.beginTransaction()
                        .add(R.id.fragment_container, stepsFragment).commit();
            }
        }
    }

    @Override
    public void onStepClickListener(int position) {
        Log.i(TAG ,"onStepClickListener"+ position);
        if(!isTwoPane){
            instructionsFragment.setCurrentStepPosition(position);
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container,instructionsFragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            instructionsFragment.populateUI(position);
        }
    }

    private void setActivityTitle(String title){
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(title);
    }

    public boolean getIsTwoPane(){
        return  isTwoPane;
    }

}
