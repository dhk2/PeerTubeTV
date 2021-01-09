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
package net.anticlimacticteleservices.peertube.application;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.webkit.WebView;

import net.anticlimacticteleservices.peertube.model.Seed;

import java.util.ArrayList;

public class AppApplication extends Application {
    private static Application instance;
    private static ArrayList<Seed> seeds;
    private static String hackSearch="";

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        seeds=new ArrayList<Seed>();
    }

    public ArrayList<Seed> getSeeds() {
        return seeds;
    }

    public void setSeeds(ArrayList<WebView> webViews) {
        this.seeds = seeds;
    }

    public static Seed getMatch(String uuid){
        for (Seed w : seeds){
            if (w.getVideo().getUuid().equals(uuid)){
                return w;
            }
        }
        return null;
    }
    public static void addSeed(Seed seedToAdd){
        for (Seed w : seeds){
            if (w.getVideo().getUuid().equals(seedToAdd.getVideo().getUuid())){
                w=seedToAdd;
                return;
            }
        }
        seeds.add(seedToAdd);
        Log.e("WTF","added a new web seed seed "+seeds.size());
    }
    public static Context getContext() {
        return instance.getApplicationContext();
    }

    public static String getHackSearch() {
        return hackSearch;
    }

    public static void setHackSearch(String hackSearch) {
        AppApplication.hackSearch = hackSearch;
    }
}