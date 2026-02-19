package ceui.lisa.fragments;

import android.content.Intent;
import android.view.MenuItem;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import ceui.lisa.R;
import ceui.lisa.activities.MainActivity;
import ceui.lisa.activities.Shaft;
import ceui.lisa.activities.TemplateActivity;
import ceui.lisa.databinding.FragmentLeftBinding;
import ceui.lisa.utils.MyOnTabSelectedListener;
import ceui.lisa.utils.Dev;
import ceui.lisa.utils.Params;

public class FragmentLeft extends BaseLazyFragment<FragmentLeftBinding> {

    private NetListFragment[] mFragments = null;

    @Override
    public void initLayout() {
        mLayoutID = R.layout.fragment_left;
    }

    @Override
    public void initView() {
        if (Dev.hideMainActivityStatus) {
            ViewGroup.LayoutParams headParams = baseBind.head.getLayoutParams();
            headParams.height = Shaft.statusHeight;
            baseBind.head.setLayoutParams(headParams);
        }

        baseBind.toolbar.setNavigationOnClickListener(v -> {
            if (mActivity instanceof MainActivity) {
                ((MainActivity) mActivity).getDrawer().openDrawer(GravityCompat.START, true);
            }
        });
        baseBind.toolbarTitle.setText(R.string.string_207);
        baseBind.toolbar.inflateMenu(R.menu.fragment_left);
        baseBind.toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_search) {
                    Intent intent = new Intent(mContext, TemplateActivity.class);
                    intent.putExtra(TemplateActivity.EXTRA_FRAGMENT, "搜索");
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void lazyData() {
        String[] TITLES = new String[]{
                Shaft.getContext().getString(R.string.recommend_illust),
                Shaft.getContext().getString(R.string.hot_tag)
        };
        mFragments = new NetListFragment[]{
                FragmentRecmdIllust.newInstance("插画"),
                FragmentHotTag.newInstance(Params.TYPE_ILLUST)
        };
        baseBind.viewPager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager(), 0) {
            @NonNull
            @Override
            public Fragment getItem(int i) {
                return mFragments[i];
            }

            @Override
            public int getCount() {
                return TITLES.length;
            }

            @NonNull
            @Override
            public CharSequence getPageTitle(int position) {
                return TITLES[position];
            }
        });
        baseBind.tabLayout.setupWithViewPager(baseBind.viewPager);
        MyOnTabSelectedListener listener = new MyOnTabSelectedListener(mFragments);
        baseBind.tabLayout.addOnTabSelectedListener(listener);

        // Fix: On the second tab (Hot Tag), swiping back to the first tab can be hijacked by
        // DrawerLayout's left-edge gesture on some devices (gesture navigation / larger edge size).
        // Lock the drawer when we are not on the first page so ViewPager can receive the gesture.
    }

    public void forceRefresh() {
        try {
            mFragments[baseBind.viewPager.getCurrentItem()].forceRefresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return true if the internal tab ViewPager is currently on the first tab ("推荐作品").
     * Used by MainActivity to decide whether a right-swipe should open the left drawer.
     */
    public boolean isOnRecommendWorksTab() {
        try {
            return baseBind != null
                    && baseBind.viewPager != null
                    && baseBind.viewPager.getCurrentItem() == 0;
        } catch (Throwable ignore) {
            return false;
        }
    }
}
