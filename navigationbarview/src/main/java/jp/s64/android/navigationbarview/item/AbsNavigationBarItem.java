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

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public abstract class AbsNavigationBarItem implements INavigationBarItem {

    @Override
    public View updateIcon(@Nullable View original, Context context, int width, int height, IconAnimator animator) {
        RelativeLayout ret;
        AppCompatImageView img;
        if (original == null) {
            ret = new RelativeLayout(context);
            {
                ret.setLayoutParams(new ViewGroup.LayoutParams(width, height));
            }
            img = new AppCompatImageView(context);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            {
                img.setLayoutParams(params);
                img.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
            ret.addView(img);
        } else {
            ret = (RelativeLayout) original;
            img = (AppCompatImageView) ret.getChildAt(0);
        }
        {
            img.setImageResource(getDrawableIdRes(animator.isChecked));
        }
        Integer colorInt;
        if (animator.isChecked != animator.oldIsChecked) {
            ArgbEvaluator ev = new ArgbEvaluator();
            Integer before, after;
            {
                before = getColorInt(animator.oldIsChecked);
                after = getColorInt(animator.isChecked);
            }
            if (before != null && after != null) {
                colorInt = (int) ev.evaluate(
                        animator.fraction,
                        before,
                        after
                );
            } else {
                colorInt = null;
            }
        } else {
            colorInt = getColorInt(animator.isChecked);
        }
        {
            Drawable d = DrawableCompat.wrap(img.getDrawable()).mutate();
            if (colorInt != null) {
                DrawableCompat.setTint(d, colorInt);
                DrawableCompat.setTintMode(d, PorterDuff.Mode.SRC_IN);
            } else {
                DrawableCompat.setTintList(d, null);
            }
            img.setImageDrawable(d);
        }
        return ret;
    }

    @Override
    public void onItemViewCreated(View newView) {
        // no-op
    }

    @Override
    public void onItemViewRemoved(View removedView) {
        // no-op
    }

    @DrawableRes
    public abstract int getDrawableIdRes(boolean isChecked);

    @Nullable
    @ColorInt
    public abstract Integer getColorInt(boolean isChecked);

    @Nullable
    @Override
    public Integer getIconPixelSize() {
        return null;
    }

}
