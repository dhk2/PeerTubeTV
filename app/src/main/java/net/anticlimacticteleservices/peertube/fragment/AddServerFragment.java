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
package net.anticlimacticteleservices.peertube.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.tooltip.TooltipDrawable;

import net.anticlimacticteleservices.peertube.R;
import net.anticlimacticteleservices.peertube.activity.RegisterAccountActivity;
import net.anticlimacticteleservices.peertube.activity.SearchServerActivity;
import net.anticlimacticteleservices.peertube.activity.ServerAddressBookActivity;
import net.anticlimacticteleservices.peertube.activity.TvActivity;
import net.anticlimacticteleservices.peertube.database.Server;
import net.anticlimacticteleservices.peertube.database.ServerViewModel;
import net.anticlimacticteleservices.peertube.helper.APIUrlHelper;
import net.anticlimacticteleservices.peertube.network.Session;
import net.anticlimacticteleservices.peertube.service.LoginService;

import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static net.anticlimacticteleservices.peertube.activity.VideoListActivity.EXTRA_VIDEOID;


public class AddServerFragment extends Fragment {

    public static final String TAG = "AddServerFragment";
    public static final Integer PICK_SERVER = 1;
    public static final Integer CREATE_USER = 2;

    private OnFragmentInteractionListener mListener;
    private Server oldServer=null;
    private View mView;
    EditText serverUrl ;
    EditText serverLabel;
    EditText serverUsername ;
    EditText serverPassword;
    Button addServerButton;
    Button deleteServerButton;
    Button registerAccountButton;
    Button pickServerUrl ;

    public AddServerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        oldServer=TvFragment.getEditServer();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        // Inflate the layout for this fragment

        mView = inflater.inflate(R.layout.fragment_add_server, container, false);
        //create ui elements
        EditText serverUrl = mView.findViewById(R.id.serverUrl);
        EditText serverLabel = mView.findViewById(R.id.serverLabel);
        EditText serverUsername = mView.findViewById(R.id.serverUsername);
        EditText serverPassword = mView.findViewById(R.id.serverPassword);
        Button addServerButton = mView.findViewById(R.id.addServerButton);
        Button deleteServerButton = mView.findViewById(R.id.deleteServerButton);
        Button registerAccountButton = mView.findViewById(R.id.registerUserButton);
        Button pickServerUrl = mView.findViewById(R.id.pickServerUrl);
        // bind button click


        addServerButton.setOnClickListener(view -> {

            Activity act = getActivity();

            boolean formValid = true;

            // close keyboard
            try {
                assert act != null;
                InputMethodManager inputManager = (InputMethodManager)
                        act.getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow(Objects.requireNonNull(act.getCurrentFocus()).getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            } catch (Exception e) {

            }

            if ( TextUtils.isEmpty(serverLabel.getText())){
                serverLabel.setError( act.getString(R.string.server_book_label_is_required ));
                Toast.makeText(act,"server name can't be blank", Toast.LENGTH_LONG).show();
                formValid = false;
            }
            // validate url
            String selectedUrl = APIUrlHelper.cleanServerUrl(serverUrl.getText().toString());
            serverUrl.setText(selectedUrl);


            if (!Patterns.WEB_URL.matcher(selectedUrl).matches()) {
                serverUrl.setError( act.getString(R.string.server_book_valid_url_is_required ) );
                Toast.makeText(act, R.string.invalid_url, Toast.LENGTH_LONG).show();
                formValid = false;
            }

            if (formValid) {
                if (act instanceof ServerAddressBookActivity) {
                    Server proposedServer=new Server(serverLabel.getText().toString());
                    if (oldServer !=null) {
                        proposedServer = oldServer;
                        proposedServer.setServerName(serverLabel.getText().toString());
                    }
                    proposedServer.setUsername(String.valueOf(serverUsername.getText()));
                    proposedServer.setServerHost(selectedUrl);
                    proposedServer.setPassword(String.valueOf(serverPassword.getText()));
                    Log.e("wtf",proposedServer.toString());
                    ((ServerAddressBookActivity) act).addServer(proposedServer);


                    //change to selected server
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(getContext().getString(R.string.pref_api_base_key), selectedUrl);
                    editor.apply();

                    Session session = Session.getInstance();
                    if (session.isLoggedIn()) {
                        session.invalidate();
                    }

                    // attempt authentication if we have a username
                    if (!TextUtils.isEmpty(proposedServer.getUsername())) {
                        Log.e("wtf","attemptig to logo nwith "+proposedServer.getUsername());
                        LoginService.Authenticate(
                                proposedServer.getUsername(),
                                proposedServer.getPassword()
                        );
                    }
                    Intent intent = new Intent(getContext(), TvActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    getContext().startActivity(intent);

                }

            }

        });



        deleteServerButton.setVisibility(View.GONE);
        deleteServerButton.dispatchSystemUiVisibilityChanged(View.INVISIBLE);
        deleteServerButton.setOnClickListener(view -> {
            ServerViewModel mServerViewModel = new ViewModelProvider(this).get(ServerViewModel.class);
            new AlertDialog.Builder(getContext())
                    .setTitle(getString(R.string.server_book_del_alert_title))
                    .setMessage(getString(R.string.server_book_del_alert_msg))
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        mServerViewModel.delete(oldServer);
                        Intent intent = new Intent(getContext(), TvActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        getContext().startActivity(intent);
                        Runtime.getRuntime().exit(0);
                    })
                    .setNegativeButton(android.R.string.no, (dialog, which) -> {
                        Log.e("WTF","wtf mate");
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            ;

        });

        registerAccountButton.setOnClickListener(view -> {
            Activity act = getActivity();
            if (act instanceof ServerAddressBookActivity) {
                if (!Patterns.WEB_URL.matcher(serverUrl.getText().toString()).matches()) {
                    serverUrl.setError( act.getString(R.string.server_book_valid_url_is_required ) );
                    Toast.makeText(act, R.string.invalid_url, Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(getContext(), RegisterAccountActivity.class);
                    intent.putExtra(Intent.EXTRA_USER, serverUsername.getText().toString());
                    intent.putExtra(Intent.EXTRA_TEXT, serverPassword.getText().toString());
                    intent.putExtra(Intent.EXTRA_TITLE, serverLabel.getText().toString());
                    intent.putExtra(Intent.EXTRA_ORIGINATING_URI, serverUrl.getText().toString());
                    // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    getContext().startActivity(intent);
                }
            }
        });



        pickServerUrl.setOnClickListener(view -> {
            Intent intentServer = new Intent(getActivity(), SearchServerActivity.class);
            this.startActivityForResult(intentServer, PICK_SERVER);
        });
        oldServer=TvFragment.getEditServer();
        if (null != oldServer){
            deleteServerButton.setVisibility(View.VISIBLE);
            Log.e("wtf",oldServer.toString());
            serverUrl.setText(oldServer.getServerHost());
            serverLabel.setText(oldServer.getServerName());
            serverUsername.setText(oldServer.getUsername());
            serverPassword.setText(oldServer.getPassword());

            addServerButton.setText("Save");
        }


        return mView;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_SERVER) {
            if(resultCode == RESULT_OK) {

                String serverUrlTest = data.getStringExtra("serverUrl");
                //Log.d(TAG, "serverUrl " + serverUrlTest);
                EditText serverUrl = mView.findViewById(R.id.serverUrl);
                serverUrl.setText(serverUrlTest);

                EditText serverLabel = mView.findViewById(R.id.serverLabel);
                if ("".equals(serverLabel.getText().toString())) {
                    serverLabel.setText(data.getStringExtra("serverName"));
                }

            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
