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

import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.view.View;

import jp.s64.android.navigationbarview.item.INavigationBarItem;

public interface INavigationBarView extends INavigationBarTransitionView {

    void clearItems();

    void add(INavigationBarItem... items);

    void remove(INavigationBarItem... items);

    void remove(int index);

    int size();

    void setItemLimit(@Nullable Integer min, @Nullable Integer max);

    int getMaxSize();

    int getMinSize();

    void check(@IdRes int idRes);

    void uncheck();

    @Nullable
    View getItemView(@IdRes int idRes);

    void setOnCheckChangedListener(@Nullable OnCheckChangeListener listener);

    void show(OnVisibilityAnimateListener doAnimate);

    void hide(OnVisibilityAnimateListener doAnimate);

    boolean isNavigationBarShown();

    @IdRes
    int getChecked();

    interface OnCheckChangeListener {

        void onCheckChanged(@IdRes int oldIdRes, @IdRes int newIdRes);

    }

    interface OnVisibilityAnimateListener {

        <SELF extends View & INavigationBarView> void onVisibilityAnimate(SELF self);

    }

}
