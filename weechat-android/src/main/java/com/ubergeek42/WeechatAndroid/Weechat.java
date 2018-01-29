/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.ubergeek42.WeechatAndroid;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.StringRes;
import android.widget.Toast;

import com.ubergeek42.WeechatAndroid.service.Events;
import com.ubergeek42.WeechatAndroid.service.Notificator;
import com.ubergeek42.WeechatAndroid.service.P;
import com.ubergeek42.WeechatAndroid.service.RelayService.STATE;
import com.ubergeek42.cats.Cats;

import org.greenrobot.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;

public class Weechat extends Application {
    private static Logger logger = LoggerFactory.getLogger("W");

    static Thread mainThread = Thread.currentThread();
    static Handler mainHandler = new Handler();
    static Context applicationContext;

    @Override public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();
        Cats.setupKittens(applicationContext);
        P.init(getApplicationContext());
        P.restoreStuff();
        Notificator.init(this);
        EventBus.builder().logNoSubscriberMessages(false).eventInheritance(false).installDefaultEventBus();
        EventBus.getDefault().postSticky(new Events.StateChangedEvent(EnumSet.of(STATE.STOPPED)));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////// main thread
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // like runOnUiThread, but always queue
    public static void runOnMainThread(Runnable action) {
        mainHandler.post(action);
    }

    public static void runOnMainThreadASAP(Runnable action) {
        if (Thread.currentThread() == mainThread) action.run();
        else mainHandler.post(action);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////// toasts
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static void showShortToast(final String message) {
        logger.trace("showShortToast({})", message);
        mainHandler.post(() -> Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show());
    }

    public static void showShortToast(final String message, final Object... args) {
        showShortToast(String.format(message, args));
    }

    public static void showShortToast(final @StringRes int id) {
        showShortToast(applicationContext.getResources().getString(id));
    }

    public static void showShortToast(final @StringRes int id, final Object... args) {
        showShortToast(applicationContext.getResources().getString(id, args));
    }

    public static void showLongToast(final String message) {
        logger.trace("showLongToast({})", message);
        mainHandler.post(() -> Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show());
    }

    public static void showLongToast(final String message, final Object... args) {
        showLongToast(String.format(message, args));
    }

    public static void showLongToast(final @StringRes int id) {
        showLongToast(applicationContext.getResources().getString(id));
    }

    public static void showLongToast(final @StringRes int id, final Object... args) {
        showLongToast(applicationContext.getResources().getString(id, args));
    }
}
