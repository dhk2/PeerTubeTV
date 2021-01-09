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
package net.anticlimacticteleservices.peertube.network;

import net.anticlimacticteleservices.peertube.model.Rating;
import net.anticlimacticteleservices.peertube.model.Video;
import net.anticlimacticteleservices.peertube.model.VideoList;

import java.util.Set;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GetVideoDataService {
    @GET("videos/")
    Call<VideoList> getVideosData(
            @Query("start") int start,
            @Query("count") int count,
            @Query("sort") String sort,
            @Query("nsfw") String nsfw,
            @Query("filter") String filter,
            @Query("languageOneOf") Set<String> languages
    );

    @GET("videos/{id}")
    Call<Video> getVideoData(
            @Path(value = "id", encoded = true) String id
    );

    @GET("search/videos/")
    Call<VideoList> searchVideosData(
            @Query("start") int start,
            @Query("count") int count,
            @Query("sort") String sort,
            @Query("nsfw") String nsfw,
            @Query("search") String search,
            @Query("filter") String filter,
            @Query("languageOneOf") Set<String> languages
    );

    @GET("users/me/videos/{id}/rating")
    Call<Rating> getVideoRating(
            @Path(value = "id", encoded = true) Integer id
    );

    @PUT("videos/{id}/rate")
    Call<ResponseBody> rateVideo(
            @Path(value = "id", encoded = true) Integer id,
            @Body RequestBody params
    );

    // https://troll.tv/api/v1/accounts/theouterlinux@peertube.mastodon.host/videos?start=0&count=8&sort=-publishedAt

    @GET("accounts/{displayNameAndHost}/videos")
    Call<VideoList> getAccountVideosData(
            @Path(value = "displayNameAndHost", encoded = true) String displayNameAndHost,
            @Query("start") int start,
            @Query("count") int count,
            @Query("sort") String sort
    );

}