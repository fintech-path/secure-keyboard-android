package android.widget;

import android.content.Context;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.View;

import androidx.annotation.Nullable;

public class TextView extends View {
    private boolean mShowSoftInputOnFocus;

    private int imeOptions;

    private ActionMode.Callback mCustomSelectionActionModeCallback;

    private Editor mEditor = new Editor();

    public TextView(Context context) {
        super(context);
    }

    public TextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public InputFilter[] getFilters() {
        return null;
    }

    public final void setShowSoftInputOnFocus(boolean show) {
        mShowSoftInputOnFocus = show;
    }

    public final Boolean getShowSoftInputOnFocus() {
        return mShowSoftInputOnFocus;
    }

    public void setCustomSelectionActionModeCallback(ActionMode.Callback actionModeCallback) {
        mCustomSelectionActionModeCallback = actionModeCallback;
    }

    public ActionMode.Callback getCustomSelectionActionModeCallback() {
        return mCustomSelectionActionModeCallback;
    }

    public void setImeOptions(int imeOptions) {
        this.imeOptions = imeOptions;
    }

    public int getImeOptions() {
        return imeOptions;
    }

    public void setTextIsSelectable(boolean selectable) {}

    public final void setText(CharSequence text) {}
}
