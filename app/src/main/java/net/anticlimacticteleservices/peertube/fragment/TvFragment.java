package net.anticlimacticteleservices.peertube.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.BackgroundManager;
import androidx.leanback.app.BrowseFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import net.anticlimacticteleservices.peertube.R;
import net.anticlimacticteleservices.peertube.activity.AccountActivity;
import net.anticlimacticteleservices.peertube.activity.ServerAddressBookActivity;
import net.anticlimacticteleservices.peertube.activity.SettingsActivity;
import net.anticlimacticteleservices.peertube.activity.VideoPlayActivity;
import net.anticlimacticteleservices.peertube.helper.APIUrlHelper;
import net.anticlimacticteleservices.peertube.model.LeanBackHeaderCategory;
import net.anticlimacticteleservices.peertube.model.Video;
import net.anticlimacticteleservices.peertube.model.VideoList;
import net.anticlimacticteleservices.peertube.network.GetUserService;
import net.anticlimacticteleservices.peertube.network.GetVideoDataService;
import net.anticlimacticteleservices.peertube.network.RetrofitInstance;
import net.anticlimacticteleservices.peertube.network.Session;
import net.anticlimacticteleservices.peertube.presenter.CardPresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static net.anticlimacticteleservices.peertube.activity.VideoListActivity.EXTRA_VIDEOID;
import static net.anticlimacticteleservices.peertube.activity.VideoListActivity.SWITCH_INSTANCE;

public class TvFragment extends BrowseFragment {
    private static final String TAG = "MainFragment";

    private static final int BACKGROUND_UPDATE_DELAY = 300;
    private static final int GRID_ITEM_WIDTH = 200;
    private static final int GRID_ITEM_HEIGHT = 200;
    private static final int NUM_ROWS = 1;
    private static final int NUM_COLS = 15;

