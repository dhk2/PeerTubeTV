package net.anticlimacticteleservices.peertube.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.anticlimacticteleservices.peertube.R;
import net.anticlimacticteleservices.peertube.activity.AccountActivity;
import net.anticlimacticteleservices.peertube.activity.ServerAddressBookActivity;
import net.anticlimacticteleservices.peertube.activity.SettingsActivity;
import net.anticlimacticteleservices.peertube.activity.TvActivity;
import net.anticlimacticteleservices.peertube.activity.VideoPlayActivity;
import net.anticlimacticteleservices.peertube.database.Server;
import net.anticlimacticteleservices.peertube.database.ServerDao;
import net.anticlimacticteleservices.peertube.database.ServerRoomDatabase;
import net.anticlimacticteleservices.peertube.database.ServerViewModel;
import net.anticlimacticteleservices.peertube.helper.APIUrlHelper;
import net.anticlimacticteleservices.peertube.helper.ErrorHelper;
import net.anticlimacticteleservices.peertube.model.LeanBackHeaderCategory;
import net.anticlimacticteleservices.peertube.model.ServerList;
import net.anticlimacticteleservices.peertube.model.Video;
import net.anticlimacticteleservices.peertube.model.VideoList;
import net.anticlimacticteleservices.peertube.network.GetUserService;
import net.anticlimacticteleservices.peertube.network.GetVideoDataService;
import net.anticlimacticteleservices.peertube.network.RetrofitInstance;
import net.anticlimacticteleservices.peertube.network.Session;
import net.anticlimacticteleservices.peertube.presenter.CardPresenter;
import net.anticlimacticteleservices.peertube.service.LoginService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static net.anticlimacticteleservices.peertube.R.color.lb_action_text_color;
import static net.anticlimacticteleservices.peertube.R.color.lb_basic_card_bg_color;
import static net.anticlimacticteleservices.peertube.R.color.lb_basic_card_content_text_color;
import static net.anticlimacticteleservices.peertube.R.color.lb_basic_card_title_text_color;
import static net.anticlimacticteleservices.peertube.R.color.lb_tv_white;
import static net.anticlimacticteleservices.peertube.activity.VideoListActivity.EXTRA_VIDEOID;
import static net.anticlimacticteleservices.peertube.activity.VideoListActivity.SWITCH_INSTANCE;

public class TvFragment extends BrowseFragment {
    private static final String TAG = "MainFragment";

    private static final int BACKGROUND_UPDATE_DELAY = 300;
    private static final int GRID_ITEM_WIDTH = 200;
    private static final int GRID_ITEM_HEIGHT = 200;
    private static final int NUM_ROWS = 1;
    private static final int NUM_COLS = 15;
    private static Server editServer;

    private final Handler mHandler = new Handler();
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private Timer mBackgroundTimer;
    private String mBackgroundUri;
    private BackgroundManager mBackgroundManager;

    private int currentStart = 0;
    private int count = 12;
    private String sort = "-createdAt";
    private String filter = null;
    private String searchQuery = "";
    private Boolean subscriptions = false;
    private ArrayList<LeanBackHeaderCategory> ui;
    private List<net.anticlimacticteleservices.peertube.database.Server> allServers;
    private String currentRow;
    private Video currentVideo;
    private long currentRowNumber;
    private LeanBackHeaderCategory head;
    private int isLoading = 0;
    private  boolean drawWhenLoaded=true;
    private String apiBaseURL="";
    private String currentServer="";

