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

public class Server {

    private Integer id;
    private String host;
    private String name;
    private String shortDescription;
    private String version;
    private Boolean signupAllowed;
    private Double userVideoQuota;
    private Category category;
    private ArrayList<String> languages;
    private Boolean autoBlacklistUserVideosEnabled;
    private String defaultNSFWPolicy;
    private Boolean isNSFW;
    private Integer totalUsers;
    private Integer totalVideos;
    private Integer totalLocalVideos;
    private Integer totalInstanceFollowers;
    private Integer totalInstanceFollowing;
    private Boolean supportsIPv6;
    private String country;
    private Integer health;
    private Date createdAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Boolean getSignupAllowed() {
        return signupAllowed;
    }

    public void setSignupAllowed(Boolean signupAllowed) {
        this.signupAllowed = signupAllowed;
    }

    public Double getUserVideoQuota() {
        return userVideoQuota;
    }

    public void setUserVideoQuota(Double userVideoQuota) {
        this.userVideoQuota = userVideoQuota;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public ArrayList<String> getLanguages() {
        return languages;
    }

    public void setLanguages(ArrayList<String> languages) {
        this.languages = languages;
    }

    public Boolean getAutoBlacklistUserVideosEnabled() {
        return autoBlacklistUserVideosEnabled;
    }

    public void setAutoBlacklistUserVideosEnabled(Boolean autoBlacklistUserVideosEnabled) {
        this.autoBlacklistUserVideosEnabled = autoBlacklistUserVideosEnabled;
    }

    public String getDefaultNSFWPolicy() {
        return defaultNSFWPolicy;
    }

    public void setDefaultNSFWPolicy(String defaultNSFWPolicy) {
        this.defaultNSFWPolicy = defaultNSFWPolicy;
    }

    public Boolean getNSFW() {
        return isNSFW;
    }

    public void setNSFW(Boolean NSFW) {
        isNSFW = NSFW;
    }

    public Integer getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Integer totalUsers) {
        this.totalUsers = totalUsers;
    }

    public Integer getTotalVideos() {
        return totalVideos;
    }

    public void setTotalVideos(Integer totalVideos) {
        this.totalVideos = totalVideos;
    }

    public Integer getTotalLocalVideos() {
        return totalLocalVideos;
    }

    public void setTotalLocalVideos(Integer totalLocalVideos) {
        this.totalLocalVideos = totalLocalVideos;
    }

    public Integer getTotalInstanceFollowers() {
        return totalInstanceFollowers;
    }

    public void setTotalInstanceFollowers(Integer totalInstanceFollowers) {
        this.totalInstanceFollowers = totalInstanceFollowers;
    }

    public Integer getTotalInstanceFollowing() {
        return totalInstanceFollowing;
    }

    public void setTotalInstanceFollowing(Integer totalInstanceFollowing) {
        this.totalInstanceFollowing = totalInstanceFollowing;
    }

    public Boolean getSupportsIPv6() {
        return supportsIPv6;
    }

    public void setSupportsIPv6(Boolean supportsIPv6) {
        this.supportsIPv6 = supportsIPv6;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Integer getHealth() {
        return health;
    }

    public void setHealth(Integer health) {
        this.health = health;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}