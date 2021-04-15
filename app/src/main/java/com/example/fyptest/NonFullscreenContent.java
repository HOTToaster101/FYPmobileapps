package com.example.fyptest;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
                Button bCamera = mContent.findViewById(R.id.b_return_h);
                bCamera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContent.getContext(), ActivityCamera.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        mContent.getContext().startActivity(intent);
                    }
                });

                Button bHome = mContent.findViewById(R.id.b_home_h);
                bHome.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContent.getContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        mContent.getContext().startActivity(intent);
                    }
                });

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
