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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.squareup.picasso.Picasso;

import net.anticlimacticteleservices.peertube.R;
import net.anticlimacticteleservices.peertube.database.Server;
import net.anticlimacticteleservices.peertube.database.ServerViewModel;
import net.anticlimacticteleservices.peertube.fragment.TvFragment;
import net.anticlimacticteleservices.peertube.helper.APIUrlHelper;
import net.anticlimacticteleservices.peertube.helper.ErrorHelper;
import net.anticlimacticteleservices.peertube.model.Avatar;
import net.anticlimacticteleservices.peertube.model.Me;
import net.anticlimacticteleservices.peertube.model.Result;
import net.anticlimacticteleservices.peertube.model.User;
import net.anticlimacticteleservices.peertube.model.VideoList;
import net.anticlimacticteleservices.peertube.network.GetUserService;
import net.anticlimacticteleservices.peertube.network.GetVideoDataService;
import net.anticlimacticteleservices.peertube.network.RetrofitInstance;
import net.anticlimacticteleservices.peertube.network.Session;
import net.anticlimacticteleservices.peertube.network.SimpleRetrofitInstance;
import net.anticlimacticteleservices.peertube.service.LoginService;

import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static net.anticlimacticteleservices.peertube.application.AppApplication.getContext;
import static net.anticlimacticteleservices.peertube.fragment.AddServerFragment.PICK_SERVER;


public class RegisterAccountActivity extends CommonActivity {

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // close this activity as oppose to navigating up
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register_account);

        EditText newUserName = findViewById(R.id.newUserName);
        Button createUserButton = findViewById(R.id.createUserButton);
        EditText newUseremail = findViewById(R.id.newUserEmail);
        EditText newUserPassword = findViewById(R.id.newUserPassword);
        TextView terms=findViewById(R.id.terms);
        CheckBox agree = findViewById(R.id.ageCheckbox);

        Intent intent = getIntent();
        newUserPassword.setText(intent.getStringExtra(Intent.EXTRA_TEXT));
        newUserName.setText(intent.getStringExtra(Intent.EXTRA_USER));
        String serverUrl=intent.getStringExtra(Intent.EXTRA_ORIGINATING_URI);
        String serverLabel=intent.getStringExtra(Intent.EXTRA_TITLE);
        ServerViewModel
        mServerViewModel = new ViewModelProvider(this).get(ServerViewModel.class);

        createUserButton.setEnabled(false);
        createUserButton.setOnClickListener(view -> {
            boolean formValid = true;
            // close keyboard
            try {
                InputMethodManager inputManager = (InputMethodManager)
                        this.getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow(Objects.requireNonNull(this.getCurrentFocus()).getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            } catch (Exception e) {

            }


            String userName = newUserName.getText().toString();
            if ( TextUtils.isEmpty(userName)){
                newUserName.setError( getString(R.string.prompt_name ));
                Toast.makeText(this,"user name can't be blank", Toast.LENGTH_LONG).show();
                formValid = false;
            }

            String email = newUseremail.getText().toString();


            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                newUseremail.setError( getString(R.string.error_invalid_email ) );
                Toast.makeText(this, R.string.error_invalid_email, Toast.LENGTH_LONG).show();
                formValid = false;
            }

            if (formValid) {
                String password = newUserPassword.getText().toString();
                Log.e("WTF","need to create user:["+userName+"]["+password+"]["+email+"]");
                String apiBaseURL = APIUrlHelper.getUrlWithVersion(this);
                GetUserService service = RetrofitInstance.getRetrofitInstance(apiBaseURL).create(GetUserService.class);
                Call<Result> call = service.registerUser(userName,email,password);
                call.enqueue(new Callback<Result>() {
                    @Override
                    public void onResponse(Call<Result> call, Response<Result> response) {
                        Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_LONG).show();
                        Log.e("WTF", "succeeded"+response);
                        switch (response.code()){
                            case 204:
                                Log.e("wtf","new account created");
                                Server proposedServer= TvFragment.getEditServer();
                                if (null==proposedServer){
                                   proposedServer=new Server(serverLabel);
                                }
                                proposedServer.setServerHost(serverUrl);
                                proposedServer.setUsername(userName);
                                proposedServer.setPassword(password);

                                Log.e("wtf",proposedServer.toString());

                                mServerViewModel.insert(proposedServer);


                                //change to selected server
                                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString(getContext().getString(R.string.pref_api_base_key), serverUrl);
                                editor.apply();

                                Session session = Session.getInstance();
                                if (session.isLoggedIn()) {
                                    session.invalidate();
                                }


                                Log.e("wtf","attemptig to logon with "+proposedServer.getUsername());
                                LoginService.Authenticate(
                                        proposedServer.getUsername(),
                                        proposedServer.getPassword()
                                );

                                Intent intent = new Intent(getContext(), TvActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                getContext().startActivity(intent);




                                break;
                            case 409:
                                Log.e("wtf","Account or email already exists");
                                break;
                            case 400:
                                Log.e("wtf","400 error could mean password too short");

                        }
                    }

                    @Override
                    public void onFailure(Call<Result> call, Throwable t) {
                        Toast.makeText(getApplicationContext(), t.toString(), Toast.LENGTH_LONG).show();
                        Log.e("WTF", "failed");
                        Log.e("WTF", t.getMessage()+"\n"+t.getLocalizedMessage());
                    }
                });
            }

        });

        agree.setOnClickListener (view -> {

            terms.setText("need to actually get the terms from the site to display here");
            terms.setVisibility(View.VISIBLE);
            createUserButton.setEnabled(true);
        });

    }
}
