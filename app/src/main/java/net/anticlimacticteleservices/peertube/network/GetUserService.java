package net.anticlimacticteleservices.peertube.network;

import net.anticlimacticteleservices.peertube.model.Account;
import net.anticlimacticteleservices.peertube.model.ChannelList;
import net.anticlimacticteleservices.peertube.model.Me;
import net.anticlimacticteleservices.peertube.model.Result;
import net.anticlimacticteleservices.peertube.model.VideoList;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
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

    @GET("users/me/history/videos")
    Call<VideoList> getVideosHistory(
            @Query("start") int start,
            @Query("count") int count,
            @Query("search") String sort
    );


    @GET("accounts/{displayName}")
    Call<Account> getAccount(
            @Path(value = "displayName", encoded = true) String displayName
    );


    @GET("accounts/{displayName}/video-channels")
    Call<ChannelList> getAccountChannels(
            @Path(value = "displayName", encoded = true) String displayName
    );


  //  @Headers("Content-Type: application/x-www-form-urlencoded")
    @FormUrlEncoded
    @POST("/api/v1/users/register")
    Call<Result> registerUser(
            @Field(value = "username" ) String username,
            @Field(value = "email") String email,
            @Field(value = "password") String password

    );


    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("/api/v1/users/register")
    Call<String> registerUserhack(@Body String fuck);


}