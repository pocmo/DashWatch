package com.androidzeitgeist.dashwatch.common;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.IOException;

public class Utils {
    private static final String TAG = "DashWatch/Utils";

    private static final int EXTENSION_ICON_SIZE = 128;

    public static Bitmap flattenExtensionIcon(Drawable baseIcon, int color) {
        if (baseIcon == null) {
            return null;
        }

        Bitmap outBitmap = Bitmap.createBitmap(EXTENSION_ICON_SIZE, EXTENSION_ICON_SIZE,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outBitmap);
        baseIcon.setBounds(0, 0, EXTENSION_ICON_SIZE, EXTENSION_ICON_SIZE);
        baseIcon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        baseIcon.draw(canvas);
        baseIcon.setColorFilter(null);
        baseIcon.setCallback(null); // free up any references
        return outBitmap;
    }

    public static Bitmap flattenExtensionIcon(Context context, Bitmap baseIcon, int color) {
        return flattenExtensionIcon(new BitmapDrawable(context.getResources(), baseIcon), color);
    }

    public static Bitmap loadExtensionIcon(Context context, ComponentName extension,
                                           int icon, Uri iconUri, int color) {
        if (iconUri != null) {
            return loadExtensionIconFromUri(context, iconUri);
        }

        if (icon <= 0) {
            return null;
        }

        String packageName = extension.getPackageName();
        try {
            Context packageContext = context.createPackageContext(packageName, 0);
            Resources packageRes = packageContext.getResources();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(packageRes, icon, options);

            // Cut down the icon to a smaller size.
            int sampleSize = 1;
            while (true) {
                if (options.outHeight / (sampleSize * 2) > Utils.EXTENSION_ICON_SIZE / 2) {
                    sampleSize *= 2;
                } else {
                    break;
                }
            }

            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;

            return Utils.flattenExtensionIcon(
                    context,
                    BitmapFactory.decodeResource(packageRes, icon, options),
                    color);

        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Couldn't access extension's package while loading icon data.");
        }

        return null;
    }

    public static Bitmap loadExtensionIconFromUri(Context context, Uri iconUri) {
        try {
            ParcelFileDescriptor pfd = context.getContentResolver()
                    .openFileDescriptor(iconUri, "r");

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor(), null, options);

            // Cut down the icon to a smaller size.
            int sampleSize = 1;
            while (true) {
                if (options.outHeight / (sampleSize * 2) > Utils.EXTENSION_ICON_SIZE / 2) {
                    sampleSize *= 2;
                } else {
                    break;
                }
            }

            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;

            return Utils.flattenExtensionIcon(
                    context,
                    BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor(), null, options),
                    0xffffffff);

        } catch (IOException e) {
            Log.e(TAG, "Couldn't read icon from content URI.", e);
        } catch (SecurityException e) {
            Log.e(TAG, "Couldn't read icon from content URI.", e);
        }

        return null;
    }
}
