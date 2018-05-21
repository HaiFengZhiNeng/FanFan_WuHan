package com.fanfan.robot.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.ArrayMap;
import android.view.Menu;
import android.view.MenuItem;

import com.fanfan.novel.adapter.TreeRecyclerAdapter;
import com.fanfan.novel.common.ChatConst;
import com.fanfan.novel.common.activity.BarBaseActivity;
import com.fanfan.novel.common.base.BaseRecyclerAdapter;
import com.fanfan.novel.service.animator.SlideInOutBottomItemAnimator;
import com.fanfan.novel.service.event.ChildItemEvent;
import com.fanfan.novel.service.item.LocalGroupItem;
import com.fanfan.novel.ui.recyclerview.tree.ViewHolder;
import com.fanfan.novel.ui.recyclerview.tree.base.BaseItemData;
import com.fanfan.novel.ui.recyclerview.tree.factory.ItemConfig;
import com.fanfan.novel.ui.recyclerview.tree.factory.ItemHelperFactory;
import com.fanfan.novel.ui.recyclerview.tree.item.TreeItem;
import com.fanfan.novel.ui.recyclerview.tree.item.TreeItemGroup;
import com.fanfan.novel.utils.DialogUtils;
import com.fanfan.robot.R;
import com.fanfan.robot.db.CheckInDBManager;
import com.fanfan.robot.model.CheckIn;
import com.fanfan.robot.model.Data;
import com.fanfan.robot.service.item.DataGroupItem;
import com.fanfan.robot.service.item.SingItem;
import com.iflytek.cloud.SpeechConstant;
import com.seabreeze.log.Print;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import butterknife.BindView;

/**
 * Created by android on 2018/1/10.
 */

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class SignAllActivity extends BarBaseActivity {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    public static void newInstance(Activity context) {
        Intent intent = new Intent(context, SignAllActivity.class);
        context.startActivity(intent);
        context.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private CheckInDBManager mCheckInDBManager;

    private TreeRecyclerAdapter mTreeRecyclerAdapter;

    private List<TreeItem> mTreeItems;

    private Set<Long> letters = new HashSet<>();
    private ArrayMap<Long, List<CheckIn>> inArrayMap = new ArrayMap<>();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_sing_all;
    }

    @Override
    protected void initData() {
        mCheckInDBManager = new CheckInDBManager();

        ItemConfig.addTreeHolderType(ChatConst.DATA, DataGroupItem.class);
        ItemConfig.addTreeHolderType(ChatConst.SING, SingItem.class);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new SlideInOutBottomItemAnimator(recyclerView));
        mTreeRecyclerAdapter = new TreeRecyclerAdapter();
        recyclerView.setAdapter(mTreeRecyclerAdapter);

        List<CheckIn> checkIns = mCheckInDBManager.loadAll();

        final List<BaseItemData> localBeanList = loadData(checkIns);
        mTreeItems = ItemHelperFactory.createTreeItemList(localBeanList, null);

        mTreeRecyclerAdapter.setDatas(mTreeItems);

        mTreeRecyclerAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ViewHolder viewHolder, int position) {
                BaseItemData itemData = (BaseItemData) mTreeRecyclerAdapter.getData(position).getData();

                if (itemData instanceof Data) {
                    Data data = (Data) itemData;
                    if (data.getData() > System.currentTimeMillis()) {
                        mTreeRecyclerAdapter.getItemManager().removeItem(position);
                    }
                } else if (itemData instanceof CheckIn) {
                    CheckIn in = (CheckIn) itemData;
//                    if (in.getTime() > System.currentTimeMillis()) {
                    showDialog(in, position);
//                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.home_black, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDialog(final CheckIn itemData, final int position) {
        DialogUtils.showBasicNoTitleDialog(this, "确定要删除此此条签到信息吗？", "取消", "确定",
                new DialogUtils.OnNiftyDialogListener() {
                    @Override
                    public void onClickLeft() {
                    }

                    @Override
                    public void onClickRight() {
                        mCheckInDBManager.delete(itemData);
                        mTreeRecyclerAdapter.getItemManager().removeItem(position);
                        //检查上一个
                        BaseItemData itemData = (BaseItemData) mTreeRecyclerAdapter.getData(position - 1).getData();

                        if (itemData instanceof Data) {
                            mTreeRecyclerAdapter.getItemManager().removeItem(position - 1);
                        }
                    }
                });

    }

    private List<BaseItemData> loadData(List<CheckIn> checkIns) {
        List<BaseItemData> localBeanList = new ArrayList<>();


        for (CheckIn checkIn : checkIns) {
            letters.add(checkIn.getTime());
            long time = checkIn.getTime();
            long zero = time / (1000 * 3600 * 24) * (1000 * 3600 * 24) - TimeZone.getDefault().getRawOffset();

            if (inArrayMap.containsKey(zero)) {
                List<CheckIn> ins = inArrayMap.get(zero);
                ins.add(checkIn);
            } else {
                List<CheckIn> ins = new ArrayList<>();
                ins.add(checkIn);
                inArrayMap.put(zero, ins);
            }
        }

        Iterator iterator = inArrayMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, List<CheckIn>> entry = (Map.Entry<Long, List<CheckIn>>) iterator.next();
            long key = entry.getKey();
            List<CheckIn> value = entry.getValue();
            Data data = new Data();

            data.setData(key);
            data.setViewItemType(ChatConst.DATA);
            for (CheckIn in : value) {
                in.setViewItemType(ChatConst.SING);
            }
            data.setSingleBeen(value);

            localBeanList.add(data);
        }
        return localBeanList;

    }

}