    private ServerViewModel mServerViewModel;
    private AddServerFragment addServerFragment;
    private FloatingActionButton floatingActionButton;
    private FragmentManager fragmentManager;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "getting videos");
        initVideos();
        Log.i(TAG, "getting servers");
        initServers();
        Log.i(TAG, "preparing background manager");
        prepareBackgroundManager();
        Log.i(TAG, "setting up ui elements");
        setupUIElements();
        Log.i(TAG, "loading rows");
        //loadRows();
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
    private void initServers(){

        ServerRoomDatabase db = ServerRoomDatabase.getDatabase(getContext());
        ServerDao mServerDao = db.serverDao();
        currentServer = APIUrlHelper.getUrl(getContext());
        allServers = mServerDao.getDeadServers();
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initVideos() {
        ui=new ArrayList<LeanBackHeaderCategory>();
        currentRow=null;
        currentVideo=null;
        currentRowNumber=0;
        head = new LeanBackHeaderCategory("Chronological");
        head.setLoading(true);
        head.setAdapterIndex(ui.size());
        pullVideos(head,0,10,"-createdAt",null);

        ui.add(head);

        head = new LeanBackHeaderCategory("Local");
        head.setLoading(true);
        head.setAdapterIndex(ui.size());
        pullVideos(head,0,10,"-createdAt","local");
        ui.add(head);


        head = new LeanBackHeaderCategory("Trending");
        head.setLoading(true);
        head.setAdapterIndex(ui.size());
        pullVideos(head,0,10,"-trending",null);
        ui.add(head);


        head = new LeanBackHeaderCategory("Most Viewed");
        head.setLoading(true);
        head.setAdapterIndex(ui.size());
        pullVideos(head,0,10,"-views",null);
        ui.add(head);

        head = new LeanBackHeaderCategory("Most Liked");
        head.setLoading(true);
        head.setAdapterIndex(ui.size());
        pullVideos(head,0,10,"-likes",null);
        ui.add(head);
        loadRows();
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void loadRows() {
        int selected=-1;
        Log.e("wtf","server base url"+apiBaseURL);
        ArrayObjectAdapter rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        CardPresenter cardPresenter = new CardPresenter();

        for ( LeanBackHeaderCategory head : ui){
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);
            if (head.getVideos().size()>0) {
                for (Video item : head.getVideos()) {
                    listRowAdapter.add(item);
                }
                HeaderItem headerItem = new HeaderItem(head.getName());
                ListRow toAdd = new ListRow(headerItem, listRowAdapter);
                rowsAdapter.add(toAdd);
                head.setAdapterIndex(rowsAdapter.indexOf(toAdd));
                Log.e("wth",head.getAdapterIndex()+" should be "+head.getName());

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

        gridHeader = new HeaderItem("Servers");
        mGridPresenter = new GridItemPresenter();
        gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);
        if (null != allServers){
            for (Server knownServer:allServers){
                gridRowAdapter.add(knownServer);
            }
        }
        gridRowAdapter.add("Add Server");
        rowsAdapter.add(new ListRow(gridHeader, gridRowAdapter));
        setAdapter(rowsAdapter);
        if (Session.getInstance().isLoggedIn() && !subscriptions) {
            drawWhenLoaded=true;
            getHistoryAndSubscriptions();
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
         setBadgeDrawable(getActivity().getResources().getDrawable(
         R.mipmap.banner2));

        //setTitle("PeerTubeTV"); // Badge, when set, takes precedent
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

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Search Videos");

// Set up the input
                final EditText input = new EditText(getContext());
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
                builder.setView(input);

// Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        searchVideos(input.getText().toString(),0,50,"-createdAt",null);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
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

            } else if (item instanceof Server){
                Server server = (Server)item;
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = sharedPref.edit();
                if (server.getServerHost().equals(currentServer)){
                    //selected current server, open edit server dialog.
                    editServer=server;
                    Intent addressBookActivityIntent = new Intent(getActivity(), ServerAddressBookActivity.class);
                    startActivityForResult(addressBookActivityIntent, SWITCH_INSTANCE);

                } else {
                    //change to selected server
                    String serverUrl = APIUrlHelper.cleanServerUrl(server.getServerHost());
                    Log.e("WTF", serverUrl + " == " + currentServer);
                    editor.putString(getContext().getString(R.string.pref_api_base_key), serverUrl);
                    editor.apply();

                    // Logout if logged in
                    Session session = Session.getInstance();
                    if (session.isLoggedIn()) {
                        session.invalidate();
                    }

                    // attempt authentication if we have a username
                    if (!TextUtils.isEmpty(server.getUsername())) {
                        LoginService.Authenticate(
                                server.getUsername(),
                                server.getPassword()
                        );
                    }
                    //redraw interface with new server info
                    ui = new ArrayList<LeanBackHeaderCategory>();
                    subscriptions = false;
                    Toast.makeText(getContext(), getContext().getString(R.string.server_selection_set_server, serverUrl), Toast.LENGTH_LONG).show();
                    currentServer=serverUrl;
                    initVideos();
                    drawWhenLoaded = true;
                    getActivity().getFragmentManager().popBackStack();
                }
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
                    Intent intent = new Intent(getContext(), TvActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    getContext().startActivity(intent);
                    Runtime.getRuntime().exit(0);
                }
                if (item.equals("Add Server")){
                    editServer=null;
                    Intent addressBookActivityIntent = new Intent(getActivity(), ServerAddressBookActivity.class);
                    startActivityForResult(addressBookActivityIntent, SWITCH_INSTANCE);
                }

            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onItemSelected(
                Presenter.ViewHolder itemViewHolder,
                Object item,
                RowPresenter.ViewHolder rowViewHolder,
                Row row) {
            Log.e("wtf","on item selected");
            if (Session.getInstance().isLoggedIn() && !subscriptions) {
                drawWhenLoaded=true;
                getHistoryAndSubscriptions();
            }
            if (item instanceof Video) {
                currentRow= row.getHeaderItem().getName();
                currentVideo = ((Video) item);
                for (LeanBackHeaderCategory lbh:ui){
                    if (lbh.getName().equals(currentRow)){
                        currentRowNumber=ui.indexOf(lbh);
                    }
                }
                row.getHeaderItem().getName();
                try {
                    mBackgroundUri = currentVideo.getAccount().getHost() + currentVideo.getThumbnailPath();
                }
                catch(NullPointerException e)
                {
                    System.out.print("NullPointerException Caught");
                }
                int rowPosition=0;
                int rowSize=0;
                for (LeanBackHeaderCategory test:ui){
                    if ((!test.isLoading()) && (test.getName().equals(currentRow))){
                        rowPosition = test.getVideos().indexOf((Video)item);
                        rowSize = test.getVideos().size();
                        Log.e("WTF",rowPosition+" of "+rowSize+ "current row "+currentRowNumber+"  "+currentVideo.getName());
                        if ((rowSize-rowPosition)<10){
                            switch (test.getName()) {
                                case "Chronological":
                                    test.setLoading(true);
                                    pullVideos(test, rowSize+1, 40, "-createdAt", null);
                                    break;
                                case "Local":
                                    test.setLoading(true);
                                    pullVideos(test, rowSize+1, 40, "-createdAt", "local");
                                    break;
                                case "Trending":
                                    test.setLoading(true);
                                    pullVideos(test, rowSize+1, 40, "-trending", null);
                                    break;
                                case "Most Viewed":
                                    test.setLoading(true);
                                    pullVideos(test, rowSize+1, 40, "-views", null);
                                    break;
                                case "Most Liked":
                                    test.setLoading(true);
                                    pullVideos(test, rowSize+1, 40, "-likes", null);
                                    break;
                                case "Subscriptions":
                                    test.setLoading(true);
                                    pullVideos(test,rowSize+1,40,"Subscriptions",null);
                                    break;
                                case "History":
                                    test.setLoading(true);
                                    pullVideos(test,rowSize+1,40,"History",null);
                                    break;
                            }
                        }
                    }
                }
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

        @RequiresApi(api = Build.VERSION_CODES.M)
        @SuppressLint("ResourceAsColor")
        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item) {

            if (item instanceof Video) {
                ((TextView) viewHolder.view).setText(((Video) item).getName());
            } else if (item instanceof Server){
                Server server =(Server)item;
                if (server.getUsername().isEmpty()) {
                    ((TextView) viewHolder.view).setText(server.getServerName());
                } else {
                    ((TextView) viewHolder.view).setText(server.getUsername()+"\n@\n"+ server.getServerName());
                }
                if (server.getServerHost().equals(currentServer)){
                    ((TextView) viewHolder.view).setTextSize((float) (((TextView) viewHolder.view).getTextSize()*(1.2)));
                    ((TextView) viewHolder.view).setBackgroundColor(lb_basic_card_content_text_color);
                }
            }
            else {
                ((TextView) viewHolder.view).setText((String) item);
            }
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
        apiBaseURL = APIUrlHelper.getUrlWithVersion(getContext());
        GetVideoDataService service = RetrofitInstance.getRetrofitInstance(apiBaseURL).create(GetVideoDataService.class);
        GetUserService userService = RetrofitInstance.getRetrofitInstance(apiBaseURL).create(GetUserService.class);
        Call<VideoList> call;
        if (sort.equals("Subscriptions")){
            call = userService.getVideosSubscripions(start,count, "-createdAt");
        } else if (sort.equals("History")){
            call = userService.getVideosHistory(start,count, null);
        }else {
            call = service.getVideosData(start, count, sort, nsfw, filter, languages);
        }
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
                        Log.e("wth", header.getName()+" adding "+videoList.size()+" to "+header.getName()+" at index "+header.getAdapterIndex());
                        header.addAllVideo(videoList);
                        if (getAdapter().size()>header.getAdapterIndex()) {
                            ListRow listRow = (ListRow) getAdapter().get(header.getAdapterIndex());
                            Log.e("WTH", listRow.getHeaderItem().getName() + " list row should be " + header.getName());
                            ArrayObjectAdapter listRowAdapter = (ArrayObjectAdapter) listRow.getAdapter();
                            Log.e("wth", String.valueOf(listRowAdapter.size()));
                            for (Video toAdd : videoList) {
                                listRowAdapter.add(toAdd);
                            }
                        }
                        if (drawWhenLoaded) {
                            boolean allLoaded=true;
                            for (LeanBackHeaderCategory head : ui) {
                                if (head.isLoading()){
                                    allLoaded=false;
                                }
                            }
                            if (allLoaded){
                                drawWhenLoaded=false;
                                loadRows();
                            }
                        }
                    }


                } else {
                    Log.e(TAG, "null response");
                }
            }
            @Override
            public void onFailure(@NonNull Call<VideoList> call, @NonNull Throwable t) {
                Log.wtf("err", t.fillInStackTrace());
                ErrorHelper.showToastFromCommunicationError( getContext(), t );
            }
        });
    }
    public void setTitle(String title){
        setTitle(title);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void updateRows() {
        if (drawWhenLoaded){
            loadRows();
        }
        ArrayObjectAdapter rowsAdapter = (ArrayObjectAdapter) this.getAdapter();
        CardPresenter cardPresenter = new CardPresenter();

        for ( LeanBackHeaderCategory head : ui){
           ListRow listRow = (ListRow) getAdapter().get(head.getAdapterIndex());
           ArrayObjectAdapter listRowAdapter = (ArrayObjectAdapter) listRow.getAdapter();
            Log.e("WTF", head.getName()+" "+head.getVideos().size()+" "+head.getAdapterIndex()+"  "+listRow.getAdapter().size());
            if (head.getVideos().size()>listRow.getAdapter().size()) {
                for (Video item : head.getVideos()) {
                    listRowAdapter.add(item);
                }
            }
        }
/*        if (selected>0){
            this.setSelectedPosition((int) currentRowNumber, false, new ListRowPresenter.SelectItemViewHolderTask(selected));
        }
*/
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void searchVideos(String searchQuery, int start, int count, String sort, String filter) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String nsfw = sharedPref.getBoolean(getString(R.string.pref_show_nsfw_key), false) ? "both" : "false";
        Set<String> languages = sharedPref.getStringSet(getString(R.string.pref_video_language_key), null);
        String apiBaseURL = APIUrlHelper.getUrlWithVersion(getContext());

        GetVideoDataService service = RetrofitInstance.getRetrofitInstance(apiBaseURL).create(GetVideoDataService.class);

        Call<VideoList> call;
        if (!searchQuery.equals("")) {
            call = service.searchVideosData(start, count, sort, nsfw, searchQuery, filter, languages);
        }else {
            call = service.getVideosData(start, count, sort, nsfw, filter, languages);
        }

        /*Log the URL called*/
        Log.d("URL Called", call.request().url() + "");
//        Toast.makeText(VideoListActivity.this, "URL Called: " + call.request().url(), Toast.LENGTH_SHORT).show();

        call.enqueue(new Callback<VideoList>() {
            @Override
            public void onResponse(@NonNull Call<VideoList> call, @NonNull Response<VideoList> response) {

                if (response.body() != null) {
                    ArrayList<Video> videoList = response.body().getVideoArrayList();
                    if (videoList != null) {
                        LeanBackHeaderCategory header=new LeanBackHeaderCategory("\uD83D\uDD0D"+searchQuery);

                        Log.e("wth", header.getName()+" adding "+videoList.size()+" to "+header.getName()+" at index "+header.getAdapterIndex());
                        header.addAllVideo(videoList);
                        ui.add(0,header);
                        loadRows();
                        }
                    }
                }

            @Override
            public void onFailure(@NonNull Call<VideoList> call, @NonNull Throwable t) {
                Log.wtf("err", t.fillInStackTrace());
                ErrorHelper.showToastFromCommunicationError( getContext(), t );
            }
        });
    }

    public static Server getEditServer() {
        return editServer;
    }

    public void setEditServer(Server editServer) {
        this.editServer = editServer;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getHistoryAndSubscriptions(){
        subscriptions=true;
        LeanBackHeaderCategory headsub = new LeanBackHeaderCategory("Subscriptions");
        headsub.setLoading(true);
        String apiBaseURL = APIUrlHelper.getUrlWithVersion(getContext());
        GetUserService userService = RetrofitInstance.getRetrofitInstance(apiBaseURL).create(GetUserService.class);
        Call<VideoList> call;
        call = userService.getVideosSubscripions(0,50, "-createdAt");

        Log.d("WTF","subscriptions URL Called"+ call.request().url() + "");
        call.enqueue(new Callback<VideoList>() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onResponse(@NonNull Call<VideoList> call, @NonNull Response<VideoList> response) {
                if (response.body() != null) {
                    ArrayList<Video> videoList = response.body().getVideoArrayList();
                    if (videoList != null) {
                        Log.e("wtf", headsub.getName()+" is getting response, adding "+videoList.size());
                        headsub.addAllVideo(videoList);
                        //loadRows();
                        headsub.setLoading(false);
                    }
                } else {
                    Log.e("WTF", "subscripotions failed to load");
                }
                headsub.setLoading(false);
                if (drawWhenLoaded) {
                    boolean allLoaded=true;
                    for (LeanBackHeaderCategory head : ui) {
                        if (head.isLoading()){
                            allLoaded=false;
                        }
                    }
                    if (allLoaded){
                        drawWhenLoaded=false;
                        loadRows();
                    }
                }
            }
            @Override
            public void onFailure(Call<VideoList> call, Throwable t) {
            }
        });
        ui.add(0,headsub);
        LeanBackHeaderCategory headHistory = new LeanBackHeaderCategory("History");
        headHistory.setLoading(true);
        call = userService.getVideosHistory(0,50, null);

        Log.d("WTF","history URL Called"+ call.request().url() + "");
        call.enqueue(new Callback<VideoList>() {
            @Override
            public void onResponse(@NonNull Call<VideoList> call, @NonNull Response<VideoList> response) {
                if (response.body() != null) {
                    ArrayList<Video> videoList = response.body().getVideoArrayList();
                    if (videoList != null) {
                        Log.e("wtf", headHistory.getName()+" is getting response history adding "+videoList.size());
                        headHistory.addAllVideo(videoList);
                        loadRows();
                        headHistory.setLoading(false);
                    }
                }
                else {
                    Log.e("WTF", "history failed to load");
                }
                headHistory.setLoading(false);
                if (drawWhenLoaded) {
                    boolean allLoaded=true;
                    for (LeanBackHeaderCategory head : ui) {
                        if (head.isLoading()){
                            allLoaded=false;
                        }
                    }
                    if (allLoaded){
                        drawWhenLoaded=false;
                        loadRows();
                    }
                }
            }
            @Override
            public void onFailure(Call<VideoList> call, Throwable t) {
            }
        });
        ui.add(headHistory);
    }
}