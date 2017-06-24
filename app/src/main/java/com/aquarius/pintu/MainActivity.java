package com.aquarius.pintu;

import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.aquarius.pintu.core.ResultActionListener;
import com.aquarius.pintu.utils.ScreenUtil;
import com.aquarius.pintu.view.PintuLayout;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements ResultActionListener {

    private static final int GAME_SPEND_TIME_MSG = 100;

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

        }
        return super.onOptionsItemSelected(item);
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


}