    private final Handler mHandler = new Handler();
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private Timer mBackgroundTimer;
    private String mBackgroundUri;
    private BackgroundManager mBackgroundManager;
    private List<Video> list;
    private List<Video> localVideos;
    private List<Video> TrendingVideos;
    private List<Video> likedVideos;
    private List<Video> recentVideos;
    private List<Video> subscriptionVideos;
    private List<Video> myVideos;
    private List<Video> History;
    private int currentStart = 0;
    private int count = 12;
    private String sort = "-createdAt";
    private String filter = null;
    private String searchQuery = "";
    private Boolean subscriptions = false;
    private ArrayList<LeanBackHeaderCategory> ui;
    private TextView emptyView;
    private RecyclerView recyclerView;
    LeanBackHeaderCategory head;
    private int isLoading = 0;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "getting videos");
        initVideos();
        Log.i(TAG, "preparing background manager");
        prepareBackgroundManager();
        Log.i(TAG, "setting up ui elements");
        setupUIElements();
        Log.i(TAG, "loading rows");
        loadRows();
        Log.i(TAG, "setting listeners");
        setupEventListeners();
        Log.i(TAG, "done with create");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mBackgroundTimer) {
            Log.d(TAG, "onDestroy: " + mBackgroundTimer.toString());
            mBackgroundTimer.cancel();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initVideos() {
        ui=new ArrayList<LeanBackHeaderCategory>();
        head = new LeanBackHeaderCategory("Chronological");
        head.setLoading(true);
        pullVideos(head,0,10,"-createdAt",null);
        ui.add(head);

        head = new LeanBackHeaderCategory("Local");
        head.setLoading(true);
        pullVideos(head,0,10,"-createdAt","local");
        ui.add(head);


        head = new LeanBackHeaderCategory("Trending");
        head.setLoading(true);
        pullVideos(head,0,10,"-trending",null);
        ui.add(head);


        head = new LeanBackHeaderCategory("Most Viewed");
        head.setLoading(true);
        pullVideos(head,0,10,"-views",null);
        ui.add(head);
        head = new LeanBackHeaderCategory("Most Liked");
        head.setLoading(true);
        pullVideos(head,0,10,"-likes",null);
        ui.add(head);

    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void loadRows() {

        ArrayObjectAdapter rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        CardPresenter cardPresenter = new CardPresenter();

        for ( LeanBackHeaderCategory head : ui){
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
            Log.e("WTF", head.getName()+" "+head.getVideos().size());
            if (head.getVideos().size()>0) {
                for (Video item : head.getVideos()) {
                    listRowAdapter.add(item);
                }
                HeaderItem headerItem = new HeaderItem(head.getName());
                rowsAdapter.add(new ListRow(headerItem, listRowAdapter));
            }
        }

        HeaderItem gridHeader = new HeaderItem(2, "PREFERENCES");

        GridItemPresenter mGridPresenter = new GridItemPresenter();
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);
        gridRowAdapter.add("Servers");
        gridRowAdapter.add("Settings");
        gridRowAdapter.add("Account");
        gridRowAdapter.add("Refresh");
        rowsAdapter.add(new ListRow(gridHeader, gridRowAdapter));

        setAdapter(rowsAdapter);
        if (Session.getInstance().isLoggedIn() && !subscriptions) {

            subscriptions=true;
            head = new LeanBackHeaderCategory("Subscribed");
            head.setLoading(true);
            String apiBaseURL = APIUrlHelper.getUrlWithVersion(getContext());
            GetUserService userService = RetrofitInstance.getRetrofitInstance(apiBaseURL).create(GetUserService.class);
            Call<VideoList> call;
            call = userService.getVideosSubscripions(0,50, "-createdAt");
            Log.d("WTF","subscription URL Called"+ call.request().url() + "");
            call.enqueue(new Callback<VideoList>() {
                @Override
                public void onResponse(@NonNull Call<VideoList> call, @NonNull Response<VideoList> response) {
                    if (response.body() != null) {
                        ArrayList<Video> videoList = response.body().getVideoArrayList();
                        if (videoList != null) {
                            Log.e("wtf", head.getName()+"subscription adding "+videoList.size());
                            head.addAllVideo(videoList);
                            loadRows();
                        }
                    }
                    Log.e("WTF","subscripotions failed to load");
                    head.setLoading(false);
                }
                @Override
                public void onFailure(Call<VideoList> call, Throwable t) {
                }
            });
            ui.add(0,head);
           // ui.add(head);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void prepareBackgroundManager() {

        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());

        mDefaultBackground = ContextCompat.getDrawable(getContext(), R.drawable.lb_background);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setupUIElements() {
        // setBadgeDrawable(getActivity().getResources().getDrawable(
        // R.drawable.videos_by_google_banner));

        setTitle("PeerTubeTV"); // Badge, when set, takes precedent
        // over title
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);

        // set fastLane (or headers) background color
        setBrandColor(ContextCompat.getColor(getContext(), R.color.lb_default_brand_color));
        // set search icon color
        setSearchAffordanceColor(ContextCompat.getColor(getContext(), R.color.lb_default_search_color));
    }

    private void setupEventListeners() {
        setOnSearchClickedListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Implement your own in-app search", Toast.LENGTH_LONG)
                        .show();
            }
        });

        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }

    private void updateBackground(String uri) {
        int width = mMetrics.widthPixels;
        int height = mMetrics.heightPixels;
        Glide.with(getActivity())
                .load(uri)
                .centerCrop()
                .error(mDefaultBackground)
                .into(new SimpleTarget<GlideDrawable>(width, height) {
                    @Override
                    public void onResourceReady(GlideDrawable resource,
                                                GlideAnimation<? super GlideDrawable>
                                                        glideAnimation) {
                        mBackgroundManager.setDrawable(resource);
                    }
                });
        mBackgroundTimer.cancel();
    }

    private void startBackgroundTimer() {
        if (null != mBackgroundTimer) {
            mBackgroundTimer.cancel();
        }
        mBackgroundTimer = new Timer();
        mBackgroundTimer.schedule(new UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY);
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Video) {
                Video movie = (Video) item;
                Log.d(TAG, "Item: " + item.toString());

                Intent intent = new Intent(getContext(), VideoPlayActivity.class);
                intent.putExtra(EXTRA_VIDEOID, movie.getUuid());
                getContext().startActivity(intent);

             } else if (item instanceof String) {
                Toast.makeText(getActivity(), ((String) item), Toast.LENGTH_SHORT).show();
                if (item.equals("Settings")){
                    Intent settingsActivityIntent = new Intent(getActivity(), SettingsActivity.class);
                    startActivityForResult(settingsActivityIntent, SWITCH_INSTANCE);
                }
                if (item.equals("Servers")){
                    Intent addressBookActivityIntent = new Intent(getActivity(), ServerAddressBookActivity.class);
                    startActivityForResult(addressBookActivityIntent, SWITCH_INSTANCE);
                }
                if (item.equals("Account")){
                    Intent accountActivityIntent = new Intent(getActivity(), AccountActivity.class);
                    startActivityForResult(accountActivityIntent, SWITCH_INSTANCE);
                }
                if (item.equals("Refresh")){
                    ui=new ArrayList<LeanBackHeaderCategory>();
                    subscriptions=false;
                   /* Intent intent = new Intent(getContext(), TvActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    getContext().startActivity(intent);
                    Runtime.getRuntime().exit(0);
                    */
                    initVideos();
                }
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(
                Presenter.ViewHolder itemViewHolder,
                Object item,
                RowPresenter.ViewHolder rowViewHolder,
                Row row) {
            if (item instanceof Video) {
                mBackgroundUri = ((Video) item).getThumbnailPath();
                startBackgroundTimer();
            }
        }
    }

    private class UpdateBackgroundTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateBackground(mBackgroundUri);
                }
            });
        }
    }

    private class GridItemPresenter extends Presenter {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            TextView view = new TextView(parent.getContext());
            view.setLayoutParams(new ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT));
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.setBackgroundColor(
                    ContextCompat.getColor(getContext(), R.color.lb_default_brand_color));
            view.setTextColor(Color.WHITE);
            view.setGravity(Gravity.CENTER);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            ((TextView) viewHolder.view).setText((String) item);
        }

        @Override
        public void onUnbindViewHolder(ViewHolder viewHolder) {
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void pullVideos(LeanBackHeaderCategory header, int start, int count, String sort, String filter) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String nsfw = sharedPref.getBoolean(getString(R.string.pref_show_nsfw_key), false) ? "both" : "false";
        Set<String> languages = sharedPref.getStringSet(getString(R.string.pref_video_language_key), null);
        String apiBaseURL = APIUrlHelper.getUrlWithVersion(getContext());
        GetVideoDataService service = RetrofitInstance.getRetrofitInstance(apiBaseURL).create(GetVideoDataService.class);

        Call<VideoList> call;
        call = service.getVideosData(0, count, sort, nsfw, filter, languages);
        /*Log the URL called*/
        Log.d("URL Called", call.request().url() + "");
   //     Toast.makeText(getContext(), "URL Called: " + call.request().url(), Toast.LENGTH_SHORT).show();

        call.enqueue(new Callback<VideoList>() {
            @Override
            public void onResponse(@NonNull Call<VideoList> call, @NonNull Response<VideoList> response) {
                if (response.body() != null) {
                    ArrayList<Video> videoList = response.body().getVideoArrayList();
                    header.setLoading(false);
                    if (videoList != null) {
                        Log.e("wtf", header.getName()+" adding "+videoList.size());
                        header.addAllVideo(videoList);
                        loadRows();
                    }
                    if (header.getVideos().size()<30){
                        switch (header.getName()) {
                            case "Chronological":
                                header.setLoading(true);
                                pullVideos(header, 11, 40, "-createdAt", null);
                                break;
                            case "Local":
                                header.setLoading(true);
                                pullVideos(header, 11, 40, "-createdAt", "local");
                                break;
                            case "Trending":
                                header.setLoading(true);
                                pullVideos(header, 11, 40, "-trending", null);
                                break;
                            case "Most Viewed":
                                header.setLoading(true);
                                pullVideos(header, 11, 40, "-views", null);
                                break;
                            case "Most Liked":
                                header.setLoading(true);
                                pullVideos(header, 11, 40, "-liked", null);
                                break;
                        }
                    }

                } else {
                    Log.e(TAG, "null response");
                }
            }
            @Override
            public void onFailure(@NonNull Call<VideoList> call, @NonNull Throwable t) {
                Log.wtf("err", t.fillInStackTrace());
            }
        });
    }

}