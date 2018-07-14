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

package jp.s64.android.navigationbarview.example;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jp.s64.android.navigationbarview.bottom.BottomNavigationBarView;
import jp.s64.android.navigationbarview.item.AbsBadgeNavigationBarItem;
import jp.s64.android.navigationbarview.item.INavigationBarItem;
import jp.s64.android.navigationbarview.view.INavigationBarView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, INavigationBarView.OnCheckChangeListener, RadioGroup.OnCheckedChangeListener {

    private final Map<Integer, Integer> INDEX_ID_PAIRS = new HashMap<>();
    private final Map<Integer, AbsBadgeNavigationBarItem> ID_ITEM_PAIRS = new HashMap<>();

    private BottomNavigationBarView mNavigation;
    private Button mAdd, mRemove, mUncheck;

    private RadioGroup mTextModeGroup;
    private RadioGroup mWeightModeGroup;

    private RadioGroup mBehaviorModeGroup;
    private RadioGroup mLayoutModeGroup;

    private RadioGroup mIconModeGroup;

    private boolean mIconModeIsTintable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        {
            INDEX_ID_PAIRS.put(0, R.id.menu_item_0);
            INDEX_ID_PAIRS.put(1, R.id.menu_item_1);
            INDEX_ID_PAIRS.put(2, R.id.menu_item_2);
            INDEX_ID_PAIRS.put(3, R.id.menu_item_3);
            INDEX_ID_PAIRS.put(4, R.id.menu_item_4);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        {
            mNavigation = (BottomNavigationBarView) findViewById(R.id.target_view);
        }
        {
            mAdd = (Button) findViewById(R.id.add);
            mRemove = (Button) findViewById(R.id.remove);
            mUncheck = (Button) findViewById(R.id.uncheck_all);
        }
        {
            mTextModeGroup = (RadioGroup) findViewById(R.id.text_mode_group);
            mWeightModeGroup = (RadioGroup) findViewById(R.id.weight_mode_group);
        }
        {
            mBehaviorModeGroup = (RadioGroup) findViewById(R.id.behavior_mode_group);
            mLayoutModeGroup = (RadioGroup) findViewById(R.id.layout_mode_group);
        }
        {
            mIconModeGroup = (RadioGroup) findViewById(R.id.icon_mode_group);
        }
        for (MyItem.TextMode mode : MyItem.TextMode.values()) {
            RadioButton radio = new RadioButton(this);
            {
                radio.setId(mode.radioIdRes);
                radio.setText(mode.name().toLowerCase());
            }
            {
                RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.weight = 1;
                radio.setLayoutParams(params);
            }
            mTextModeGroup.addView(radio);
        }
        {
            mAdd.setOnClickListener(this);
            mRemove.setOnClickListener(this);
            mUncheck.setOnClickListener(this);
        }
        {
            mTextModeGroup.setOnCheckedChangeListener(this);
            mTextModeGroup.check(MyItem.TextMode.ONLY_ACTIVE.radioIdRes);
        }
        {
            mWeightModeGroup.setOnCheckedChangeListener(this);
            mWeightModeGroup.check(R.id.weight_mode_flexible);
        }
        {
            mBehaviorModeGroup.setOnCheckedChangeListener(this);
            mBehaviorModeGroup.check(R.id.behavior_mode_behavior);
        }
        {
            mLayoutModeGroup.setOnCheckedChangeListener(this);
            mBehaviorModeGroup.check(R.id.layout_mode_show);
        }
        {
            mIconModeGroup.setOnCheckedChangeListener(this);
            mIconModeGroup.check(R.id.icon_mode_tintable);
        }
        {
            mNavigation.setItemLimit(0, null);
            mNavigation.setOnCheckChangedListener(this);
        }
        {
            resetButtonState();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mAdd) {
            mNavigation.add(createItem());
        } else if (v == mRemove) {
            mNavigation.remove(mNavigation.size() - 1);
        } else if (v == mUncheck) {
            mNavigation.uncheck();
        }
        {
            resetButtonState();
        }
    }

    private void refreshItems() {
        for (int i = 0; i < mNavigation.size(); i++) {
            mNavigation.replace(i, createItem(i));
        }
        resetButtonState();
    }

    protected void resetButtonState() {
        {
            mAdd.setEnabled(mNavigation.size() < mNavigation.getMaxSize());
            mRemove.setEnabled(mNavigation.size() > mNavigation.getMinSize());
        }
    }

    protected INavigationBarItem createItem() {
        return createItem(mNavigation.size());
    }

    protected INavigationBarItem createItem(int i) {
        MyItem.TextMode textMode = MyItem.TextMode.ONLY_ACTIVE;
        for (MyItem.TextMode mode : MyItem.TextMode.values()) {
            if (mode.radioIdRes == mTextModeGroup.getCheckedRadioButtonId()) {
                textMode = mode;
            }
        }
        final int id = INDEX_ID_PAIRS.get(i);
        final Integer iconPixelSize;
        {
            EditText input = (EditText) findViewById(R.id.icon_size_input);
            String str = input.getText().toString();
            if (str != null && str.length() > 0) {
                iconPixelSize = Integer.valueOf(str);
            } else {
                iconPixelSize = null;
            }
        }
        MyItem ret = new MyItem(this, id, textMode) {

            @Nullable
            @Override
            public Integer getIconPixelSize() {
                return iconPixelSize;
            }

            @Override
            public int getDrawableIdRes(boolean isChecked) {
                if (mIconModeIsTintable)
                    return R.drawable.ic_account_circle_black;
                else
                    return isChecked ? R.mipmap.ic_rasterized_active : R.mipmap.ic_rasterized_inactive;
            }

            @Nullable
            @Override
            public Integer getColorInt(boolean isChecked) {
                if (mIconModeIsTintable)
                    return getMyColor(isChecked);
                else
                    return null;
            }

        };
        {
            ret.setBadgeText(String.valueOf(1));
        }
        ID_ITEM_PAIRS.put(id, ret);
        return ret;
    }

    @Override
    public void onCheckChanged(@IdRes int oldIdRes, @IdRes int newIdRes) {
        Toast.makeText(
                this,
                String.format(Locale.ROOT, "%d -> %d", oldIdRes, newIdRes),
                Toast.LENGTH_SHORT
        ).show();
        if (ID_ITEM_PAIRS.containsKey(newIdRes)) {
            ID_ITEM_PAIRS.get(newIdRes).setBadgeText(null);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        if (group == mWeightModeGroup) {
            mNavigation.setItemWidthFixed(checkedId == R.id.weight_mode_fixed);
        } else if (group == mBehaviorModeGroup) {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mNavigation.getLayoutParams();
            switch (checkedId) {
                case R.id.behavior_mode_behavior:
                    params.setBehavior(new BottomNavigationBarView.LiftUpBottomBehavior());
                    break;
                case R.id.behavior_mode_layout:
                default:
                    params.setBehavior(null);
                    break;
            }
            mNavigation.setLayoutParams(params);
            mLayoutModeGroup.check(R.id.layout_mode_show);
        } else if (group == mLayoutModeGroup) {
            switch (checkedId) {
                case R.id.layout_mode_show:
                    mNavigation.show(new INavigationBarView.OnVisibilityAnimateListener() {
                        @Override
                        public <SELF extends View & INavigationBarView> void onVisibilityAnimate(SELF self) {
                            // no-op
                        }
                    });
                    break;
                case R.id.layout_mode_hide:
                default:
                    mNavigation.hide(new INavigationBarView.OnVisibilityAnimateListener() {
                        @Override
                        public <SELF extends View & INavigationBarView> void onVisibilityAnimate(SELF self) {
                            // no-op
                        }
                    });
                    break;
            }
        } else if (group == mIconModeGroup) {
            switch (checkedId) {
                case R.id.icon_mode_rasterized:
                    mIconModeIsTintable = false;
                    break;
                case R.id.icon_mode_tintable:
                default:
                    mIconModeIsTintable = true;
                    break;
            }
            refreshItems();
        }
    }

    public static abstract class MyItem extends AbsBadgeNavigationBarItem {

        private final Context context;

        @IdRes
        private final int idRes;

        private final TextMode mode;

        public enum TextMode {
            ALWAYS(R.id.text_mode_always),
            ONLY_ACTIVE(R.id.text_mode_only_active),
            DISABLE(R.id.text_mode_disable),
            //
            ;

            @IdRes
            public final int radioIdRes;

            TextMode(@IdRes int radioIdRes) {
                this.radioIdRes = radioIdRes;
            }

        }

        public MyItem(Context context, @IdRes int idRes, TextMode mode) {
            this.context = context;
            this.idRes = idRes;
            this.mode = mode;
        }

        @Override
        public int getIdRes() {
            return idRes;
        }

        @Nullable
        @Override
        public String getText(boolean isChecked) {
            switch (mode) {
                case ALWAYS:
                    return "Always";
                case ONLY_ACTIVE:
                    return isChecked ? "Active" : null;
                case DISABLE:
                default:
                    return null;
            }
        }

        @Override
        public int getTextColorInt(boolean isChecked) {
            return getMyColor(isChecked);
        }

        protected Integer getMyColor(boolean isChecked) {
            return ContextCompat.getColor(
                    context,
                    isChecked ? R.color.menu_enabled : R.color.menu_disabled
            );
        }

    }

}
