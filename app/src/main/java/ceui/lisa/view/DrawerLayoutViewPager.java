package ceui.lisa.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class DrawerLayoutViewPager extends ViewPager {

    // NOTE:
    // Drawer gesture handling (QQ-like full-screen swipe with a 50dp threshold)
    // is implemented in MainActivity.dispatchTouchEvent().
    // We DO NOT forward touch events to DrawerLayout here, otherwise nested
    // ViewPagers (e.g. 推荐作品/热门标签) will feel "stuck".
    private IForwardTouchEvent touchEventForwarder = null;

    public DrawerLayoutViewPager(@NonNull Context context) {
        super(context);
    }

    public DrawerLayoutViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setTouchEventForwarder(IForwardTouchEvent touchEventForwarder) {
        this.touchEventForwarder = touchEventForwarder;
    }

    // Keep default ViewPager touch handling.

    public interface IForwardTouchEvent {
        void forwardTouchEvent(MotionEvent ev);
    }
}
