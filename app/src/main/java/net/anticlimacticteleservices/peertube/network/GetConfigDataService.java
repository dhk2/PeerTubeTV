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

import net.anticlimacticteleservices.peertube.database.Server;
import net.anticlimacticteleservices.peertube.model.Config;
import net.anticlimacticteleservices.peertube.model.RemoteServer;
import net.anticlimacticteleservices.peertube.model.ServerAbout;
import net.anticlimacticteleservices.peertube.model.ServerConfig;
import net.anticlimacticteleservices.peertube.model.ServerConfigInstance;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GetConfigDataService {

    @GET("config")
    Call<Config> getConfigData();

    @GET("config/custom")
    Call<RemoteServer> getConfigCustom(
    );

    @GET("config")
    Call<ServerConfig> getConfigTest();

    @GET("config/about")
    Call<ServerAbout> getServerAbout();

}