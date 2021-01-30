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
package net.anticlimacticteleservices.peertube.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Server.class}, version = 1, exportSchema = false)
public abstract class ServerRoomDatabase extends RoomDatabase {

    public abstract ServerDao serverDao();

    private static volatile ServerRoomDatabase INSTANCE;

    private static final int NUMBER_OF_THREADS = 4;

    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
//TODO implement some other way to load the initial server list to keep android studio from bitching about a main thread trivial database call
    public static ServerRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ServerRoomDatabase.class) {
                if (INSTANCE == null) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                ServerRoomDatabase.class, "server_database")
                                .allowMainThreadQueries()
                                .build();
                    }
                }
            }
        }
        return INSTANCE;
    }

}

