package com.groupproject.justeat.feature.staff;

import android.os.Bundle;

import com.groupproject.justeat.R;

/** 兼容旧入口；业务界面统一由 Fragment 实现。 */
public class StaffOrderActivity extends androidx.appcompat.app.AppCompatActivity {
    @Override
    protected void onCreate(Bundle state) {
        setTheme(R.style.Theme_JustEat_Staff);
        super.onCreate(state);
        setContentView(R.layout.activity_staff_standalone);
        if (state == null)
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.staff_standalone_content, new StaffOrderFragment())
                    .commit();
    }
}
