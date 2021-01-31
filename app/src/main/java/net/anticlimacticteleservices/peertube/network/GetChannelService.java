package net.anticlimacticteleservices.peertube.network;

import net.anticlimacticteleservices.peertube.model.Account;
import net.anticlimacticteleservices.peertube.model.ChannelList;
import net.anticlimacticteleservices.peertube.model.Me;
import net.anticlimacticteleservices.peertube.model.Result;
import net.anticlimacticteleservices.peertube.model.VideoList;

import java.util.Set;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GetChannelService {

    @GET("accounts/{displayName}")
    Call<Account> getAccount(
            @Path(value = "displayName", encoded = true) String displayName
    );

    @GET("accounts/{displayName}/video-channels")
    Call<ChannelList> getAccountChannels(
            @Path(value = "displayName", encoded = true) String displayName,
            @Query("start") int start,
            @Query("count") int count,
            @Query("sort") String sort
    );

    @GET("video-channels/{channelHandle}/videos")
    Call<VideoList> getChannelVideos(
        @Path(value = "channelHandle", encoded = true) String channelName,
        @Query("start") int start,
        @Query("count") int count,
        @Query("sort") String sort,
        @Query("nsfw") String nsfw,
        @Query("filter") String filter,
        @Query("languageOneOf")
        Set<String> languages
    );

    @GET("video-channels")
    Call<ChannelList> getChannels(
            @Query("start") int start,
            @Query("count") int count,
            @Query("sort") String sort
    );

}