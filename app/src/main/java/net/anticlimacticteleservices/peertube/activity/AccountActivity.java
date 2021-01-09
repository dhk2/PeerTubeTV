/*
 * Copyright 2018 Stefan Schüller <sschueller@techdroid.com>
 *
 * License: GPL-3.0+
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package net.anticlimacticteleservices.peertube.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.squareup.picasso.Picasso;

import net.anticlimacticteleservices.peertube.R;
import net.anticlimacticteleservices.peertube.adapter.ChannelAdapter;
import net.anticlimacticteleservices.peertube.adapter.VideoAdapter;
import net.anticlimacticteleservices.peertube.helper.APIUrlHelper;
import net.anticlimacticteleservices.peertube.helper.ErrorHelper;
import net.anticlimacticteleservices.peertube.helper.MetaDataHelper;
import net.anticlimacticteleservices.peertube.model.Account;
import net.anticlimacticteleservices.peertube.model.Avatar;
import net.anticlimacticteleservices.peertube.model.ChannelList;
import net.anticlimacticteleservices.peertube.model.VideoList;
import net.anticlimacticteleservices.peertube.network.GetUserService;
import net.anticlimacticteleservices.peertube.network.GetVideoDataService;
import net.anticlimacticteleservices.peertube.network.RetrofitInstance;


import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AccountActivity extends CommonActivity {

    private String TAG = "AccountActivity";
    private String apiBaseURL;

    private Integer videosStart, videosCount, videosCurrentStart;
    private String videosFilter, videosSort, videosNsfw;
    private Set<String> videosLanguages;

    private ChannelAdapter channelAdapter;
    private VideoAdapter videoAdapter;

    private RecyclerView recyclerViewVideos;
    private RecyclerView recyclerViewChannels;

    private SwipeRefreshLayout swipeRefreshLayoutVideos;
    private SwipeRefreshLayout swipeRefreshLayoutChannels;
    private CoordinatorLayout aboutView;
    //private TextView emptyView;

    private Boolean isLoadingVideos;

    private GetUserService userService;

    private String displayNameAndHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_account);

        apiBaseURL = APIUrlHelper.getUrlWithVersion(this);

        userService = RetrofitInstance.getRetrofitInstance(apiBaseURL).create(GetUserService.class);

        recyclerViewVideos = findViewById(R.id.account_video_recyclerView);
        recyclerViewChannels = findViewById(R.id.account_channel_recyclerView);

        swipeRefreshLayoutVideos = findViewById(R.id.account_swipeRefreshLayout_videos);
        swipeRefreshLayoutChannels = findViewById(R.id.account_swipeRefreshLayout_channels);
        aboutView = findViewById(R.id.account_about);

        RecyclerView.LayoutManager layoutManagerVideos = new LinearLayoutManager(AccountActivity.this);
        recyclerViewVideos.setLayoutManager(layoutManagerVideos);

        RecyclerView.LayoutManager layoutManagerVideosChannels = new LinearLayoutManager(AccountActivity.this);
        recyclerViewChannels.setLayoutManager(layoutManagerVideosChannels);

        videoAdapter = new VideoAdapter(new ArrayList<>(), AccountActivity.this);
        recyclerViewVideos.setAdapter(videoAdapter);

        channelAdapter = new ChannelAdapter(new ArrayList<>(), AccountActivity.this);
        recyclerViewChannels.setAdapter(channelAdapter);


        swipeRefreshLayoutVideos.setOnRefreshListener(() -> {
            // Refresh items
            if (!isLoadingVideos) {
                videosCurrentStart = 0;
                loadAccountVideos(displayNameAndHost);
            }
        });

        // get video ID
        Intent intent = getIntent();
        displayNameAndHost = intent.getStringExtra(VideoListActivity.EXTRA_ACCOUNTDISPLAYNAME);
        Log.v(TAG, "click: " + displayNameAndHost);


        createBottomBarNavigation();

        videosStart = 0;
        videosCount = 25;
        videosCurrentStart = 0;
        videosFilter = "";
        videosSort = "-publishedAt";
        videosNsfw = "";


        // Attaching the layout to the toolbar object
        Toolbar toolbar = findViewById(R.id.tool_bar_account);
        // Setting toolbar as the ActionBar with setSupportActionBar() call
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);
        getSupportActionBar().setTitle(displayNameAndHost);

        loadAccountVideos(displayNameAndHost);

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // close this activity as oppose to navigating up

        return false;
    }

    private void loadAccount(String ownerString) {

        // get video details from api
        Call<Account> call = userService.getAccount(ownerString);

        call.enqueue(new Callback<Account>() {
            @Override
            public void onResponse(@NonNull Call<Account> call, @NonNull Response<Account> response) {


                if (response.isSuccessful()) {
                    Account account = response.body();

                    String owner = MetaDataHelper.getOwnerString(account.getName(),
                            account.getHost(),
                            AccountActivity.this
                    );

                    // set view data
                    TextView ownerStringView = findViewById(R.id.account_owner_string);
                    ownerStringView.setText(owner);

                    TextView followers = findViewById(R.id.account_followers);
                    followers.setText(account.getFollowersCount().toString());

                    TextView description = findViewById(R.id.account_description);
                    description.setText(account.getDescription());

                    TextView joined = findViewById(R.id.account_joined);
                    joined.setText(account.getCreatedAt().toString());


                    ImageView accountAvatar = findViewById(R.id.account_avatar);

                    // set Avatar
                    Avatar avatar = account.getAvatar();
                    if (avatar != null) {
                        String avatarPath = avatar.getPath();
                        Picasso.get()
                                .load(APIUrlHelper.getUrl(AccountActivity.this) + avatarPath)
                                .into(accountAvatar);
                    }

                } else {
                    ErrorHelper.showToastFromCommunicationError( AccountActivity.this, null );
                }


            }

            @Override
            public void onFailure(@NonNull Call<Account> call, @NonNull Throwable t) {
                Log.wtf(TAG, t.fillInStackTrace());
                ErrorHelper.showToastFromCommunicationError( AccountActivity.this, t );
            }
        });

    }


    private void loadAccountVideos(String displayNameAndHost) {

        isLoadingVideos = false;

        GetVideoDataService service = RetrofitInstance.getRetrofitInstance(apiBaseURL).create(GetVideoDataService.class);
        Call<VideoList> call;

        call = service.getAccountVideosData(displayNameAndHost, videosStart, videosCount, videosSort);

        call.enqueue(new Callback<VideoList>() {
            @Override
            public void onResponse(@NonNull Call<VideoList> call, @NonNull Response<VideoList> response) {

                Log.v(TAG, response.toString());

                if (response.isSuccessful()) {
                    if (videosCurrentStart == 0) {
                        videoAdapter.clearData();
                    }

                    if (response.body() != null) {
                        videoAdapter.setData(response.body().getVideoArrayList());
                    }

                } else{
                    ErrorHelper.showToastFromCommunicationError( AccountActivity.this, null );
                }

                isLoadingVideos = false;
                swipeRefreshLayoutVideos.setRefreshing(false);
            }

            @Override
            public void onFailure(@NonNull Call<VideoList> call, @NonNull Throwable t) {
                Log.wtf("err", t.fillInStackTrace());
                ErrorHelper.showToastFromCommunicationError( AccountActivity.this, t );
                isLoadingVideos = false;
                swipeRefreshLayoutVideos.setRefreshing(false);
            }
        });
    }

    private void loadAccountChannels(String displayNameAndHost) {

        // get video details from api
        Call<ChannelList> call = userService.getAccountChannels(displayNameAndHost);

        call.enqueue(new Callback<ChannelList>() {
            @Override
            public void onResponse(@NonNull Call<ChannelList> call, @NonNull Response<ChannelList> response) {


                if (response.isSuccessful()) {
                    ChannelList channelList = response.body();



                } else {
                    ErrorHelper.showToastFromCommunicationError( AccountActivity.this, null );
                }


            }

            @Override
            public void onFailure(@NonNull Call<ChannelList> call, @NonNull Throwable t) {
                Log.wtf(TAG, t.fillInStackTrace());
                ErrorHelper.showToastFromCommunicationError( AccountActivity.this, t );
            }
        });
    }



    private void createBottomBarNavigation() {

        // Get Bottom Navigation
        BottomNavigationView navigation = findViewById(R.id.account_navigation);

        // Always show text label
        navigation.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);

        // Add Icon font
        Menu navMenu = navigation.getMenu();
        navMenu.findItem(R.id.account_navigation_about).setIcon(
                new IconicsDrawable(this, FontAwesome.Icon.faw_user));
        navMenu.findItem(R.id.account_navigation_channels).setIcon(
                new IconicsDrawable(this, FontAwesome.Icon.faw_list));
        navMenu.findItem(R.id.account_navigation_videos).setIcon(
                new IconicsDrawable(this, FontAwesome.Icon.faw_video));

        // Click Listener
        navigation.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.account_navigation_about:

                    swipeRefreshLayoutVideos.setVisibility(View.GONE);
                    swipeRefreshLayoutChannels.setVisibility(View.GONE);
                    aboutView.setVisibility(View.VISIBLE);
                    loadAccount(displayNameAndHost);

                    return true;
                case R.id.account_navigation_channels:

                    swipeRefreshLayoutVideos.setVisibility(View.GONE);
                    swipeRefreshLayoutChannels.setVisibility(View.VISIBLE);
                    aboutView.setVisibility(View.GONE);
                    loadAccountChannels(displayNameAndHost);

                    return true;
                case R.id.account_navigation_videos:

                    swipeRefreshLayoutVideos.setVisibility(View.VISIBLE);
                    swipeRefreshLayoutChannels.setVisibility(View.GONE);
                    aboutView.setVisibility(View.GONE);
                    loadAccountVideos(displayNameAndHost);

                    return true;

            }
            return false;
        });

    }
}
