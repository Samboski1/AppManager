// SPDX-License-Identifier: GPL-3.0-or-later

package io.github.muntashirakon.AppManager.usage;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Size;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.PendingIntentCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.muntashirakon.AppManager.R;
import io.github.muntashirakon.AppManager.self.SelfPermissions;
import io.github.muntashirakon.AppManager.settings.FeatureController;
import io.github.muntashirakon.AppManager.users.Users;
import io.github.muntashirakon.AppManager.utils.DateUtils;
import io.github.muntashirakon.AppManager.utils.ExUtils;
import io.github.muntashirakon.AppManager.utils.appearance.AppearanceUtils;

public class ScreenTimeAppWidget extends AppWidgetProvider {
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        if (!FeatureController.isUsageAccessEnabled() || !SelfPermissions.checkUsageStatsPermission()) {
            return;
        }
        // Fetch colors
        context = AppearanceUtils.getThemedWidgetContext(context, false);
        // Fetch screens time
        int[] userIds = Users.getUsersIds();
        List<PackageUsageInfo> packageUsageInfoList = new ArrayList<>();
        AppUsageStatsManager usageStatsManager = AppUsageStatsManager.getInstance();
        for (int userId : userIds) {
            ExUtils.exceptionAsIgnored(() -> packageUsageInfoList.addAll(usageStatsManager
                    .getUsageStats(UsageUtils.USAGE_TODAY, userId)));
        }
        Collections.sort(packageUsageInfoList, (o1, o2) -> -Long.compare(o1.screenTime, o2.screenTime));
        long totalScreenTime = 0;
        for (PackageUsageInfo appItem : packageUsageInfoList) {
            totalScreenTime += appItem.screenTime;
        }
        // Get pending intent
        Intent intent = new Intent(context, AppUsageActivity.class);
        PendingIntent pendingIntent = PendingIntentCompat.getActivity(context, 0 /* no requestCode */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT, false);
        // Construct the RemoteViews object
        Size appWidgetSize = getAppWidgetSize(context, appWidgetManager, appWidgetId);
        RemoteViews views;
        if (appWidgetSize.getHeight() <= 200) {
            views = new RemoteViews(context.getPackageName(), R.layout.app_widget_screen_time_small);
        } else if (appWidgetSize.getWidth() <= 250) {
            views = new RemoteViews(context.getPackageName(), R.layout.app_widget_screen_time);
        } else {
            views = new RemoteViews(context.getPackageName(), R.layout.app_widget_screen_time_large);
        }
        // Set views
        views.setTextViewText(R.id.screen_time, DateUtils.getFormattedDurationShort(totalScreenTime, false, true, false));
        int len = Math.min(packageUsageInfoList.size(), 3);
        if (len < 3) {
            views.setViewVisibility(R.id.app3_circle, View.INVISIBLE);
            views.setViewVisibility(R.id.app3_time, View.INVISIBLE);
            views.setViewVisibility(R.id.app3_label, View.INVISIBLE);
        }
        if (len < 2) {
            views.setViewVisibility(R.id.app2_circle, View.INVISIBLE);
            views.setViewVisibility(R.id.app2_time, View.INVISIBLE);
            views.setViewVisibility(R.id.app2_label, View.INVISIBLE);
        }
        if (len < 1) {
            views.setViewVisibility(R.id.app1_circle, View.INVISIBLE);
            views.setViewVisibility(R.id.app1_time, View.INVISIBLE);
            views.setViewVisibility(R.id.app1_label, View.INVISIBLE);
        }
        if (len > 0) {
            PackageUsageInfo item1 = packageUsageInfoList.get(0);
            views.setTextViewText(R.id.app1_label, item1.appLabel);
            views.setTextViewText(R.id.app1_time, DateUtils.getFormattedDurationSingle(item1.screenTime, false));
        }
        if (len > 1) {
            PackageUsageInfo item2 = packageUsageInfoList.get(1);
            views.setTextViewText(R.id.app2_label, item2.appLabel);
            views.setTextViewText(R.id.app2_time, DateUtils.getFormattedDurationSingle(item2.screenTime, false));
        }
        if (len == 3) {
            PackageUsageInfo item3 = packageUsageInfoList.get(2);
            views.setTextViewText(R.id.app3_label, item3.appLabel);
            views.setTextViewText(R.id.app3_time, DateUtils.getFormattedDurationSingle(item3.screenTime, false));
        }
        // Set colors
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            int colorSurface = MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurface, "colorSurface");
            int textColorPrimary = MaterialColors.getColor(context, android.R.attr.textColorPrimary, "textColorSecondary");
            int textColorSecondary = MaterialColors.getColor(context, android.R.attr.textColorSecondary, "textColorSecondary");
            ColorStateList color1 = ColorStateList.valueOf(MaterialColors.harmonizeWithPrimary(context, Color.parseColor("#1b1b1b")));
            ColorStateList color2 = ColorStateList.valueOf(MaterialColors.harmonizeWithPrimary(context, Color.parseColor("#565e71")));
            ColorStateList color3 = ColorStateList.valueOf(MaterialColors.harmonizeWithPrimary(context, Color.parseColor("#d4e3ff")));
            views.setTextColor(R.id.screen_time_label, textColorSecondary);
            views.setTextColor(R.id.screen_time, textColorPrimary);
            views.setTextColor(R.id.app1_label, textColorSecondary);
            views.setTextColor(R.id.app2_label, textColorSecondary);
            views.setTextColor(R.id.app3_label, textColorSecondary);
            views.setColorStateList(R.id.app1_time, "setBackgroundTintList", color1);
            views.setColorStateList(R.id.app1_circle, "setBackgroundTintList", color1);
            views.setColorStateList(R.id.app2_time, "setBackgroundTintList", color2);
            views.setColorStateList(R.id.app2_circle, "setBackgroundTintList", color2);
            views.setColorStateList(R.id.app3_time, "setBackgroundTintList", color3);
            views.setColorStateList(R.id.app3_circle, "setBackgroundTintList", color3);
            views.setInt(R.id.widget_background, "setBackgroundColor", colorSurface);
        }
        views.setOnClickPendingIntent(R.id.widget_background, pendingIntent);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    @NonNull
    private static Size getAppWidgetSize(@NonNull Context context, @NonNull AppWidgetManager manager, int appWidgetId) {
        Bundle appWidgetOptions = manager.getAppWidgetOptions(appWidgetId);
        boolean isPortrait = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        int width = appWidgetOptions.getInt(isPortrait ? AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH : AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
        int height = appWidgetOptions.getInt(isPortrait ? AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT : AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
        return new Size(width, height);
    }
}