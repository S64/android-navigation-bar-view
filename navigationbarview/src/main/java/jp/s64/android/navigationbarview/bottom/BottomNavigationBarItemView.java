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

package jp.s64.android.navigationbarview.bottom;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import jp.s64.android.navigationbarview.R;
import jp.s64.android.navigationbarview.item.INavigationBarItem;
import jp.s64.android.navigationbarview.view.INavigationBarItemView;
import jp.s64.android.radiobuttonextended.core.widget.RadioFrameLayout;

public class BottomNavigationBarItemView extends RadioFrameLayout implements INavigationBarItemView {

    private RelativeLayout mContainer;
    private FrameLayout mImageWrapper;
    private FrameLayout mTextWrapper;
    private TextView mText;

    private INavigationBarItem mItem;

    private Integer inactiveWidth = null;
    private Integer activeWidth = null;
    private Integer height = null;

    private Float mActiveTextScale, mInactiveTextScale, mDisableTextScale;

    private TimeInterpolator mInterpolator;
    private Long mDuration;

    private Integer mTextUnderPaddingVisible, mTextUnderPaddingGone;

    private Integer mTextSize;

    private boolean mOldIsChecked = false;

    public BottomNavigationBarItemView(@NonNull Context context) {
        super(context);
        init();
    }

