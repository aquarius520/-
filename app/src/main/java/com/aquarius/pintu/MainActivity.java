package com.aquarius.pintu;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.aquarius.pintu.core.ResultActionListener;
import com.aquarius.pintu.utils.BitmapHelper;
import com.aquarius.pintu.utils.ScreenUtil;
import com.aquarius.pintu.view.PintuLayout;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity implements ResultActionListener {

    private static final int GAME_SPEND_TIME_MSG = 100;
    private static final String TAG = "pintu";

    private ImageView mWholeImageView;
    private PintuLayout mGameContainer;
    private ActionBar mActionBar;
    private boolean isAlreadyStarted;   // 游戏是否开始 为了计时
    private boolean isGameSucceed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        createWholeImageView();
        ((RelativeLayout)findViewById(R.id.root)).addView(mWholeImageView);
        mGameContainer = (PintuLayout) findViewById(R.id.pingtu_container);
        mGameContainer.setResultActionListener(this);
        initActionbar();
        showStartGameDialog();
    }




    @Override
    protected void onResume() {
        super.onResume();
        if(isAlreadyStarted) {
            calculateGameTime();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
    }

    private void initActionbar() {
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == GAME_SPEND_TIME_MSG) {
                mActionBar.setTitle((String)msg.obj);
            }
            super.handleMessage(msg);
        }
    };

    private void showStartGameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("开始游戏？");
        builder.setMessage("开始后会在标题栏显示游戏消耗的时间。");
        builder.setCancelable(false);
        /*builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });*/
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                calculateGameTime();
            }
        });
        builder.create().show();
    }

    private ScheduledExecutorService executorService;

    private void calculateGameTime(){
        isAlreadyStarted = true;
        if (executorService == null) {
            executorService = Executors.newSingleThreadScheduledExecutor();
        }
        executorService.scheduleAtFixedRate(new CalculateTimeTask(), 0, 1000, TimeUnit.MILLISECONDS);
    }

    private String mSpendTime ;
    private int mSecond;
    private int mMinute;

    @Override
    public void whenGameSucceed() {
        isGameSucceed = true;
        showGameSucceedDialog();
    }

    private class CalculateTimeTask implements Runnable{

        @Override
        public void run() {
            if (isGameSucceed) {
                handleAfterGameOver();
                return;
            }
            mSecond++;
            if (mSecond == 60) {
                mMinute++;
                mSecond = 0;
            }

            mSpendTime = addZeroIfNeed(mMinute) + ":" + addZeroIfNeed(mSecond);
            mHandler.obtainMessage(GAME_SPEND_TIME_MSG, mSpendTime).sendToTarget();
        }
    }

    private void handleAfterGameOver() {
        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
    }

    private String addZeroIfNeed(int value) {
        if((value+"").length() == 1){
            return "0" + String.valueOf(value);
        }
        return String.valueOf(value);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pintu_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.resume_game:
                resetGameValues( );
                mGameContainer.resetGameAllInfo();
                break;
            case R.id.pause_game:
                showPauseGameDialog();
                break;

            case R.id.show_source:
                mGameContainer.showWholeGameResource(this, mWholeImageView);
                break;
            case R.id.show_pic2:
                mGameContainer.changeGameResource(this, R.mipmap.source_002);
                resetGameValues();
                mGameContainer.resetGameAllInfo();
                break;
            case R.id.show_pic3:
                mGameContainer.changeGameResource(this, R.mipmap.source_003);
                resetGameValues();
                mGameContainer.resetGameAllInfo();
                break;
            case R.id.select_from_gallery:
                selectPicFromGallery();
                break;

            case R.id.download_from_network:
                downloadPicFromNet();
            break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private static final int SELECT_PIC_ABOVE_KITKAT = 100 ; //  >= 4.4 版本
    private static final int SELECT_PIC = 0 ; // 4.4 以下
    private Bitmap mNewBitmap;

    private void selectPicFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra("crop", true);
        // 区别是他们返回的Uri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            startActivityForResult(intent, SELECT_PIC_ABOVE_KITKAT);
        } else {
            startActivityForResult(intent, SELECT_PIC);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PIC_ABOVE_KITKAT || requestCode == SELECT_PIC) {
            // etc: level6.0 uri = content://com.android.providers.media.documents/document/image%3A10421
            // etc: level4.1.1 uri = content://media/external/images/media/183277
            Uri uri = data.getData();
            ContentResolver cr  = getContentResolver();
            try {
                InputStream is = cr.openInputStream(uri);
                mNewBitmap = BitmapHelper.acquireCompressedBitmapIfNeed(is, this);
//                mNewBitmap = BitmapFactory.decodeStream(is, null, options);
                mGameContainer.changeGameResource(this, mNewBitmap);
                resetGameValues();
                mGameContainer.resetGameAllInfo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private ProgressDialog progressDialog;

    private void downloadPicFromNet() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请输入网络图片的完整地址");
        final EditText editText = new EditText(this);
        builder.setView(editText);
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                new AsyncTask<String, Void, Bitmap>() {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        progressDialog = ProgressDialog.show(MainActivity.this, null, "图片下载中...");
                    }

                    @Override
                    protected Bitmap doInBackground(String... params) {
                        String imageUrl = params[0];
                        try {
                            URL url = new URL(imageUrl);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setReadTimeout(60 * 1000);
                            conn.setConnectTimeout(6 * 1000);
                            conn.setUseCaches(true);
                            conn.connect();
                            if (conn.getResponseCode() >= 200 && conn.getResponseCode() < 299) {
                                InputStream is = conn.getInputStream();
                                return BitmapHelper.acquireCompressedBitmapIfNeed(is, PintuApplication.getInstance());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                        super.onPostExecute(bitmap);
                        progressDialog.dismiss();
                        if (bitmap == null) {
                            return;
                        }
                        mGameContainer.changeGameResource(PintuApplication.getInstance(), bitmap);
                        resetGameValues();
                        mGameContainer.resetGameAllInfo();
                    }
                }.execute(editText.getText().toString().trim());

            }
        });

        builder.create().show();
    }

    private void resetGameValues() {
        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
        mSecond = 0;
        mMinute = 0;
        isAlreadyStarted = false;
        isGameSucceed = false;
        mSpendTime = null;
        calculateGameTime();
    }

    private void showPauseGameDialog() {
        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("游戏已暂停");
        builder.setMessage("点击确定继续游戏。");
        builder.setCancelable(false);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                calculateGameTime();
            }
        });
        builder.create().show();
    }

    private void showGameSucceedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("恭喜");
        builder.setMessage("拼图成功。");
        builder.setCancelable(false);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }

    // 创建展示完整图的ImageView
    private void createWholeImageView() {
        mWholeImageView = new ImageView(this);
        int screenWidth = ScreenUtil.getScreenWidth(this);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                (int) (screenWidth * 1.0f /*0.9f*/), (int) (screenWidth * 1.0f /*0.9f*/));
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        mWholeImageView.setLayoutParams(lp);
        mWholeImageView.setVisibility(View.GONE);
        mWholeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGameContainer.showWholeGameResource(MainActivity.this, mWholeImageView);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
