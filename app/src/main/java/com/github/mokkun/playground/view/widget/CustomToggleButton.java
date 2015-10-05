package com.github.mokkun.playground.view.widget;

import android.content.Context;
import android.util.AttributeSet;

public class CustomToggleButton extends android.widget.ToggleButton {
    private OnCheckedChangeListener mListener = null;

    public CustomToggleButton(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomToggleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomToggleButton(Context context) {
        super(context);
    }

    @Override public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        super.setOnCheckedChangeListener(listener);
        mListener = listener;
    }

    public void setCheckedState(boolean checked) {
        super.setOnCheckedChangeListener(null);
        super.setChecked(checked);
        super.setOnCheckedChangeListener(mListener);
    }
}
