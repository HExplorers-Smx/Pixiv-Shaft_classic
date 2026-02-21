package ceui.lisa.fragments;

import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.reflect.TypeToken;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ceui.lisa.R;
import ceui.lisa.activities.Shaft;
import ceui.lisa.adapters.FileNameAdapter;
import ceui.lisa.databinding.FragmentNovelFileNameBinding;
import ceui.lisa.download.FileCreator;
import ceui.lisa.interfaces.OnItemClickListener;
import ceui.lisa.model.CustomFileNameCell;
import ceui.lisa.models.NovelBean;
import ceui.lisa.models.NovelSeriesItem;
import ceui.lisa.utils.Common;
import ceui.lisa.utils.DensityUtil;
import ceui.lisa.utils.Local;
import ceui.lisa.utils.Params;
import ceui.lisa.view.LinearItemDecoration;

/**
 * 自定义小说下载文件名
 * 逻辑与 FragmentFileName（插画命名）一致，只是字段不同。
 */
public class FragmentNovelFileName extends SwipeFragment<FragmentNovelFileNameBinding> {

    private final List<CustomFileNameCell> allItems = new ArrayList<>();
    private FileNameAdapter mAdapter;

    private NovelBean exampleNovel;
    private NovelSeriesItem exampleSeries;

    public static FragmentNovelFileName newInstance() {
        return new FragmentNovelFileName();
    }

    @Override
    protected void initLayout() {
        mLayoutID = R.layout.fragment_novel_file_name;
    }

    @Override
    protected void initView() {
        baseBind.toolbar.toolbar.setNavigationOnClickListener(v -> mActivity.finish());
        baseBind.toolbar.toolbar.setTitle(R.string.custom_novel_file_name);

        baseBind.showNow.setOnClickListener(v -> showPreview());
        baseBind.saveNow.setOnClickListener(v -> saveSettings());
        baseBind.reset.setOnClickListener(v -> {
            if (mAdapter != null) {
                allItems.clear();
                allItems.addAll(FileCreator.defaultNovelFileCells());
                mAdapter.notifyDataSetChanged();
                showPreview();
            }
        });

        exampleNovel = Shaft.sGson.fromJson(Params.EXAMPLE_NOVEL, NovelBean.class);
        exampleSeries = new NovelSeriesItem();
        exampleSeries.setId(11223344);
        exampleSeries.setTitle("系列标题");
        exampleSeries.setContent_count(12);
    }

    @Override
    protected void initData() {
        allItems.clear();
        List<CustomFileNameCell> defaults = FileCreator.defaultNovelFileCells();
        String json = Shaft.sSettings.getNovelFileNameJson();
        if (TextUtils.isEmpty(json)) {
            allItems.addAll(defaults);
        } else {
            Collection<? extends CustomFileNameCell> customCells = Shaft.sGson.fromJson(json,
                    new TypeToken<List<CustomFileNameCell>>() {}.getType());
            allItems.addAll(customCells);
            Set<Integer> set = allItems.stream().map(CustomFileNameCell::getCode).collect(Collectors.toSet());
            allItems.addAll(defaults.stream().filter(it -> !set.contains(it.getCode())).collect(Collectors.toList()));
        }

        mAdapter = new FileNameAdapter(allItems, mContext);
        baseBind.recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        baseBind.recyclerView.setNestedScrollingEnabled(true);
        baseBind.recyclerView.addItemDecoration(new LinearItemDecoration(DensityUtil.dp2px(12.0f)));
        baseBind.recyclerView.setAdapter(mAdapter);

        new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                return makeMovementFlags(dragFlags, 0);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                Collections.swap(allItems, viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }

            @Override
            public void onMoved(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, int fromPos,
                                 @NonNull RecyclerView.ViewHolder target, int toPos, int x, int y) {
                super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
                mAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                showPreview();
            }
        }).attachToRecyclerView(baseBind.recyclerView);

        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position, int viewType) {
                showPreview();
            }
        });

        showPreview();
    }

    private void showPreview() {
        String novelName = FileCreator.customNovelFileNameForPreview(exampleNovel, allItems);
        String seriesName = FileCreator.customNovelSeriesFileNameForPreview(exampleSeries, allItems);
        baseBind.fileNameNovel.setText(novelName);
        baseBind.fileNameSeries.setText(seriesName);
    }

    private void saveSettings() {
        String json = Shaft.sGson.toJson(allItems);
        Shaft.sSettings.setNovelFileNameJson(json);
        Local.setSettings(Shaft.sSettings);
        Common.showToast("保存成功！");
    }

    @Override
    public SmartRefreshLayout getSmartRefreshLayout() {
        return baseBind.refreshLayout;
    }
}
