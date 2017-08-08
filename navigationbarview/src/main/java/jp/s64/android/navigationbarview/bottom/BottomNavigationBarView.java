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
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.transition.AutoTransition;
import android.support.transition.TransitionManager;
import android.support.transition.TransitionSet;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioGroup;

import com.google.common.collect.ImmutableList;

import jp.s64.android.navigationbarview.R;
import jp.s64.android.navigationbarview.item.INavigationBarItem;
import jp.s64.android.navigationbarview.view.INavigationBarView;
import jp.s64.android.navigationbarview.view.NavigationBarViewHelper;
import jp.s64.android.radiobuttonextended.core.widget.CompoundFrameLayoutRadioGroup;

public class BottomNavigationBarView extends FrameLayout implements INavigationBarView, NavigationBarViewHelper.IListener<BottomNavigationBarItemView> {

    private final NavigationBarViewHelper<BottomNavigationBarView, BottomNavigationBarItemView> mHelper = new NavigationBarViewHelper<>(this);

    private TransitionSet mTransitionSet;

    private int mInactiveMinWidth;
    private int mInactiveMaxWidth;
    private int mActiveMaxWidth;
    private int mItemHeight;

    private boolean mItemWidthFixed = false;

    private CompoundFrameLayoutRadioGroup mItemsContainer;
    private CompoundFrameLayoutRadioGroup.OnCheckedChangeListener mCheckChanged;

    @Nullable
    private ValueAnimator mShowAnimator = null;

    @Nullable
    private ValueAnimator mHideAnimator = null;

    private OnLayoutChangeListener mInitialLayoutListener;

    public BottomNavigationBarView(@NonNull Context context) {
        super(context, null);
        init(null, 0, 0);
    }