    public BottomNavigationBarItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BottomNavigationBarItemView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BottomNavigationBarItemView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    protected void init() {
        {
            mActiveTextScale = getFloat(getResources(), R.dimen.active_item_with_text_text_scale);
            mInactiveTextScale = getFloat(getResources(), R.dimen.inactive_item_with_text_text_scale);
            mDisableTextScale = getFloat(getResources(), R.dimen.item_without_text_text_scale);
        }
        {
            setInterpolator(new FastOutSlowInInterpolator());
            setDuration(115l);
        }
        {
            mTextUnderPaddingVisible = getResources().getDimensionPixelSize(R.dimen.item_with_text_under_padding);
            mTextUnderPaddingGone = getResources().getDimensionPixelSize(R.dimen.item_without_text_under_padding);
        }
        {
            mTextSize = getResources().getDimensionPixelSize(R.dimen.item_text_size);
        }
        inflate(getContext(), R.layout.view_bottom_navigation_bar_item, this);
        {
            mContainer = (RelativeLayout) findViewById(R.id.root);
            mImageWrapper = (FrameLayout) findViewById(R.id.icon_wrapper);
            mTextWrapper = (FrameLayout) findViewById(R.id.text_wrapper);
            mText = (TextView) findViewById(R.id.text);
        }
        resetLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (inactiveWidth != null && activeWidth != null && height != null) {
            super.onMeasure(
                    MeasureSpec.makeMeasureSpec(getMeasuringWidth(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
            );
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    protected Integer getMeasuringWidth() {
        return isChecked() ? activeWidth : inactiveWidth;
    }

    @Override
    public void setWidth(int inactive, int active) {
        {
            this.inactiveWidth = inactive;
            this.activeWidth = active;
        }
        requestLayout();
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
        requestLayout();
    }

    @Override
    public void setChecked(boolean isChecked) {
        super.setChecked(isChecked);
        resetLayout();
        requestLayout();
    }

    @Override
    public void setItem(INavigationBarItem item) {
        {
            mItem = item;
        }
        setId(mItem.getIdRes());
        resetLayout();
    }

    protected void resetLayout() {
        if (mItem == null || mContainer == null || mText == null) {
            return;
        }
        final boolean oldWithTextState;
        final float oldScale;
        {
            oldWithTextState = mText.getAlpha() != 0f;
            oldScale = mText.getScaleX();
        }
        final boolean isChecked = isChecked();
        boolean withText;
        {
            String text = mItem.getText(isChecked);
            if (text != null) {
                mText.setText(text);
                withText = true;
            } else {
                withText = false;
            }
        }
        float textScale;
        {
            textScale = withText ? (isChecked ? mActiveTextScale : mInactiveTextScale) : mDisableTextScale;
        }
        int textSize = withText ? (int) (mTextSize * textScale) : 0;
        {
            int padding = withText ? mTextUnderPaddingVisible : mTextUnderPaddingGone;
            mImageWrapper.setPadding(
                    mImageWrapper.getPaddingLeft(),
                    mImageWrapper.getPaddingTop(),
                    mImageWrapper.getPaddingRight(),
                    padding + textSize
            );
        }
        {
            mTextWrapper.setPadding(
                    mTextWrapper.getPaddingLeft(),
                    mTextWrapper.getPaddingTop(),
                    mTextWrapper.getPaddingRight(),
                    mTextUnderPaddingVisible
            );
        }
        final ValueAnimator fraction, aTextAlpha, aTextScale;
        {
            aTextAlpha = ValueAnimator.ofFloat(
                    oldWithTextState ? 1f : 0f,
                    withText ? 1f : 0f
            );
            aTextAlpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mText.setAlpha((Float) animation.getAnimatedValue());
                }
            });
        }
        {
            aTextScale = ValueAnimator.ofFloat(
                    oldScale,
                    textScale
            );
            aTextScale.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mText.setScaleX((Float) animation.getAnimatedValue());
                    mText.setScaleY((Float) animation.getAnimatedValue());
                }
            });
        }
        final boolean oldIsChecked = Boolean.valueOf(mOldIsChecked);
        {
            fraction = ValueAnimator.ofFloat(0, 1);
            fraction.setDuration(mDuration);
            fraction.setInterpolator(mInterpolator);
            fraction.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    {
                        View original = null, newIcon;
                        for (int i = 0; i < mImageWrapper.getChildCount(); i++) {
                            View view = mImageWrapper.getChildAt(i);
                            if (original == null) {
                                original = view;
                            } else {
                                mImageWrapper.removeView(view);
                            }
                        }
                        newIcon = mItem.updateIcon(
                                original,
                                getContext(),
                                getResources().getDimensionPixelSize(R.dimen.item_icon_size),
                                getResources().getDimensionPixelSize(R.dimen.item_icon_size),
                                new INavigationBarItem.IconAnimator(isChecked, oldIsChecked, animation.getAnimatedFraction())
                        );
                        if (original != newIcon) {
                            mImageWrapper.removeView(original);
                            mImageWrapper.addView(newIcon);
                        }
                    }
                    {
                        ArgbEvaluator ev = new ArgbEvaluator();
                        mText.setTextColor(
                                (Integer) ev.evaluate(animation.getAnimatedFraction(), mItem.getTextColorInt(oldIsChecked), mItem.getTextColorInt(isChecked))
                        );
                    }
                }
            });
            fraction.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    {
                        mOldIsChecked = Boolean.valueOf(isChecked);
                    }
                    {
                        aTextAlpha.setDuration(fraction.getDuration());
                        aTextScale.setDuration(fraction.getDuration());
                    }
                    {
                        aTextAlpha.setInterpolator(fraction.getInterpolator());
                        aTextScale.setInterpolator(fraction.getInterpolator());
                    }
                    {
                        aTextAlpha.setInterpolator(fraction.getInterpolator());
                        aTextScale.setInterpolator(fraction.getInterpolator());
                    }
                    {
                        aTextAlpha.start();
                        aTextScale.start();
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {

                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    {
                        aTextAlpha.cancel();
                        aTextScale.cancel();
                    }
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
        fraction.start();
    }

    protected static float getFloat(Resources res, int resId) {
        TypedValue out = new TypedValue();
        res.getValue(resId, out, true);
        return out.getFloat();
    }

    @Override
    public void setDuration(long duration) {
        mDuration = duration;
    }

    @Override
    public void setInterpolator(TimeInterpolator interpolator) {
        mInterpolator = interpolator;
    }

}
