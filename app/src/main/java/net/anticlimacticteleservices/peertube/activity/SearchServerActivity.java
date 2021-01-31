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

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.anticlimacticteleservices.peertube.R;
import net.anticlimacticteleservices.peertube.adapter.ServerSearchAdapter;
import net.anticlimacticteleservices.peertube.application.AppApplication;
import net.anticlimacticteleservices.peertube.helper.APIUrlHelper;
import net.anticlimacticteleservices.peertube.helper.ErrorHelper;
import net.anticlimacticteleservices.peertube.model.ServerList;
import net.anticlimacticteleservices.peertube.network.GetServerListDataService;
import net.anticlimacticteleservices.peertube.network.RetrofitInstance;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Objects;

public class SearchServerActivity extends CommonActivity {

    private ServerSearchAdapter serverAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private EditText searchTextView;

    private final static String TAG = "SearchServerActivity";

    private int currentStart = 0;
    private final int count = 12;
    private String lastSearchtext = "";

    private TextView emptyView;
    private RecyclerView recyclerView;

    private boolean isLoading = false;

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // close this activity as oppose to navigating up
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_server);
        TextView textView = (TextView) findViewById(R.id.search_server_input_field);
        textView.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                Log.e(TAG,"text was changed top "+s.toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                loadServers(currentStart, count, s.toString());
            }

        });

        loadList();

    }


    TextView.OnEditorActionListener onSearchTextValidated = ( textView, i, keyEvent ) -> {
        if ( keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER
                || i == EditorInfo.IME_ACTION_GO ) {
            loadServers(currentStart, count, textView.getText().toString());
        }
        return false;
    };

    private void loadList() {

        recyclerView = findViewById(R.id.serverRecyclerView);
        swipeRefreshLayout = findViewById(R.id.serversSwipeRefreshLayout);
        searchTextView = findViewById(R.id.search_server_input_field );
        searchTextView.setOnEditorActionListener( onSearchTextValidated );

        emptyView = findViewById(R.id.empty_server_selection_view);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(SearchServerActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        serverAdapter = new ServerSearchAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(serverAdapter);

        loadServers(currentStart, count, AppApplication.getHackSearch() );

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {

                if (dy > 0) {
                    // is at end of list?
                    if (!recyclerView.canScrollVertically(RecyclerView.FOCUS_DOWN)) {
                        if (!isLoading) {
                            currentStart = currentStart + count;
                            loadServers(currentStart, count, searchTextView.getText().toString());
                        }
                    }
                }

            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Refresh items
            if (!isLoading) {
                currentStart = 0;
                loadServers(currentStart, count, searchTextView.getText().toString());
            }
        });


    }

    private void loadServers(int start, int count, String searchtext) {
        isLoading = true;

        GetServerListDataService service = RetrofitInstance.getRetrofitInstance(
                APIUrlHelper.getServerIndexUrl(SearchServerActivity.this)
        ).create(GetServerListDataService.class);
        Log.e(TAG,"loaad servers searching for "+searchtext);
        if ( !searchtext.equals( lastSearchtext ) )
        {
            currentStart = 0;
            lastSearchtext = searchtext;
        }

        Call<ServerList> call;

        call = service.getInstancesData(start, count, searchtext);

        Log.d("URL Called", call.request().url() + "");

        call.enqueue(new Callback<ServerList>() {
            @Override
            public void onResponse(@NonNull Call<ServerList> call, @NonNull Response<ServerList> response) {

                if (currentStart == 0) {
                    serverAdapter.clearData();
                }

                if (response.body() != null) {
                    serverAdapter.setData(response.body().getServerArrayList());
                }

                // no results show no results message
                if (currentStart == 0 && serverAdapter.getItemCount() == 0) {
                    emptyView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);

                } else {
                    emptyView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }

                isLoading = false;
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(@NonNull Call<ServerList> call, @NonNull Throwable t) {
                Log.wtf("err", t.fillInStackTrace());
                ErrorHelper.showToastFromCommunicationError( SearchServerActivity.this, t );
                isLoading = false;
                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }
}
