package com.example.android.bakingapp.fragments;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.android.bakingapp.R;
import com.example.android.bakingapp.adapters.IngredientsAdapter;
import com.example.android.bakingapp.databinding.FragmentInstructionsBinding;
import com.example.android.bakingapp.models.Ingredient;
import com.example.android.bakingapp.models.Step;
import com.example.android.bakingapp.activities.DetailsActivity;
import com.example.android.bakingapp.activities.MainActivity;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Little Princess on 4/15/2018.
 */

public class InstructionsFragment extends Fragment implements Player.EventListener {


    private static final String TAG = InstructionsFragment.class.getSimpleName();
    public static final String SAVE_STEP_POSITION = "save step position";
    private static final String SAVE_PLAYER_POSITION = "save player position";
    private static final String SAVE_PLAY_WHEN_READY = "save play when ready";

    private FragmentInstructionsBinding mBinding;
    private int currentStepPosition = -1;
    private List<Ingredient> ingredientList;
    private List<Step> stepList;
    private boolean isTwoPane;
    private IngredientsAdapter ingredientsAdapter;
    private static SimpleExoPlayer mExoPlayer;
    private static MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;
    private MediaButtonsActionsReceiver mediaButtonReceiver;
    private NoisyAudioStreamReceiver noisyAudioStreamReceiver;
    private boolean playWhenReady= false;
    private long mPlayerPosition = 0;
    private boolean isFullScreenPlayer = false;


    public InstructionsFragment(){
    }

    public static InstructionsFragment newInstance(List<Step> stepList, List<Ingredient> ingredientList) {
        InstructionsFragment instructionsFragment = new InstructionsFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(MainActivity.BUNDLE_STEP_LIST,(ArrayList<? extends Parcelable>) stepList);
        args.putParcelableArrayList(MainActivity.BUNDLE_INGREDIENT_LIST, (ArrayList<? extends Parcelable>) ingredientList);
        instructionsFragment.setArguments(args);
        return instructionsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG ,"bundle onCreate: "+savedInstanceState);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_instructions, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        isTwoPane = ((DetailsActivity)getActivity()).getIsTwoPane();
        Log.i(TAG ," isTwoPane :" +isTwoPane);


        if(savedInstanceState != null){
            restoreFragmentMembers(savedInstanceState);
        }

        initializeRecyclerView();
        initializeMediaSession();
        initializePlayer();
        setButtonsListeners();
        getLists();

