package com.example.android.bakingapp.fetching_data;

import com.example.android.bakingapp.models.RecipeModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by Little Princess on 4/15/2018.
 */

public interface BakingService {

        @GET("topher/2017/May/59121517_baking/baking.json")
        Call<List<RecipeModel>> getRecipeList();

}
