package org.qxtx.idea.player.demo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.qxtx.idea.player.utils.IdeaToast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class ChooseActivity extends AppCompatActivity {
    public static final String EXTRAS_NAME = "playlist";
    public static final String FILE_NAME = "program_list.xls";

    private IdeaToast myToast;
    private RecyclerView rvlistView;
    private ArrayList<ItemBean> playlistData;
    private GenericsAdapter<ItemBean> playlistAdapter;

    private UDiskBroadcastReceiver receiver;

    private ExitDialog exitDialog;

    //移动飞框
    private ImageView viewFlyFrame;

    //动画
    private AnimationUtils a1;
    private AnimationUtils a2;
    private AnimationUtils a3;
    private AnimationUtils a4;

    private boolean shouldBeInit = true;
    private boolean canBeShowDialog = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

        myToast = IdeaToast.getInstance(ChooseActivity.this);

        initBroadcast();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            canBeShowDialog = true;
        }

        try {
            if (hasFocus && shouldBeInit) {
                init();
                shouldBeInit = false;
            }
        } catch (Exception e) {
            myToast.showToast("Something happened. Restart app?", Toast.LENGTH_LONG);
            finish();
        }
    }

    @Override
    protected void onStop() {
        Log.e("lyx_tag", "ChooseActivity@onStop()");
        canBeShowDialog = false;

        if (exitDialog != null) {
            exitDialog.cancel();
        }

        super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (exitDialog == null) {
                exitDialog = ExitDialog.getInstance(this);
            }

            if (canBeShowDialog) {
                exitDialog.show();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void init() {
        initData();
        initView();
    }

    private void initBroadcast() {
        receiver = new UDiskBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addDataScheme("file");

        registerReceiver(receiver, intentFilter);
    }

    private void initData() {
        if (playlistData != null) {
            playlistData.clear();
        }
        playlistData = new ArrayList<>();
        readPlayList();
    }

    private void initView() {
        viewFlyFrame = (ImageView)findViewById(R.id.flyFrame);
        rvlistView = findViewById(R.id.playlist);
        int colum = 8;
        if (playlistData.size() < 9) {
            colum = playlistData.size();
        }
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, colum, GridLayoutManager.VERTICAL, false);

        if (playlistAdapter == null) {
            playlistAdapter = initAdapter();
        }

        rvlistView.setLayoutManager(gridLayoutManager);
        rvlistView.setItemAnimator(null);
        rvlistView.setAdapter(playlistAdapter);
        rvlistView.addItemDecoration(new GenericsAdapter.DefaultItemDecoration(0, 30, 0, 30));
    }

    //读取资源列表
    private void readPlayList() {
        char pathFlag = 'a';
        String basePath = "storage/sd";
        File file = null;
        //适配公司安卓7.0盒子，发现U盘
        for (int i = 0; i < 26; i++) {
            for (int j = -1; j < 10; j++) {
                file = (j == -1) ? new File(basePath + pathFlag + "/" + FILE_NAME) : new File(basePath + pathFlag + j + "/" + FILE_NAME);
                if (file.exists()) {
                    break;
                }
            }
            pathFlag = (char)((int)pathFlag + i);
        }

        //保存到本地
        if (!file.exists()) {
            myToast.showToast("Not playlist or UDisk was found", Toast.LENGTH_LONG);
            file = new File(Environment.getExternalStorageDirectory(), FILE_NAME);
        } else {
            myToast.showToast("Found playlist in UDisk-" + file.getAbsolutePath() + ". Save to localFile", Toast.LENGTH_LONG);
            FileInputStream in = null;
            try {
                in = new FileInputStream(file);
                saveToSdcard(in, file);
            } catch (FileNotFoundException e) {
                myToast.showToast("Save playlist failed, restart app?", Toast.LENGTH_LONG);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        //应急措施：读取应用内置列表
        if (!file.exists()) {
            myToast.showToast("Didn't find playlist in SD card, save the internal playlist to SD card", Toast.LENGTH_LONG);
            try {
                InputStream in = getAssets().open(FILE_NAME);
                saveToSdcard(in, file);
            } catch (IOException e) {
                finish();
            }
        } else {
            myToast.showToast("Read playlist successfully", Toast.LENGTH_LONG);
        }

        //读取xls列表中内容
        try {
            Workbook workbook = Workbook.getWorkbook(file);
            Sheet sheet = workbook.getSheet(0);
            saveXlsData(sheet); //读取资源列表
            workbook.close();
        } catch (IOException | BiffException e) {
            e.printStackTrace();
        }
    }

    //保存到本地文件
    private void saveToSdcard(InputStream in, File file) {
        try {
            file = new File(Environment.getExternalStorageDirectory(), FILE_NAME);
            if (file.exists()) {
                file.delete();
            }

            boolean success = file.createNewFile();
            if (!success) {
                throw new NullPointerException();
            }

            FileOutputStream os = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            BufferedInputStream bis = new BufferedInputStream(in);
            int read;
            while ((read = bis.read()) != -1) {
                bos.write(read);
                bos.flush();
            }

            bis.close();
            bos.close();
            in.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //获取xls文件数据
    private void saveXlsData(Sheet sheet) {
        int rowCounter = getRowCount(sheet);
        if (rowCounter < 2) {
            return ;
        }

        if (playlistData != null) {
            playlistData.clear();
        }

        if (sheet.getColumns() < 3) {
            return ;
        }
        for (int i = 1; i < rowCounter; i++) {
            String name = sheet.getCell(0, i).getContents();
            String url = sheet.getCell(2, i).getContents();
            String serverLicenseUrl = sheet.getCell(3, i).getContents();
            name = name == null ? "" : name;
            url = url == null ? "" : url;
            serverLicenseUrl = (serverLicenseUrl == null || serverLicenseUrl.equals("")) ? null : serverLicenseUrl;

            playlistData.add(new ItemBean(name, url, serverLicenseUrl));
        }
    }

    //得到最后一行的行数
    private int getRowCount(Sheet sheet) {
        int rsCols = sheet.getColumns(); // 列数
        int rsRows = sheet.getRows(); // 行数
        int nullCellNum;
        int afterRows = rsRows;
        for (int i = 1; i < rsRows; i++) { // 统计行中为空的单元格数
            nullCellNum = 0;
            for (int j = 0; j < rsCols; j++) {
                String val = sheet.getCell(j, i).getContents();
                if (val == null || val.equals(""))
                    nullCellNum++;
            }
            if (nullCellNum >= rsCols) { // 如果nullCellNum大于或等于总的列数
                afterRows--; // 行数减一
            }
        }
        return afterRows;
    }

    private GenericsAdapter<ItemBean> initAdapter() {
        return new GenericsAdapter<ItemBean>(this, R.layout.choose_item, playlistData) {
            @Override
            public void onBind(MyHolder viewHolder, int pos, List<ItemBean> data) {
                LinearLayout item = (LinearLayout)viewHolder.getView(R.id.item);
                TextView textView = (TextView)item.findViewById(R.id.name);
                textView.setText(playlistData.get(pos).getName());

                item.setOnClickListener(new ItemClickListener(pos));
                item.setOnFocusChangeListener(new ItemFocusListener());
            }
        };
    }

    private void endAnimation() {
        if (a1 != null && a1.isRunning()) {
            a1.endAnimation();
        }
        if (a2 != null && a2.isRunning()) {
            a2.endAnimation();
        }
        if (a3 != null && a3.isRunning()) {
            a3.endAnimation();
        }
        if (a4 != null && a4.isRunning()) {
            a4.endAnimation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exitDialog != null) {
            exitDialog.cancel();
            exitDialog = null;
        }

        endAnimation();

        if (myToast != null) {
            myToast.cancel();
            myToast = null;
        }

        rvlistView = null;
        playlistAdapter = null;
        if (playlistData != null) {
            playlistData.clear();
            playlistData = null;
        }

        if (receiver != null) {
            unregisterReceiver(receiver);
        }

        System.gc();
        System.runFinalization();
        System.gc();
    }

    private final class ItemClickListener implements View.OnClickListener {
        private int pos;

        ItemClickListener(int pos) {
            this.pos = pos;
        }

        @Override
        public void onClick(View v) {
            endAnimation();

            if (v.isFocusable() && !v.isFocused()) {
                v.requestFocus();
            }

            ImageView imageView = (ImageView)v.findViewById(R.id.icon);
            a1 = new AnimationUtils(viewFlyFrame);
            a1.startScale(500, 1f, 0.6f, 1f);
            a2 = new AnimationUtils(imageView);
            a2.startScale(500, 1f, 1.2f, 1f);

            Intent intent = new Intent(ChooseActivity.this, PlayerActivity.class);
            intent.putExtra(EXTRAS_NAME, playlistData.get(pos));
            new Handler().postDelayed(() -> {
                startActivity(intent);
            }, 400);
        }
    }

    private final class ItemFocusListener implements View.OnFocusChangeListener {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            TextView tv = (TextView)v.findViewById(R.id.name);

            if (hasFocus) {
                moveFlyFrame(v);
                tv.setSelected(true);
            } else {
                tv.setSelected(false);
            }
        }

        private void moveFlyFrame(View view) {
            endAnimation();

            if (viewFlyFrame.getVisibility() == View.GONE) {
                viewFlyFrame.setVisibility(View.VISIBLE);
            }

            int srcW = viewFlyFrame.getWidth();
            int srcH = viewFlyFrame.getHeight();
            int destW = view.getWidth() + 60;
            int destH = view.getHeight() + 60;
            int[] srcPos = new int[2];
            int[] destPos = new int[2];
            viewFlyFrame.getLocationInWindow(srcPos);
            view.getLocationInWindow(destPos);

            a1 = new AnimationUtils(viewFlyFrame);
            a2 = new AnimationUtils(viewFlyFrame);
            a3 = new AnimationUtils(viewFlyFrame);
            a4 = new AnimationUtils(viewFlyFrame);

            a1.set("translationX", 250, srcPos[0], destPos[0] - 30);
            a2.set("translationY", 250, srcPos[1], destPos[1] - 30);
            a3.set("width", 250, srcW, destW);
            a4.set("height", 250, srcH, destH / 5f, destH);
            a1.start();
            a2.start();
            a3.start();
            a4.start();
        }
    }

    private final class UDiskBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                String data = intent.getDataString();
                new Handler().postDelayed(() -> updatePlayList(data), 1000);
            }
        }

        private void updatePlayList(String data) {
            try {
                String path = data.substring(data.indexOf("///") + 2) + "/" + FILE_NAME;
                File file = new File(path);
                if (file.exists()) {
                    myToast.showToast("Playlist was found in UDisk. Save to local file", Toast.LENGTH_LONG);
                    FileInputStream in = null;
                    try {
                        in = new FileInputStream(file);
                        saveToSdcard(in, file);
                        in.close();

                        //读取xls列表中内容
                        Workbook workbook = Workbook.getWorkbook(file);
                        Sheet sheet = workbook.getSheet(0);
                        saveXlsData(sheet); //读取资源列表
                        workbook.close();
                        playlistAdapter.notifyDataSetChanged();
                        myToast.showToast("Update playlist", Toast.LENGTH_LONG);
                    } catch (IOException | BiffException e) {
                        myToast.showToast("Unable to save playlist, please try to re insert UDisk", Toast.LENGTH_LONG);
                        e.printStackTrace();
                    }
                } else {
                    MyLog.e("Playlist was not exist");
                }
            } catch (Exception e) {
                myToast.showToast("Unable to read UDisk, please try to re insert UDisk", Toast.LENGTH_LONG);
            }
        }
    }

    private static final class ExitDialog {
        private static ExitDialog exitDialog;
        private static AlertDialog dialog;
        private WeakReference<Context> context;
        private String msg;
        private String title;
        private int iconId;
        private int layoutId;

        private ExitDialog(Context context) {
            this.context = new WeakReference<Context>(context);
            msg = "Are you sure Exit IdeaPlayer?";
            title = "Exit";
            iconId = R.mipmap.dialog_exit;
            layoutId = 0;
        }

        static ExitDialog getInstance(Context context) {
            if (exitDialog == null) {
                synchronized (ExitDialog.class) {
                    if (exitDialog == null) {
                        exitDialog = new ExitDialog(context);
                    }
                }
            }
            return exitDialog;
        }

        ExitDialog setMessage(String msg) {
            this.msg = msg;
            return this;
        }

        ExitDialog setTitle(String title) {
            this.title = title;
            return this;
        }

        ExitDialog setIcon(int iconId) {
            this.iconId = iconId;
            return this;
        }

        ExitDialog setLayout(int layoutId) {
            this.layoutId = layoutId;
            return this;
        }

        void cancel() {
            if (dialog != null) {
                dialog.cancel();
                dialog = null;
            }

            //必须释放这个东西，不然第二次运行app后会导致问题
            exitDialog = null;
        }

        void show() {
            if (isShowing()) {
                MyLog.e("dialog is showing!");
                return ;
            }

            if (context == null || context.get() == null) {
                MyLog.e("context null!");
                return ;
            }

            dialog = new AlertDialog.Builder(context.get())
                    .setMessage(msg)
                    .setTitle(title)
                    .setIcon(iconId)
                    .setNeutralButton("No", (dialog, which) -> dialog.cancel())
                    .setPositiveButton("Yes", (dialog, which) -> {
                        dialog.cancel();
                        if (context == null || context.get() == null) {
                            System.exit(0);
                            return;
                        }
                        ((Activity) context.get()).finish();
                    })
                    .create();
            if (!((Activity)context.get()).isFinishing()) {
                dialog.show();
            } else {
                System.exit(0);
            }
        }

        boolean isShowing() {
            return dialog != null && dialog.isShowing();
        }
    }
}