    public BottomNavigationBarView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
        init(attrs, 0, 0);
    }

    public BottomNavigationBarView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BottomNavigationBarView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr, defStyleRes);
    }

    protected void init(@Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        {
            mInitialLayoutListener = new OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    removeOnLayoutChangeListener(mInitialLayoutListener);
                    add(); // empty adding
                }
            };
            addOnLayoutChangeListener(mInitialLayoutListener);
        }
        {
            mInactiveMinWidth = getResources().getDimensionPixelSize(R.dimen.item_min_width);
            mInactiveMaxWidth = getResources().getDimensionPixelSize(R.dimen.inactive_item_max_width);
            mActiveMaxWidth = getResources().getDimensionPixelSize(R.dimen.active_item_max_width);
            mItemHeight = getResources().getDimensionPixelSize(R.dimen.horizontal_height);
        }
        {
            mTransitionSet = new AutoTransition();
            mTransitionSet.setOrdering(TransitionSet.ORDERING_TOGETHER);
            setDuration(200l);
            setInterpolator(new FastOutSlowInInterpolator());

        }
        {
            mCheckChanged = new CompoundFrameLayoutRadioGroup.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundFrameLayoutRadioGroup group, @IdRes int checkedId) {
                    TransitionManager.beginDelayedTransition(mItemsContainer, mTransitionSet);
                    {
                        mHelper.onCheckChanged(checkedId);
                    }
                }

            };
        }
        {
            mItemsContainer = new CompoundFrameLayoutRadioGroup(getContext());
            mItemsContainer.setOnCheckedChangeListener(mCheckChanged);
            addView(mItemsContainer);
        }
        {
            mItemsContainer.setGravity(Gravity.CENTER_HORIZONTAL);
            mItemsContainer.clearCheck();
        }
        float defaultElevation = getResources().getDimension(R.dimen.horizontal_elevation);
        {
            TypedArray a = null;
            try {
                a = getContext().obtainStyledAttributes(attrs, R.styleable.BottomNavigationBarView, defStyleAttr, defStyleRes);
                {
                    float elevation = a.getDimension(R.styleable.BottomNavigationBarView_android_elevation, defaultElevation);
                    ViewCompat.setElevation(this, elevation);
                }
                {
                    int orientation = a.getInteger(R.styleable.BottomNavigationBarView_android_orientation, RadioGroup.HORIZONTAL);
                    mItemsContainer.setOrientation(orientation);
                }
            } finally {
                a.recycle();
            }
        }
    }

    @Override
    public void clearItems() {
        mHelper.clearItems();
    }

    @Override
    public void add(INavigationBarItem... items) {
        mHelper.add(items);
    }

    @Override
    public void remove(INavigationBarItem... items) {
        mHelper.remove(items);
    }

    @Override
    public void remove(int index) {
        mHelper.remove(index);
    }

    @Override
    public int size() {
        return mHelper.size();
    }

    @Override
    public void setItemLimit(@Nullable Integer min, @Nullable Integer max) {
        mHelper.setItemLimit(min, max);
    }

    @Override
    public int getMaxSize() {
        return mHelper.getMaxSize();
    }

    @Override
    public int getMinSize() {
        return mHelper.getMinSize();
    }

    @Override
    public void check(@IdRes int idRes) {
        mItemsContainer.check(idRes);
    }

    @Nullable
    @Override
    public View getItemView(@IdRes int idRes) {
        for (int i = 0; i < mItemsContainer.getChildCount(); i++) {
            View view = mItemsContainer.getChildAt(i);
            if (view.getId() == idRes) {
                return view;
            }
        }
        return null;
    }

    @Override
    public void setOnCheckChangedListener(@Nullable OnCheckChangeListener listener) {
        mHelper.setOnCheckChangedListener(listener);
    }

    @Override
    public void show(final OnVisibilityAnimateListener doAnimate) {
        if (mShowAnimator != null) {
            return;
        }
        if (mHideAnimator != null) {
            mHideAnimator.cancel();
            mHideAnimator = null;
        }

        if (doAnimate != null) {
            final ValueAnimator innerAnimator = ValueAnimator.ofFloat(mItemsContainer.getAlpha(), 1);
            innerAnimator.setDuration(500l);
            innerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mItemsContainer.setAlpha((Float) animation.getAnimatedValue());
                }
            });
            innerAnimator.setInterpolator(new FastOutSlowInInterpolator());

            mShowAnimator = ValueAnimator.ofFloat(getTranslationY(), 0);
            mShowAnimator.setDuration(300l);
            mShowAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    setTranslationY((Float) animation.getAnimatedValue());
                    doAnimate.onVisibilityAnimate(BottomNavigationBarView.this);
                }
            });
            mShowAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    innerAnimator.start();
                }

                @Override
                public void onAnimationEnd(Animator animation) {

                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    innerAnimator.cancel();
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            mShowAnimator.setInterpolator(new FastOutSlowInInterpolator());
            mShowAnimator.start();
        }
    }

    @Override
    public void hide(final OnVisibilityAnimateListener doAnimate) {
        if (mHideAnimator != null) {
            return;
        }
        if (mShowAnimator != null) {
            mShowAnimator.cancel();
            mShowAnimator = null;
        }

        if (doAnimate != null) {
            final ValueAnimator innerAnimator = ValueAnimator.ofFloat(mItemsContainer.getAlpha(), 0);
            innerAnimator.setDuration(250l);
            innerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mItemsContainer.setAlpha((Float) animation.getAnimatedValue());
                }
            });
            innerAnimator.setInterpolator(new FastOutSlowInInterpolator());

            mHideAnimator = ValueAnimator.ofFloat(getTranslationY(), getHeight());
            mHideAnimator.setDuration(450l);
            mHideAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    setTranslationY((Float) animation.getAnimatedValue());
                    doAnimate.onVisibilityAnimate(BottomNavigationBarView.this);
                }
            });
            mHideAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    innerAnimator.start();
                }

                @Override
                public void onAnimationEnd(Animator animation) {

                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    innerAnimator.cancel();
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            mHideAnimator.setInterpolator(new FastOutSlowInInterpolator());
            mHideAnimator.start();
        }
    }

    @Override
    public boolean isNavigationBarShown() {
        return getTranslationY() == 0;
    }

    @Override
    public void onItemsChanged(ImmutableList<INavigationBarItem> oldItems, ImmutableList<INavigationBarItem> newItems) {
        {
            mHelper.onItemsChanged(mItemsContainer, oldItems, newItems);
        }
        int measuredWidth = mItemsContainer.getMeasuredWidth();
        int count = mItemsContainer.getChildCount();
        int inactiveCount;
        {
            int activeCount = mItemsContainer.getCheckedRadioButtonId() != View.NO_ID ? 1 : 0;
            inactiveCount = count - activeCount;
        }
        int activeWidth, inactiveWidth;
        if (count == 0) {
            inactiveWidth = activeWidth = 0;
        } else if (mItemWidthFixed || count == 1) {
            inactiveWidth = activeWidth = Math.min(mActiveMaxWidth, measuredWidth / count);
        } else {
            {
                int layoutMax = measuredWidth - (inactiveCount * mInactiveMinWidth);
                activeWidth = Math.min(layoutMax, mActiveMaxWidth);
            }
            {
                int layoutMax = (measuredWidth - activeWidth) / inactiveCount;
                inactiveWidth = Math.min(layoutMax, mInactiveMaxWidth);
            }
        }
        for (int i = 0; i < count; i++) {
            BottomNavigationBarItemView itm = (BottomNavigationBarItemView) mItemsContainer.getChildAt(i);
            itm.setWidth(inactiveWidth, activeWidth);
            itm.setHeight(mItemHeight);
            itm.setDuration(mTransitionSet.getDuration());
            itm.setInterpolator(mTransitionSet.getInterpolator());
        }
    }

    @Override
    public BottomNavigationBarItemView createItemView(INavigationBarItem item) {
        BottomNavigationBarItemView ret = new BottomNavigationBarItemView(getContext());
        FrameLayout.LayoutParams params;
        {
            params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        }
        {
            ret.setLayoutParams(params);
            ret.setItem(item);
        }
        return ret;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int h;
        if (size() < 1) {
            if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
                h = heightMeasureSpec;
            } else {
                h = MeasureSpec.makeMeasureSpec(mItemHeight, MeasureSpec.EXACTLY);
            }
        } else {
            h = heightMeasureSpec;
        }
        super.onMeasure(widthMeasureSpec, h);
    }

    public void setItemWidthFixed(boolean fixed) {
        mItemWidthFixed = fixed;
        mHelper.add(); // empty adding
    }

    @Override
    public void setDuration(long duration) {
        mTransitionSet.setDuration(duration);
    }

    @Override
    public void setInterpolator(TimeInterpolator interpolator) {
        mTransitionSet.setInterpolator(interpolator);
    }

    public static class BottomBehavior extends CoordinatorLayout.Behavior<BottomNavigationBarView> {

        public BottomBehavior() {
        }

        public BottomBehavior(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, BottomNavigationBarView child, View target, int dx, int dy, int[] consumed) {
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
            boolean isScrollDown = dy > 0;

            if (isScrollDown) {
                child.hide(createAnimator(target));
            } else {
                child.show(createAnimator(target));
            }
        }

        @Override
        public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, BottomNavigationBarView child, View directTargetChild, View target, int nestedScrollAxes) {
            return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
        }

        protected OnVisibilityAnimateListener createAnimator(View target) {
            return new OnVisibilityAnimateListener() {
                @Override
                public <SELF extends View & INavigationBarView> void onVisibilityAnimate(SELF self) {
                    // no-op
                }
            };
        }

    }

    public static class LiftUpBottomBehavior extends BottomBehavior {

        public LiftUpBottomBehavior() {
            super();
        }

        public LiftUpBottomBehavior(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected OnVisibilityAnimateListener createAnimator(final View target) {
            return new OnVisibilityAnimateListener() {

                @Override
                public <SELF extends View & INavigationBarView> void onVisibilityAnimate(SELF self) {
                    MarginLayoutParams params = (MarginLayoutParams) target.getLayoutParams();
                    params.bottomMargin = (int) (self.getTranslationY() - self.getHeight());
                    target.setLayoutParams(params);
                }

            };
        }

    }

}
