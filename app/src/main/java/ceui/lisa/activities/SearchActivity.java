package ceui.lisa.activities;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mxn.soul.flowingdrawer_core.ElasticDrawer;
import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;
import ceui.lisa.R;
import ceui.lisa.databinding.FragmentNewSearchBinding;
import ceui.lisa.fragments.BaseFragment;
import ceui.lisa.fragments.FragmentFilter;
import ceui.lisa.fragments.FragmentSearchIllust;
import ceui.lisa.fragments.FragmentSearchNovel;
import ceui.lisa.fragments.FragmentSearchUser;
import ceui.lisa.utils.Common;
import ceui.lisa.utils.DensityUtil;
import ceui.lisa.utils.Params;
import ceui.lisa.viewmodel.SearchModel;

public class SearchActivity extends BaseActivity<FragmentNewSearchBinding> {

    private final BaseFragment<?>[] allPages = new BaseFragment[]{null, null,null};
    private FragmentFilter fragmentFilter;
    private String keyWord = "";
    private SearchModel searchModel;
    private int index = 0;
    private int mPosition = 0;
    private boolean isPremium = false;

    @Override
    protected void initBundle(Bundle bundle) {
        keyWord = bundle.getString(Params.KEY_WORD);
        index = bundle.getInt(Params.INDEX);
        searchModel = new ViewModelProvider(this).get(SearchModel.class);
        searchModel.getKeyword().setValue(keyWord);
        searchModel.getIsNovel().setValue(index == 1);

        isPremium = Shaft.sUserModel.getUser().isIs_premium();
        searchModel.getIsPremium().setValue(isPremium);

//        searchModel.getNowGo().observe(this, new Observer<String>() {
//            @Override
//            public void onChanged(String s) {
//                baseBind.drawerlayout.closeMenu(true);
//            }
//        });
    }

    @Override
    protected int initLayout() {
        return R.layout.fragment_new_search;
    }

    @Override
    protected void initView() {
        final String[] TITLES = new String[]{
                getString(R.string.string_136),
                getString(R.string.string_138),
                getString(R.string.string_432)
        };
        // AppBar 已移动到底部，这里 head 用作“底部系统导航栏/手势条”占位，默认 0
        try {
            ViewGroup.LayoutParams headParams = baseBind.head.getLayoutParams();
            headParams.height = 0;
            baseBind.head.setLayoutParams(headParams);
        } catch (Exception ignore) { }

        // 结果页只展示结果：不再混入“搜索历史/热门标签/底部搜索框”等搜索首页内容
        baseBind.toolbar.setTitle(keyWord);

        baseBind.viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager(), 0) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                if (allPages[position] == null) {
                    if (position == 0) {
                        allPages[position] = FragmentSearchIllust.newInstance();
                    } else if(position == 1){
                        allPages[position] = FragmentSearchNovel.newInstance();
                    } else if(position == 2){
                        allPages[position] = FragmentSearchUser.newInstance(keyWord);
                    }
                }

                return allPages[position];
            }

            @Override
            public int getCount() {
                return TITLES.length;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return TITLES[position];
            }
        });
        baseBind.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                // 通知更改 过滤器-关键字匹配 类型
                if (fragmentFilter != null) {
                    mPosition = position;
                    // 搜索结果页：允许左右滑动在【插画/小说/作者】之间切换。
                    // 为了不干扰“右滑切换上一项”的手势，这里禁用侧栏的手势滑出。
                    // 侧栏仍然可以通过右上角的筛选按钮打开。
                    baseBind.drawerlayout.setTouchMode(ElasticDrawer.TOUCH_MODE_NONE);
                    if (baseBind.drawerlayout.isMenuVisible()) {
                        baseBind.drawerlayout.closeMenu(true);
                    }

                    MutableLiveData<Boolean> isNovel = searchModel.getIsNovel();
                    if (isNovel.getValue() != null) {
                        if ((position == 0) && isNovel.getValue()) {
                            isNovel.setValue(false);
                        } else if (position == 1 && !isNovel.getValue()) {
                            isNovel.setValue(true);
                        }
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        baseBind.viewPager.setOffscreenPageLimit(2);
        baseBind.tabLayout.setupWithViewPager(baseBind.viewPager);
        // 搜索结果页禁用“手势滑出侧栏”，避免和 ViewPager 左右切页冲突。
        baseBind.drawerlayout.setTouchMode(ElasticDrawer.TOUCH_MODE_NONE);
        if (index != 0) {
            baseBind.viewPager.setCurrentItem(index);
        }

        if (Shaft.getMMKV().decodeBool(Params.MMKV_KEY_ISSHOWTIPS_SEARCHSORT, true)) {
            tipDialog(mContext);
            baseBind.drawerlayout.openMenu(true);
        }
    }

    @Override
    protected void initData() {
        // 搜索结果页的 AppBar 已整体移动到底部，返回箭头会占位置且容易误触。
        // 这里直接移除导航图标与点击监听；返回使用系统手势/返回键即可。
        baseBind.toolbar.setNavigationIcon(null);
        baseBind.toolbar.inflateMenu(R.menu.illust_filter);
        baseBind.toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_filter) {
                    Common.hideKeyboard(mActivity);
                    if (mPosition == 0 || mPosition == 1) {
                        if (baseBind.drawerlayout.isMenuVisible()) {
                            baseBind.drawerlayout.closeMenu(true);
                        } else {
                            baseBind.drawerlayout.openMenu(true);
                        }
                    } else {
                        Common.showToast(getString(R.string.string_435));
                    }
                    return true;
                }
                return false;
            }
        });

        // 结果页不再放“输入框/搜索历史/热门标签”，搜索入口保留在搜索首页（FragmentSearch）

        fragmentFilter = new FragmentFilter();
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (!fragmentFilter.isAdded()) {
            fragmentManager.beginTransaction()
                    .add(R.id.id_container_menu, fragmentFilter)
                    .commitNowAllowingStateLoss();
        } else {
            fragmentManager.beginTransaction()
                    .show(fragmentFilter)
                    .commitNowAllowingStateLoss();
        }
    }


    private void tipDialog(Context context){
        QMUIDialog qmuiDialog = new QMUIDialog.MessageDialogBuilder(context)
                .setTitle(context.getString(R.string.string_433))
                .setMessage(context.getString(R.string.string_434))
                .setSkinManager(QMUISkinManager.defaultInstance(context))
                .addAction(context.getString(R.string.string_190), new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        Shaft.getMMKV().encode(Params.MMKV_KEY_ISSHOWTIPS_SEARCHSORT, false);
                        dialog.dismiss();
                    }
                })
                .create();
        qmuiDialog.show();
    }
}
