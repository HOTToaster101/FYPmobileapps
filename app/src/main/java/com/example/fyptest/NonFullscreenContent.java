package com.example.fyptest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import io.mattcarroll.hover.Content;

public class NonFullscreenContent implements Content {

        private final Context mContext;
        private View mContent;

        public NonFullscreenContent(@NonNull Context context) {
            mContext= context.getApplicationContext();
        }

        @NonNull
        @Override
        public View getView() {
            if (null == mContent) {
                mContent = LayoutInflater.from(mContext).inflate(R.layout.content_non_fullscreen, null);

                // We present our desire to be non-fullscreen by using WRAP_CONTENT for height.  This
                // preference will be honored by the Hover Menu to make our content only as tall as we
                // want to be.
                mContent.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            return mContent;
        }

        @Override
        public boolean isFullscreen() {
            return false;
        }

        @Override
        public void onShown() {
            // No-op.
        }

        @Override
        public void onHidden() {
            // No-op.
        }
}
