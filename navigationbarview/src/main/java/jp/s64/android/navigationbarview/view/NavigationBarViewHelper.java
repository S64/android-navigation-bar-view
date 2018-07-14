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

package jp.s64.android.navigationbarview.view;

import android.animation.TimeInterpolator;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import jp.s64.android.navigationbarview.R;
import jp.s64.android.navigationbarview.item.INavigationBarItem;
import jp.s64.android.radiobuttonextended.core.widget.CompoundFrameLayoutRadioGroup;

public class NavigationBarViewHelper<SELF extends View & INavigationBarView & NavigationBarViewHelper.IListener<ITEM>, ITEM extends View & Checkable & INavigationBarItemView> implements INavigationBarView {

    private final LinkedHashSet<INavigationBarItem> mItems = new LinkedHashSet<>();
    private final SELF self;

    private final View.OnLayoutChangeListener mLayoutChangeListener = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            if (self.getVisibility() == View.VISIBLE) {
                assertMoreThanMinSize(self.size());
                assertLessThanMaxSize(self.size());
            }
        }
    };

    @Nullable
    private Integer mDefinedMaxSize = null;

    @Nullable
    private Integer mDefinedMinSize = null;

    @Nullable
    private OnCheckChangeListener mListener = null;

    @Nullable
    private Integer mOldCheckedId = null;

    public NavigationBarViewHelper(SELF self) {
        {
            this.self = self;
        }
        setItemLimit(null, null);
        self.addOnLayoutChangeListener(mLayoutChangeListener);
    }

    @Override
    public void clearItems() {
        ImmutableList<INavigationBarItem> oldItems = ImmutableList.copyOf(mItems),
                newItems = ImmutableList.of();
        {
            mItems.clear();
        }
        self.onItemsChanged(oldItems, newItems);
    }

    @Override
    public void add(INavigationBarItem... items) {
        ImmutableList<INavigationBarItem> oldItems, newItems;
        {
            oldItems = ImmutableList.copyOf(mItems);
        }
        for (INavigationBarItem item : items) {
            mItems.add(item);
        }
        {
            newItems = ImmutableList.copyOf(mItems);
        }
        self.onItemsChanged(oldItems, newItems);
    }

    @Override
    public void remove(INavigationBarItem... items) {
        ImmutableList<INavigationBarItem> oldItems, newItems;
        {
            oldItems = ImmutableList.copyOf(mItems);
        }
        for (INavigationBarItem item : items) {
            mItems.remove(item);
        }
        {
            newItems = ImmutableList.copyOf(mItems);
        }
        self.onItemsChanged(oldItems, newItems);
    }

    @Override
    public void remove(int index) {
        remove(Lists.newArrayList(mItems).get(index));
    }

    @Override
    public void replace(int index, INavigationBarItem item) {
        ImmutableList<INavigationBarItem> oldItems;
        List<INavigationBarItem> newItems;
        {
            oldItems = ImmutableList.copyOf(mItems);
        }
        {
            newItems = new ArrayList<>(oldItems);
            newItems.remove(index);
            newItems.add(index, item);
        }
        {
            self.onItemsChanged(oldItems, ImmutableList.copyOf(newItems));
        }
    }

    @Override
    public int size() {
        return mItems.size();
    }

    @Override
    public void setItemLimit(@Nullable Integer min, @Nullable Integer max) {
        mDefinedMaxSize = max != null ? max : self.getResources().getInteger(R.integer.default_max_items);
        mDefinedMinSize = min != null ? min : self.getResources().getInteger(R.integer.default_min_items);
    }

    @Override
    public int getMaxSize() {
        return mDefinedMaxSize != null ? mDefinedMaxSize : Integer.MAX_VALUE;
    }

    @Override
    public int getMinSize() {
        return mDefinedMinSize != null ? mDefinedMinSize : 0;
    }

    @Deprecated
    @Override
    public void check(@IdRes int idRes) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void uncheck() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Nullable
    @Override
    public View getItemView(@IdRes int idRes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOnCheckChangedListener(@Nullable OnCheckChangeListener listener) {
        mListener = listener;
    }

    @Deprecated
    @Override
    public void show(OnVisibilityAnimateListener doAnimate) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void hide(OnVisibilityAnimateListener doAnimate) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public boolean isNavigationBarShown() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public int getChecked() {
        throw new UnsupportedOperationException();
    }

    public void onItemsChanged(CompoundFrameLayoutRadioGroup container, ImmutableList<INavigationBarItem> oldItems, ImmutableList<INavigationBarItem> newItems) {
        assertLessThanMaxSize(newItems.size());
        //assertMoreThanMinSize(newItems.size());

        final int max;
        {
            int nc = newItems.size(), oc = oldItems.size();
            max = nc > oc ? nc : oc;
        }

        final List<View> pendingRemoveViews = new LinkedList<>();

        for (int i = 0; i < max; i++) {
            INavigationBarItem newItem;
            {
                newItem = newItems.size() > i ? newItems.get(i) : null;
            }
            if (newItem == null) {
                pendingRemoveViews.add(container.getChildAt(i));
            } else if (oldItems.indexOf(newItem) != i) {
                View newView = self.createItemView(newItem, container.getCheckedRadioButtonId() == newItem.getIdRes()),
                        placedView = container.getChildAt(i);
                {
                    container.addView(newView, i);
                    newItem.onItemViewCreated(newView);
                }
                if (placedView != null) {
                    container.removeView(placedView);
                    newItem.onItemViewRemoved(placedView);
                }
            }
        }
        for (View pendingRemoveView : pendingRemoveViews) {
            container.removeView(pendingRemoveView);
        }
    }

    protected void assertLessThanMaxSize(int newSize) {
        if (newSize <= getMaxSize()) {
            return;
        }
        throw new ItemsOutOfLimitException(getMinSize(), getMaxSize());
    }

    protected void assertMoreThanMinSize(int newSize) {
        if (newSize >= getMinSize()) {
            return;
        }
        throw new ItemsOutOfLimitException(getMinSize(), getMaxSize());
    }

    @Deprecated
    @Override
    public void setDuration(long duration) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void setInterpolator(TimeInterpolator interpolator) {
        throw new UnsupportedOperationException();
    }

    public void onCheckChanged(@IdRes int checkedId) {
        if (mListener != null) {
            mListener.onCheckChanged(
                    mOldCheckedId == null ? View.NO_ID : mOldCheckedId,
                    checkedId
            );
        }
        mOldCheckedId = checkedId;
    }

    public static class ItemsOutOfLimitException extends RuntimeException {

        protected static final String FORMAT = "Number of items supported by BottomNavigationView is %d - %d.";

        public ItemsOutOfLimitException(int min, int max) {
            super(String.format(
                    Locale.ROOT,
                    FORMAT,
                    min,
                    max
            ));
        }

    }

    public interface IListener<ITEM extends View & Checkable & INavigationBarItemView> {

        void onItemsChanged(ImmutableList<INavigationBarItem> oldItems, ImmutableList<INavigationBarItem> newItems);

        ITEM createItemView(INavigationBarItem item, boolean isChecked);

    }

}
