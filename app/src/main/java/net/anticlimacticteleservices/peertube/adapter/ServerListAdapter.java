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
package net.anticlimacticteleservices.peertube.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.anticlimacticteleservices.peertube.R;

import net.anticlimacticteleservices.peertube.database.Server;
import net.anticlimacticteleservices.peertube.helper.APIUrlHelper;
import net.anticlimacticteleservices.peertube.network.Session;
import net.anticlimacticteleservices.peertube.service.LoginService;


import java.util.List;

import static android.app.Activity.RESULT_OK;

public class ServerListAdapter extends RecyclerView.Adapter<ServerListAdapter.ServerViewHolder> {


    private final LayoutInflater mInflater;
    private List<Server> mServers; // Cached copy of Servers

    public ServerListAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ServerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.row_server_address_book, parent, false);
        return new ServerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ServerViewHolder holder, int position) {

        if (mServers != null) {
            Server current = mServers.get(position);
            holder.serverLabel.setText(current.getServerName());
            holder.serverUrl.setText(current.getServerHost());

            if (TextUtils.isEmpty(current.getUsername())) {
                holder.hasLogin.setVisibility(View.GONE);
            } else {
                holder.hasLogin.setVisibility(View.VISIBLE);
            }

        } else {
            // Covers the case of data not being ready yet.
            holder.serverLabel.setText(R.string.server_book_no_servers_found);
        }

        holder.itemView.setOnClickListener(v -> {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mInflater.getContext());
            SharedPreferences.Editor editor = sharedPref.edit();

            String serverUrl = APIUrlHelper.cleanServerUrl(getServerAtPosition(position).getServerHost());

            editor.putString(mInflater.getContext().getString(R.string.pref_api_base_key), serverUrl);
            editor.apply();

            // Logout if logged in
            Session session = Session.getInstance();
            if (session.isLoggedIn()) {
                session.invalidate();
            }

            // attempt authentication if we have a username
            if (!TextUtils.isEmpty(getServerAtPosition(position).getUsername())) {
                LoginService.Authenticate(
                        getServerAtPosition(position).getUsername(),
                        getServerAtPosition(position).getPassword()
                );
            }

            // tell server list activity to reload list
            Intent intent = new Intent();
            ((Activity) mInflater.getContext()).setResult(RESULT_OK, intent);

            // close this activity
            ((Activity) mInflater.getContext()).finish();

            Toast.makeText(mInflater.getContext(), mInflater.getContext().getString(R.string.server_selection_set_server, serverUrl), Toast.LENGTH_LONG).show();

        });


//
//        holder.itemView.setOnLongClickListener(v -> {
//            Log.v("ServerListAdapter", "setOnLongClickListener " + position);
//            return true;
//        });


    }

    public void setServers(List<Server> Servers) {
        mServers = Servers;
        this.notifyDataSetChanged();
    }

    // getItemCount() is called many times, and when it is first called,
    // mServers has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mServers != null)
            return mServers.size();
        else return 0;
    }

    static class ServerViewHolder extends RecyclerView.ViewHolder {
        TextView serverLabel, serverUrl, serverUsername;
        ImageView hasLogin;

        private ServerViewHolder(View itemView) {
            super(itemView);
            serverLabel = itemView.findViewById(R.id.serverLabelRow);
            serverUrl = itemView.findViewById(R.id.serverUrlRow);
            hasLogin = itemView.findViewById(R.id.sb_row_has_login_icon);
        }
    }

    public Server getServerAtPosition (int position) {
        return mServers.get(position);
    }
}