package net.anticlimacticteleservices.peertube.network;

import net.anticlimacticteleservices.peertube.model.Account;
import net.anticlimacticteleservices.peertube.model.ChannelList;
import net.anticlimacticteleservices.peertube.model.Me;
import net.anticlimacticteleservices.peertube.model.VideoList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GetUserService {

    @GET("users/me")
    Call<Me> getMe();

    @GET("users/me/subscriptions/videos")
    Call<VideoList> getVideosSubscripions(
            @Query("start") int start,
            @Query("count") int count,
            @Query("sort") String sort
    );


    @GET("accounts/{displayName}")
    Call<Account> getAccount(
            @Path(value = "displayName", encoded = true) String displayName
    );


    @GET("accounts/{displayName}/video-channels")
    Call<ChannelList> getAccountChannels(
            @Path(value = "displayName", encoded = true) String displayName
    );


}