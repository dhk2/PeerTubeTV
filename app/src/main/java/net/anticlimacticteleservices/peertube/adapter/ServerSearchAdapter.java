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

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import net.anticlimacticteleservices.peertube.R;
import net.anticlimacticteleservices.peertube.activity.SearchServerActivity;
import net.anticlimacticteleservices.peertube.helper.APIUrlHelper;
import net.anticlimacticteleservices.peertube.model.RemoteServer;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import static android.app.Activity.RESULT_OK;


public class ServerSearchAdapter extends RecyclerView.Adapter<ServerSearchAdapter.AccountViewHolder> {


    private ArrayList<RemoteServer> remoteServerList;
    private SearchServerActivity activity;
    private String baseUrl;

    public ServerSearchAdapter(ArrayList<RemoteServer> remoteServerList, SearchServerActivity activity) {
        this.remoteServerList = remoteServerList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.row_search_server, parent, false);

        baseUrl = APIUrlHelper.getUrl(activity);

        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {

        holder.name.setText(remoteServerList.get(position).getName());
        holder.host.setText(remoteServerList.get(position).getHost());
        holder.signupAllowed.setText(activity.getString(R.string.server_selection_signup_allowed, activity.getString(
                remoteServerList.get(position).getSignupAllowed() ?
                        R.string.server_selection_signup_allowed_yes :
                        R.string.server_selection_signup_allowed_no
        )));

        holder.videoTotals.setText(
                activity.getString(R.string.server_selection_video_totals,
                        remoteServerList.get(position).getTotalVideos().toString(),
                        remoteServerList.get(position).getTotalLocalVideos().toString()
                ));

        // don't show description if it hasn't been changes from the default
        if (!activity.getString(R.string.peertube_instance_search_default_description).equals(remoteServerList.get(position).getShortDescription())) {
            holder.shortDescription.setText(remoteServerList.get(position).getShortDescription());
            holder.shortDescription.setVisibility(View.VISIBLE);
        } else {
            holder.shortDescription.setVisibility(View.GONE);
        }

        DefaultArtifactVersion serverVersion = new DefaultArtifactVersion(remoteServerList.get(position).getVersion());

        // at least version 2.2
        DefaultArtifactVersion minVersion22 = new DefaultArtifactVersion("2.2.0");
        if (serverVersion.compareTo(minVersion22) >= 0) {
            // show NSFW Icon
            if (remoteServerList.get(position).getNSFW()) {
                holder.isNSFW.setVisibility(View.VISIBLE);
            }
        }


        // select server
        holder.itemView.setOnClickListener(v -> {

            String serverUrl = APIUrlHelper.cleanServerUrl(remoteServerList.get(position).getHost());

            Toast.makeText(activity, activity.getString(R.string.server_selection_set_server, serverUrl), Toast.LENGTH_LONG).show();

            Intent intent = new Intent();
            intent.putExtra("serverUrl", serverUrl);
            intent.putExtra("serverName", remoteServerList.get(position).getName());
            activity.setResult(RESULT_OK, intent);

            activity.finish();

        });

//
//
//        holder.moreButton.setText(R.string.video_more_icon);
//        new Iconics.IconicsBuilder().ctx(context).on(holder.moreButton).build();
//
//        holder.moreButton.setOnClickListener(v -> {
//
//            PopupMenu popup = new PopupMenu(context, v);
//            popup.setOnMenuItemClickListener(menuItem -> {
//                switch (menuItem.getItemId()) {
//                    case R.id.menu_share:
//                        Intents.Share(context, serverList.get(position));
//                        return true;
//                    default:
//                        return false;
//                }
//            });
//            popup.inflate(R.menu.menu_video_row_mode);
//            popup.show();
//
//        });

    }

    public void setData(ArrayList<RemoteServer> data) {
        remoteServerList.addAll(data);
        this.notifyDataSetChanged();
    }

    public void clearData() {
        remoteServerList.clear();
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return remoteServerList.size();
    }

    static class AccountViewHolder extends RecyclerView.ViewHolder {

        TextView name, host, signupAllowed, shortDescription, videoTotals;
        ImageView isNSFW;

        AccountViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.sl_row_name);
            host = itemView.findViewById(R.id.sl_row_host);
            signupAllowed = itemView.findViewById(R.id.sl_row_signup_allowed);
            shortDescription = itemView.findViewById(R.id.sl_row_short_description);
            isNSFW = itemView.findViewById(R.id.sl_row_is_nsfw);
            videoTotals = itemView.findViewById(R.id.sl_row_video_totals);
        }
    }


}