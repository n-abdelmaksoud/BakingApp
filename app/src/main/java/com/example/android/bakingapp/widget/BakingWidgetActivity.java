package com.example.android.bakingapp.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.android.bakingapp.R;
import com.example.android.bakingapp.adapters.RecipeAdapter;
import com.example.android.bakingapp.data.WidgetDataOnSharedPreferences;
import com.example.android.bakingapp.databinding.ActivityBakingWidgetBinding;
import com.example.android.bakingapp.databinding.ActivityMainBinding;
import com.example.android.bakingapp.fetching_data.ApiClient;
import com.example.android.bakingapp.fetching_data.BakingService;
import com.example.android.bakingapp.models.Ingredient;
import com.example.android.bakingapp.models.RecipeModel;
import com.example.android.bakingapp.networkconnection.CheckingConnection;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.android.bakingapp.data.IngredientsUtils.getIngredientsText;

public class BakingWidgetActivity extends AppCompatActivity implements RecipeAdapter.RecipeClickListener{

    private static final String TAG = BakingWidgetProvider.class.getSimpleName();
    private ActivityBakingWidgetBinding mBinding;

    private static final String RECYCLER_VIEW_STATE_KEY= "save recycler view state";
    private Parcelable recyclerViewState;


    private BakingService mBakingService;
    private List<RecipeModel> list = null;
    private RecipeAdapter adapter;
    private GridLayoutManager gridLayoutManager;


    private int mAppWidgetId;
    private AppWidgetManager appWidgetManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_baking_widget);

        setResult(RESULT_CANCELED);
        appWidgetManager = AppWidgetManager.getInstance(this);
        getAppWidgetId();
        setWidgetConfiguration();
    }

    private void getAppWidgetId() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
    }

    private void setWidgetConfiguration() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }

        initializeGridLayoutManager();
        initializeRecyclerView();

        mBakingService = ApiClient.getClient().create(BakingService.class);

        if(!CheckingConnection.isNetworkConnected(this)) {
            displayOfflineToast();
            Log.i(TAG,"startLoading method failed: no network");
            return;
        }

        startLoadingRecipesList();

    }

    private void initializeGridLayoutManager() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            gridLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
        } else {
            gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        }
    }

    private void initializeRecyclerView() {
        mBinding.recyclerView.setLayoutManager(gridLayoutManager);
        mBinding.recyclerView.setHasFixedSize(true);
        adapter = new RecipeAdapter(this, new ArrayList<RecipeModel>());
        mBinding.recyclerView.setAdapter(adapter);
    }

    private void displayOfflineToast() {
        Toast.makeText(getApplicationContext(), getString(R.string.message_offline), Toast.LENGTH_SHORT).show();
    }

    private void startLoadingRecipesList() {
        Log.i(TAG, "start Loading RecipeList");

        Call<List<RecipeModel>> call =mBakingService.getRecipeList();
        call.enqueue(new Callback<List<RecipeModel>>() {
            @Override
            public void onResponse(Call<List<RecipeModel>> call, Response<List<RecipeModel>> response) {
                if(response.isSuccessful()) {
                    list = response.body();
                    Log.i(TAG,"Response is successful list size= "+list.size());
                    populateUI();
                }  else {
                    Log.i(TAG,"Loading RecipeList onResponse : NotSuccessful "+response.code());
                }

            }

            @Override
            public void onFailure(Call<List<RecipeModel>> call, Throwable t) {
                Log.e(TAG, "Response Failure "+ t.getMessage());
            }
        });
    }

    private void populateUI() {
        if(list.size()>0){
            adapter = new RecipeAdapter(BakingWidgetActivity.this, list);
            mBinding.recyclerView.setAdapter(adapter);
        }
        if(recyclerViewState!=null) {
            mBinding.recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
        }
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState!=null && savedInstanceState.containsKey(RECYCLER_VIEW_STATE_KEY)) {
            recyclerViewState=savedInstanceState.getParcelable(RECYCLER_VIEW_STATE_KEY);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        recyclerViewState= mBinding.recyclerView.getLayoutManager().onSaveInstanceState();
        outState.putParcelable(RECYCLER_VIEW_STATE_KEY,recyclerViewState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLoadingRecipesList();
    }

    @Override
    public void onRecipeClickListener(int position) {

        String ingredients = getIngredientsText(list.get(position).getIngredients());
        String title = list.get(position).getName();
        RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.widget_layout);
        views.setTextViewText(R.id.recipe_title, title);
        views.setTextViewText(R.id.recipe_ingredients, ingredients);

        WidgetDataOnSharedPreferences.saveWidgetDataOnSharedPreferences(this, title, ingredients);

        appWidgetManager.updateAppWidget(mAppWidgetId, views);
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }


}
