package com.example.android.bakingapp.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.Snackbar;
import android.support.test.espresso.IdlingResource;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.example.android.bakingapp.data.IngredientsUtils;
import com.example.android.bakingapp.data.WidgetDataOnSharedPreferences;
import com.example.android.bakingapp.idlingresource.SimpleIdlingResource;
import com.example.android.bakingapp.models.Ingredient;
import com.example.android.bakingapp.models.Step;
import com.example.android.bakingapp.networkconnection.CheckingConnection;
import com.example.android.bakingapp.R;
import com.example.android.bakingapp.adapters.RecipeAdapter;
import com.example.android.bakingapp.databinding.ActivityMainBinding;
import com.example.android.bakingapp.fetching_data.ApiClient;
import com.example.android.bakingapp.fetching_data.BakingService;
import com.example.android.bakingapp.models.RecipeModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements RecipeAdapter.RecipeClickListener{
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String RECYCLER_VIEW_STATE_KEY= "save recycler view state";
    public static final String BUNDLE_RECIPE_TITLE = "intent clicked recipe title";
    public static final String BUNDLE_STEP_LIST = "intent step list";
    public static final String BUNDLE_INGREDIENT_LIST = "intent ingredient list";
    private Parcelable recyclerViewState;
    
    private ActivityMainBinding mBinding;
    private BakingService bakingService;
    private List<RecipeModel> list=null;
    private RecipeAdapter adapter;
    @Nullable
    private SimpleIdlingResource mIdlingResource;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getIdlingResource();

        mBinding= DataBindingUtil.setContentView(this,R.layout.activity_main);
        bakingService = ApiClient.getClient().create(BakingService.class);

       initializeRecyclerView(numberOfColumns());

       if(!CheckingConnection.isNetworkConnected(this)) {
            displayOfflineEmptyView();
            Log.i(TAG,"startLoadingPopularMovies method failed: no network");
            return;
        }

        startLoadingRecipesList();
    }

    //This method is copied from reviewer suggestion.
    private int numberOfColumns() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        // You can change this divider to adjust the size of the poster
        int widthDivider =(int) getResources().getDimension(R.dimen.poster_image_width);
        int width = displayMetrics.widthPixels;
        int nColumns = width / widthDivider;
        return nColumns;
    }


    private void initializeRecyclerView(int columnCount){
        GridLayoutManager layoutManager= new GridLayoutManager(this,columnCount);
        mBinding.recyclerView.setLayoutManager(layoutManager);
        mBinding.recyclerView.setHasFixedSize(true);
    }

    private void startLoadingRecipesList() {
        Log.i(TAG, "start Loading RecipeList");

        setRecyclerViewVisibility(false);

        Call<List<RecipeModel>> call =bakingService.getRecipeList();
        call.enqueue(new Callback<List<RecipeModel>>() {
            @Override
            public void onResponse(Call<List<RecipeModel>> call, Response<List<RecipeModel>> response) {
                if(response.isSuccessful()) {
                    list = response.body();
                    Log.i(TAG,"Response is successful list size= "+list.size());
                    if(list.size()>0){
                        setRecyclerViewVisibility(true);
                        populateUI(list);
                        if (mIdlingResource != null) {
                            mIdlingResource.setIdleState(true);
                        }
                    }
                }  else {
                Log.i(TAG,"Loadingie RecipeList onResponse : NotSuccessful "+response.code());
                showSnackBarMessage(response.message());
            }

            }

            @Override
            public void onFailure(Call<List<RecipeModel>> call, Throwable t) {
                Log.e(TAG, "Response Failure "+ t.getMessage());
            }
        });
    }



    private void populateUI(List<RecipeModel> list) {
        if(list.size()>0) {
            Log.i(TAG,"started populateUI . list size = "+list.size());
            adapter= new RecipeAdapter(this,list);
            mBinding.recyclerView.setAdapter(adapter);
            if(recyclerViewState!=null) {
                mBinding.recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
            }
        }
    }


    private void setRecyclerViewVisibility(boolean isVisible){
        if(isVisible){
            mBinding.placeHolderLayout.progressBar.setVisibility(View.INVISIBLE);
            mBinding.placeHolderLayout.tvEmptyView.setVisibility(View.INVISIBLE);
            mBinding.recyclerView.setVisibility(View.VISIBLE);
        } else {
            mBinding.recyclerView.setVisibility(View.INVISIBLE);
            mBinding.placeHolderLayout.progressBar.setVisibility(View.VISIBLE);
            mBinding.placeHolderLayout.tvEmptyView.setVisibility(View.INVISIBLE);
        }

    }

    private void displayOfflineEmptyView(){
        mBinding.placeHolderLayout.progressBar.setVisibility(View.INVISIBLE);
        mBinding.placeHolderLayout.tvEmptyView.setText(R.string.message_offline);
        mBinding.placeHolderLayout.tvEmptyView.setVisibility(View.VISIBLE);
        mBinding.recyclerView.setVisibility(View.INVISIBLE);
    }

    private void showSnackBarMessage(String message) {
        Snackbar.make(mBinding.parentView,message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState!=null && savedInstanceState.containsKey(RECYCLER_VIEW_STATE_KEY)) {
            Log.i(TAG,"restore recycler view state from onRestoreInstanceState");
            recyclerViewState=savedInstanceState.getParcelable(RECYCLER_VIEW_STATE_KEY);

        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG,"save recycler view state from onSaveInstanceState");
        recyclerViewState= mBinding.recyclerView.getLayoutManager().onSaveInstanceState();
        outState.putParcelable(RECYCLER_VIEW_STATE_KEY,recyclerViewState);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"onResume startLoadingRecipeList");
        if (mIdlingResource != null) {
            mIdlingResource.setIdleState(false);
        }
        startLoadingRecipesList();
    }

    @Override
    public void onRecipeClickListener(int position) {
        startRecipeDetailsActivity(position);
        saveSelectedRecipeOnSharedPreferences(position);
    }

    private void startRecipeDetailsActivity(int position) {
        List<Step> stepList = list.get(position).getSteps();
        List<Ingredient> ingredientList = list.get(position).getIngredients();
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putParcelableArrayListExtra(BUNDLE_STEP_LIST,(ArrayList<? extends Parcelable>)stepList);
        intent.putParcelableArrayListExtra(BUNDLE_INGREDIENT_LIST,(ArrayList<? extends Parcelable>)ingredientList);
        intent.putExtra(BUNDLE_RECIPE_TITLE, list.get(position).getName());
        startActivity(intent);

    }

    private void saveSelectedRecipeOnSharedPreferences(int position) {
        String title = list.get(position).getName();
        List<Ingredient> ingredientList = list.get(position).getIngredients();
        String ingredients = IngredientsUtils.getIngredientsText(ingredientList);
        WidgetDataOnSharedPreferences.saveWidgetDataOnSharedPreferences(this, title, ingredients);
    }

    @VisibleForTesting
    @Nullable
    public IdlingResource getIdlingResource(){
        if(mIdlingResource == null){
            mIdlingResource = new SimpleIdlingResource();
        }
        return mIdlingResource;
    }
}
