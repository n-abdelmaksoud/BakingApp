package com.example.android.bakingapp.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.bakingapp.R;
import com.example.android.bakingapp.databinding.IngredientsDetailsItemBinding;
import com.example.android.bakingapp.models.Ingredient;

import java.util.List;

/**
 * Created by Little Princess on 4/19/2018.
 */

public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.CustomViewHolder> {

    private static final String TAG = IngredientsAdapter.class.getSimpleName();
    private List<Ingredient> list;
    private Context context;

    public IngredientsAdapter( Context context,List<Ingredient> list){
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        IngredientsDetailsItemBinding mBinding = DataBindingUtil.inflate(layoutInflater, R.layout.ingredients_details_item,parent,false);
        return new CustomViewHolder(mBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        holder.bindView(position);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder{

        IngredientsDetailsItemBinding mBinding;

        public CustomViewHolder(IngredientsDetailsItemBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding= mBinding;
        }

        public void bindView(int position) {
            Ingredient currentIngredient = list.get(position);
            String quantity = String.format(context.getResources().getString(R.string.quantity_measure),
                    currentIngredient.getQuantity(), currentIngredient.getMeasure());
            mBinding.ingredientName.setText(currentIngredient.getIngredient());
            mBinding.quantity.setText(quantity);

        }
    }
}
