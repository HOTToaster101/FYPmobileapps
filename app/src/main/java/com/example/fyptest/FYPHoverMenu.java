package com.example.fyptest;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;

import io.mattcarroll.hover.HoverMenu;

public class FYPHoverMenu extends HoverMenu {

    private final Context mContext;
    private final String mMenuId;
    private final Section mSection;

    public FYPHoverMenu(Context context, String menuId) {
        mContext = context.getApplicationContext();
        mMenuId = menuId;
        mSection = new Section(
                new SectionId("0"),
                createTabView(),
                new NonFullscreenContent(context)
        );
    }

    private View createTabView() {
        Resources resources = mContext.getResources();

        FYPTabView view = new FYPTabView(
                mContext,
                resources.getDrawable(R.drawable.tab_background),
                resources.getDrawable(R.drawable.tab_background)
        );
        view.setTabBackgroundColor(0xFFFF9600);
        view.setTabForegroundColor(null);
        return view;
    }

    @Override
    public String getId() {
        return mMenuId;
    }

    @Override
    public int getSectionCount() {
        return 1;
    }

    @Nullable
    @Override
    public Section getSection(int index) {
        return mSection;
    }

    @Nullable
    @Override
    public Section getSection(@NonNull SectionId sectionId) {
        return mSection;
    }

    @NonNull
    @Override
    public List<Section> getSections() {
        return Collections.singletonList(mSection);
    }
}
