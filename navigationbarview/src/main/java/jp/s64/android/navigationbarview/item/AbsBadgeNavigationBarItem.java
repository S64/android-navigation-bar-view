/*
 * Copyright (C) 2017 Shuma Yoshioka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.s64.android.navigationbarview.item;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import jp.s64.android.navigationbarview.R;

public abstract class AbsBadgeNavigationBarItem extends AbsNavigationBarItem {

    protected AppCompatTextView mBadge;

    protected CharSequence mBadgeText;

    @Override
    public View updateIcon(@Nullable View original, Context context, int width, int height, IconAnimator animator) {
        RelativeLayout ret = (RelativeLayout) super.updateIcon(original, context, width, height, animator);
        AppCompatTextView badge = null;
        {
            for (int i = 0; i < ret.getChildCount(); i++) {
                View v = ret.getChildAt(i);
                if (v instanceof TextView) {
                    badge = (AppCompatTextView) v;
                    break;
                }
            }
        }
        if (badge == null) {
            CharSequence oldText;
            if (mBadgeText != null) {
                oldText = mBadgeText;
                mBadgeText = null;
            } else {
                oldText = mBadge != null ? mBadge.getText() : null;
            }
            badge = mBadge = new AppCompatTextView(context);
            int size = context.getResources().getDimensionPixelSize(R.dimen.item_badge_size);
            {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                params.topMargin = (size / 4) * -1;
                params.rightMargin = (size / 3) * -1;
                badge.setLayoutParams(params);
                badge.setIncludeFontPadding(false);
                badge.setGravity(Gravity.CENTER);
            }
            {
                badge.setTextColor(ContextCompat.getColor(context, R.color.item_badge_text_color));
                ViewCompat.setBackground(badge, ResourcesCompat.getDrawable(context.getResources(), R.drawable.item_badge_background, context.getTheme()));
                badge.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.item_badge_text_size));
                badge.setTypeface(null, Typeface.BOLD);
            }
            {
                setBadgeText(oldText);
            }
            ret.addView(badge);
        }
        return ret;
    }

    public void setBadgeText(@Nullable CharSequence text) {
        if (mBadge != null) {
            mBadge.setText(text);
            mBadge.setAlpha(text != null ? 1f : 0f);
        } else {
            mBadgeText = text;
        }
    }

}
