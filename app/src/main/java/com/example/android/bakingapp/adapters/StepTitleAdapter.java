package com.example.android.bakingapp.adapters;

import android.databinding.DataBindingUtil;
import android.graphics.ColorFilter;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.bakingapp.R;
import com.example.android.bakingapp.databinding.RecipeImageItemBinding;
import com.example.android.bakingapp.databinding.StepTitleItemLayoutBinding;
import com.example.android.bakingapp.models.RecipeModel;
import com.example.android.bakingapp.models.Step;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Little Princess on 4/16/2018.*/

public class StepTitleAdapter extends RecyclerView.Adapter<StepTitleAdapter.CustomViewHolder> {
    private static final String TAG = StepTitleAdapter.class.getSimpleName();
    private OnStepClickListener stepClickListener;
    private List<Step> list;


    public interface OnStepClickListener {
        void onStepClickListener(int position);
    }

    public StepTitleAdapter(OnStepClickListener listener, List<Step> list) {
        this.stepClickListener = listener;
        this.list = list;
    }

    @NonNull
    @Override
    public StepTitleAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        StepTitleItemLayoutBinding mBinding = DataBindingUtil.inflate(layoutInflater, R.layout.step_title_item_layout, parent, false);
        return new StepTitleAdapter.CustomViewHolder(mBinding);
    }


    @Override
    public void onBindViewHolder(@NonNull StepTitleAdapter.CustomViewHolder holder, int position) {
        holder.bindView(position);
        holder.mBinding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final StepTitleItemLayoutBinding mBinding;

        CustomViewHolder(StepTitleItemLayoutBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
            mBinding.getRoot().setOnClickListener(this);
        }


        void bindView(int position) {
            String title = list.get(position).getShortDescription();
            if(title!=null && !TextUtils.isEmpty(title)){
                mBinding.textView.setText(title);
            }
        }

        @Override
        public void onClick(View view) {
           // view.setBackgroundColor(ContextCompat.getColor(view.getContext(),R.color.colorAccent));
            Log.i(TAG ,"stepClickListener :"+ stepClickListener + "  position:"+getAdapterPosition());
            stepClickListener.onStepClickListener(getAdapterPosition());
        }
    }
}
