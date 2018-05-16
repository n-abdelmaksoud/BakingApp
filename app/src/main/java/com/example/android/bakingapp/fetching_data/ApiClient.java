package com.example.android.bakingapp.fetching_data;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Little Princess on 4/15/2018.
 */


    public class ApiClient {

        private static final String BASE_URL = "https://d17h27t6h515a5.cloudfront.net/";
        private static Retrofit retrofit = null;


        public static Retrofit getClient() {
            if (retrofit==null) {
                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }
            return retrofit;
        }
    }

