package com.sip.voip;


import android.app.Activity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

public abstract class AssistantActivity extends Activity {
    protected ImageView mBack;
    @Override
    protected void onResume() {
        super.onResume();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        mBack = findViewById(R.id.back);
        mBack.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
    }
    @Override
    protected void onDestroy() {
        mBack = null;
        super.onDestroy();
    }

}
