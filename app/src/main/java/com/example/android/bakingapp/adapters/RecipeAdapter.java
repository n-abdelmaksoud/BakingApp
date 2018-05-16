package com.example.android.bakingapp.adapters;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.bakingapp.R;
import com.example.android.bakingapp.databinding.RecipeImageItemBinding;
import com.example.android.bakingapp.models.RecipeModel;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Little Princess on 4/15/2018.
 */

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.CustomViewHolder> {

    private static final String TAG = RecipeAdapter.class.getSimpleName();
    private RecipeClickListener recipeClickListener;
    private List<RecipeModel> list;


    public interface RecipeClickListener {
        void onRecipeClickListener(int position);
    }

    public RecipeAdapter(RecipeClickListener listener, List<RecipeModel> list) {
        this.recipeClickListener = listener;
        this.list = list;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        RecipeImageItemBinding mBinding = DataBindingUtil.inflate(layoutInflater, R.layout.recipe_image_item, parent, false);
        return new CustomViewHolder(mBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        holder.bindView(position);
        holder.mBinding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final RecipeImageItemBinding mBinding;

        CustomViewHolder(RecipeImageItemBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
            mBinding.getRoot().setOnClickListener(this);
        }


        void bindView(int position) {
            String title = list.get(position).getName();
            if(title!=null && !TextUtils.isEmpty(title)){
                mBinding.title.setText(title);
            }
            String imagePath = list.get(position).getImage();
            if(imagePath!=null && !TextUtils.isEmpty(imagePath)){
                Uri imageUri = Uri.parse(imagePath);
                Picasso.get().load(imageUri).error(R.drawable.error).placeholder(R.drawable.placeholder)
                        .resizeDimen(R.dimen.poster_image_width, R.dimen.poster_image_height)
                        .onlyScaleDown().centerCrop().into(mBinding.recipeImage);
            } else {
                Picasso.get().load(R.drawable.recipe_image).error(R.drawable.error).placeholder(R.drawable.placeholder)
                        .resizeDimen(R.dimen.poster_image_width, R.dimen.poster_image_height)
                        .onlyScaleDown().centerCrop().into(mBinding.recipeImage);
            }

        }

        @Override
        public void onClick(View view) {
            recipeClickListener.onRecipeClickListener(getAdapterPosition());
        }
    }

}
