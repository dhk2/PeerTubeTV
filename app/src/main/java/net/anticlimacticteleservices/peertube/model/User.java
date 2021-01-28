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
package net.anticlimacticteleservices.peertube.model;

import java.util.ArrayList;
import java.util.Date;

public class User {

    private Integer id;
    private String username;
    private String password;
    private String email;
    private String displayName;
    private ArrayList<Channel> Channels;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public User (String username,String email,String password){
        this.username=username;
        this.email=email;
        this.password=password;
    }
    public User(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public ArrayList<Channel> getChannels() {
        return Channels;
    }

    public void setChannels(ArrayList<Channel> channels) {
        Channels = channels;
    }
}