/*
 * Copyright (C) 2014 Sebastian Kaspari
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.androidzeitgeist.dashwatch.event;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

public class BusProvider {
    private static Bus bus;
    private static Handler handler;

    public static synchronized Bus getBus() {
        if (bus == null) {
            bus = new Bus();
        }

        return bus;
    }

    private static synchronized Handler getMainThreadHandler() {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }

        return handler;
    }

    public static void postOnMainThread(final Object object) {
        getMainThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                bus.post(object);
            }
        });
    }
}