        Log.i(TAG , "onActivityCreated + current position"+currentStepPosition );
        populateUI(currentStepPosition);
        if(savedInstanceState!=null){
            restoreExoPlayerState();
        }
    }

    private void setButtonsListeners() {
        mBinding.btnNext.setOnClickListener(mNextClickListener);
        mBinding.btnPrevious.setOnClickListener(mPreviousClickListener);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mExoPlayer != null){
            mPlayerPosition = mExoPlayer.getCurrentPosition();
            playWhenReady = mExoPlayer.getPlayWhenReady();
        }
        Log.i(TAG , "saveInstance: stepPosition "+ currentStepPosition);

        outState.putBoolean(SAVE_PLAY_WHEN_READY, playWhenReady);
        outState.putInt(SAVE_STEP_POSITION, currentStepPosition);
        outState.putLong(SAVE_PLAYER_POSITION, mPlayerPosition);
    }

    private void restoreFragmentMembers(Bundle saveInstanceState) {
        currentStepPosition = saveInstanceState.getInt(SAVE_STEP_POSITION);
        playWhenReady = saveInstanceState.getBoolean(SAVE_PLAY_WHEN_READY);
        mPlayerPosition = saveInstanceState.getLong(SAVE_PLAYER_POSITION);
        Log.i(TAG , "restoreFragmentMembers: stepPosition "+currentStepPosition);
        Log.i(TAG , "restoreFragmentMembers: playWhenReady "+playWhenReady);
        Log.i(TAG , "restoreFragmentMembers: mPlayerPosition "+mPlayerPosition);
    }

    private void restoreExoPlayerState(){
        if(mExoPlayer!=null){
            mExoPlayer.seekTo(mPlayerPosition);
            mExoPlayer.setPlayWhenReady(playWhenReady);
        }
    }

    public void setCurrentStepPosition(int position){
        currentStepPosition = position;
    }


    private void getLists(){
        if(!isTwoPane){
            ingredientList = getArguments().getParcelableArrayList(MainActivity.BUNDLE_INGREDIENT_LIST);
            stepList = getArguments().getParcelableArrayList(MainActivity.BUNDLE_STEP_LIST);
        } else {
            if(getActivity().getIntent()!=null) {
                //currentStepPosition = -1;
                ingredientList = getActivity().getIntent().getParcelableArrayListExtra(MainActivity.BUNDLE_INGREDIENT_LIST);
                stepList = getActivity().getIntent().getParcelableArrayListExtra(MainActivity.BUNDLE_STEP_LIST);
            }
        }
        Log.i(TAG ,"getLists ingredientList size : "+ ingredientList.size());
        Log.i(TAG ,"getLists stepList size : "+ stepList.size());

    }

    private void initializeRecyclerView(){
        GridLayoutManager layoutManager= new GridLayoutManager(getActivity(), 1);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mBinding.rvIngredients.setLayoutManager(layoutManager);
        mBinding.rvIngredients.setHasFixedSize(true);
        ingredientsAdapter = new IngredientsAdapter(getActivity(), new ArrayList<Ingredient>());
        mBinding.rvIngredients.setAdapter(ingredientsAdapter);
    }

    public void populateUI(int position) {
        // position = -1 in case of ingredients item is clicked
        Log.i(TAG ,"populateUI position: "+ position);
        currentStepPosition = position;
        if(position == -1){
            setIngredientsVisibility(true);
            populateIngredientList();
        } else {
            setIngredientsVisibility(false);
            populateStepInstructions(position);
        }

        setNextButtonStatus(currentStepPosition);
        setPreviousButtonStatus(currentStepPosition);

        if(isTwoPane){
            mBinding.buttonsLayout.setVisibility(View.GONE);
        }
    }



    private void setIngredientsVisibility(boolean isIngredientsVisible) {
        if(isIngredientsVisible){
            mBinding.rvIngredients.setVisibility(View.VISIBLE);
            mBinding.instructionsViews.setVisibility(View.GONE);
        } else {
            mBinding.rvIngredients.setVisibility(View.GONE);
            mBinding.instructionsViews.setVisibility(View.VISIBLE);
        }

    }

    private void populateIngredientList() {
        ingredientsAdapter = new IngredientsAdapter(getActivity(), ingredientList);
        mBinding.rvIngredients.setAdapter(ingredientsAdapter);
    }

    public void populateStepInstructions(int position) {
        Step step = stepList.get(position);

        String recipeImage = step.getThumbnailURL();
        String videoUrl = step.getVideoURL();
        String shortDescription = step.getShortDescription();
        String description = step.getDescription();
        boolean isLandscapeOrientation = getResources().getBoolean(R.bool.is_landscape);

        Log.i(TAG , "isLandscape :"+isLandscapeOrientation);
        Log.i(TAG , "videoUrl :"+videoUrl);
        Log.i(TAG , "recipeImage :"+recipeImage);

        if(isLandscapeOrientation && !isTwoPane && videoUrl != null && !TextUtils.isEmpty(videoUrl)){
            setPlayerVisibility(videoUrl, recipeImage);
            setPlayerFullScreen();
            return;
        }

        if(shortDescription!=null && !TextUtils.isEmpty(shortDescription)) {
            mBinding.shortDescription.setVisibility(View.VISIBLE);
            mBinding.shortDescription.setText(shortDescription);
        } else {
            mBinding.shortDescription.setVisibility(View.GONE);
        }


        if(description!=null && !TextUtils.isEmpty(description)) {
            mBinding.description.setVisibility(View.VISIBLE);
            mBinding.description.setText(description);
        } else {
            mBinding.description.setVisibility(View.GONE);
        }

        if (videoUrl != null && !TextUtils.isEmpty(videoUrl)) {
            setPlayerVisibility(videoUrl, recipeImage);
            mBinding.thumbnailImage.setVisibility(View.GONE);

        } else {
            mBinding.simplePlayer.setVisibility(View.GONE);
            if(recipeImage!=null && !TextUtils.isEmpty(recipeImage)) {
                mBinding.thumbnailImage.setVisibility(View.VISIBLE);
                Picasso.get().load(recipeImage).error(R.drawable.error).placeholder(R.drawable.placeholder)
                        .resizeDimen(R.dimen.poster_image_width, R.dimen.poster_image_height)
                        .onlyScaleDown().centerCrop().into(mBinding.thumbnailImage);
            } else {
                mBinding.thumbnailImage.setVisibility(View.GONE);
            }
        }
    }

    private void setPlayerVisibility(String videoUrl, String recipeImage) {
        mBinding.simplePlayer.setVisibility(View.VISIBLE);
        prepareExoPlayer(Uri.parse(videoUrl));
    }

    /*This method is copied from Google ExoPlayer Code Lab*/
    ///setPlayerFullScreen called in onResume is just an implementation detail to have a pure full screen experience:

    @SuppressLint("InlinedApi")
    private void setPlayerFullScreen() {
        mBinding.buttonsLayout.setVisibility(View.GONE);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(0,0,0,0);
        mBinding.simplePlayer.setLayoutParams(layoutParams);

        hideSystemUI();
        isFullScreenPlayer = true;
    }

    /*This method is copied from here:
    * https://developer.android.com/training/system-ui/immersive
    **/
    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE
        View decorView = getActivity().getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    /*This method is copied from here:
 * https://developer.android.com/training/system-ui/immersive
 **/
    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getActivity().getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }


    private void registerReceivers() {
        registerMediaButtonReceiver();
        registerNoisyAudioReceiver();
    }

    private void registerMediaButtonReceiver() {
        Log.i(TAG , "register media button receiver");
        IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonReceiver = new MediaButtonsActionsReceiver();
        getActivity().registerReceiver(mediaButtonReceiver, filter);
    }

    private void registerNoisyAudioReceiver() {
        Log.i(TAG ,"register audio noisy receiver");
        noisyAudioStreamReceiver = new NoisyAudioStreamReceiver();
        getActivity().registerReceiver(noisyAudioStreamReceiver,  new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
    }



    private void initializePlayer() {
        Log.i(TAG, "initialize ExoPlayer is null : " + mExoPlayer);

            if(mExoPlayer== null) {
                mExoPlayer = ExoPlayerFactory.newSimpleInstance(
                        new DefaultRenderersFactory(getActivity()),
                        new DefaultTrackSelector(),
                        new DefaultLoadControl());
                mExoPlayer.addListener(this);
                mBinding.simplePlayer.setPlayer(mExoPlayer);
                mExoPlayer.addListener(this);
            }
        }

    private void initializeMediaSession() {
        mMediaSession = new MediaSessionCompat(getActivity(),TAG);

        mMediaSession.setFlags( MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mMediaSession.setMediaButtonReceiver(null);

        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);

        mMediaSession.setPlaybackState(mStateBuilder.build());
        mMediaSession.setCallback(new MySessionCallback());
        mMediaSession.setActive(true);
    }

    private MediaSource buildMediaSource(Uri uri){
        String userAgent = Util.getUserAgent(getActivity(), getResources().getString(R.string.app_name));
        DataSource.Factory dataSourceFactory =
                new DefaultDataSourceFactory(getActivity(), userAgent , null);

        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        ExtractorMediaSource.Factory extractorMediaFactory =
                new ExtractorMediaSource.Factory(dataSourceFactory);
        extractorMediaFactory.setExtractorsFactory(extractorsFactory);
        MediaSource mediaSource =
                extractorMediaFactory.createMediaSource(uri);

        return mediaSource;
    }

    private void prepareExoPlayer(Uri uri){
        Log.i(TAG , "prepareExoPlayer: mPlayerPosition "+ mPlayerPosition);
        Log.i(TAG , "prepareExoPlayer: playWhenReady "+ playWhenReady);

        if(mMediaSession == null){
            initializeMediaSession();
        }
        if(mExoPlayer == null){
            initializePlayer();
        }

        MediaSource mediaSource = buildMediaSource(uri);
        mExoPlayer.prepare(mediaSource);
        mExoPlayer.setPlayWhenReady(false);
    }



    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if((playbackState == Player.STATE_READY) && playWhenReady){
            mStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                    mExoPlayer.getCurrentPosition(), 1f);

        } else if((playbackState == Player.STATE_READY)){
            mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                    mExoPlayer.getCurrentPosition(), 1f);
        }

        mMediaSession.setPlaybackState(mStateBuilder.build());

    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }


    private class MySessionCallback extends MediaSessionCompat.Callback {


        @Override
        public void onPlay() {
            mExoPlayer.setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            mExoPlayer.setPlayWhenReady(false);
        }

        @Override
        public void onSkipToPrevious() {
            mExoPlayer.seekTo(0);
        }
    }

    /*The following comment is copied from Google Code Labs [ExoPlayer Code Lab]*/
    /*
* Before API Level 24 there is no guarantee of onStop being called.
* So we have to release the player as early as possible in onPause.
* Starting with API Level 24 (which brought multi and split window mode) onStop is guaranteed to be called
* and in the paused mode our activity is eventually still visible.
* Hence we need to wait releasing until onStop.
* */
/*    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
            releaseMediaSession();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
            releaseMediaSession();
        }
    }*/


    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG , "onStop");
        unregisterReceivers();
        /// Restoring System UI if Player is on FullScreen mode
        // and user hit back button to return to StepsFragment
        if(isFullScreenPlayer) showSystemUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceivers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG , "onDestroy");
        releasePlayer();
        releaseMediaSession();

    }

    private void releasePlayer(){
        if(mExoPlayer != null) {
            mExoPlayer.stop();
            mExoPlayer.release();
        }
        mExoPlayer = null;
    }

    private void unregisterReceivers(){
        if (mediaButtonReceiver != null) {
            Log.i(TAG , "unregister media button receiver");
            getActivity().unregisterReceiver(mediaButtonReceiver);
        }

        if(noisyAudioStreamReceiver != null){
            Log.i(TAG , "unregister noisy Audio Receiver");
            getActivity().unregisterReceiver(noisyAudioStreamReceiver);
        }
    }

    private void releaseMediaSession() {
        if(mMediaSession != null){
            mMediaSession.setActive(false);
            mMediaSession.release();
        }
        mMediaSession = null;
    }

    ////////////////////////////// Broadcast Receivers //////////////////////////

    public static class MediaButtonsActionsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
                Log.i(TAG, "MediaButtonReceiver : onReceive"+ intent);
                MediaButtonReceiver.handleIntent(mMediaSession, intent);
            }
        }
    }

    public static class NoisyAudioStreamReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                // pause audio when headphones are unplugged
                mExoPlayer.setPlayWhenReady(false);
            }
        }
    }

    private View.OnClickListener mNextClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            currentStepPosition++;
            populateUI(currentStepPosition);
        }
    };

    private View.OnClickListener mPreviousClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            currentStepPosition--;
            populateUI(currentStepPosition);
        }
    };

    private void setNextButtonStatus(int position) {
        if(position >= stepList.size()-1){
            mBinding.btnNext.setEnabled(false);
        } else {
            mBinding.btnNext.setEnabled(true);
        }
    }

    private void setPreviousButtonStatus(int position) {
        if(position<=-1){
            mBinding.btnPrevious.setEnabled(false);
        } else {
            mBinding.btnPrevious.setEnabled(true);
        }
    }




}
