package com.example.fyptest;

import android.content.Context;
import android.content.Intent;
import android.view.ContextThemeWrapper;

import androidx.annotation.NonNull;

import io.mattcarroll.hover.HoverMenu;
import io.mattcarroll.hover.HoverView;
import io.mattcarroll.hover.window.HoverMenuService;

public class FYPHoverMenuService extends HoverMenuService {
    private static final String TAG = "DemoHoverMenuService";

    public static void showFloatingMenu(Context context) {
        context.startService(new Intent(context, FYPHoverMenuService.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected Context getContextForHoverMenu() {
        return new ContextThemeWrapper(this, R.style.AppTheme);
    }

    @Override
    protected void onHoverMenuLaunched(@NonNull Intent intent, @NonNull HoverView hoverView) {
        hoverView.setMenu(createHoverMenu());
        hoverView.collapse();
    }

    @NonNull
    private HoverMenu createHoverMenu() {
        return new FYPHoverMenu(getApplicationContext(), "nonfullscreen");
    }
}
