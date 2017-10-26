package com.aquarius.pintu.view;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.aquarius.pintu.R;
import com.aquarius.pintu.core.GameController;
import com.aquarius.pintu.core.ResultActionListener;
import com.aquarius.pintu.entity.ImageItem;
import com.aquarius.pintu.utils.ImageSplitUtil;
import com.aquarius.pintu.utils.ScreenUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Created by aquarius on 2017/6/5.
 */
public class PintuLayout extends RelativeLayout implements View.OnClickListener{

    private Bitmap mSourceBitmap;
    private int mItemWidth;     // 每个小块的宽度 宽高一致
    private int mScreenWidth;
    private int mScreenHeight;
    private int mGamePanelWidth;

    private int mTouchSlop;     // 最小认为滑动距离

    private int mColumn = 3;
    private int mInnerMargin;   // 图片块之间的间距 dp值

    private List<ImageItem> mImageItemList;
    private List<ImageView> mGamePintuItems;

    private boolean isExchangeAniming = false;  // 图片交换过程中
    private boolean mWholeImgShowing = false;
    private Context mContext;
    private ResultActionListener mListener;

    public PintuLayout(Context context) {
        this(context, null, 0);
    }

    public PintuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PintuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mInnerMargin = ScreenUtil.dp2px(3, context);
        mScreenWidth = ScreenUtil.getScreenWidth(context);
        mScreenHeight = ScreenUtil.getScreenHeight(context);
        mGamePanelWidth = Math.min(mScreenWidth, mScreenHeight);
        mItemWidth = (mScreenWidth - getPaddingLeft() - getPaddingRight() - mInnerMargin * (mColumn - 1)) / mColumn;
        initGameBitmap(mContext, mColumn);
        initGameItemViews(mContext);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mGamePanelWidth, mGamePanelWidth);
    }

    private void initGameBitmap(Context context, int column) {
        if (mSourceBitmap == null) {
            mSourceBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.source_004);
            mImageItemList = ImageSplitUtil.splitImageToPieces(context, mSourceBitmap, column);
            GameController.elementsGenerator(mImageItemList, mColumn);
        }
    }

    private void initGameItemViews(Context context) {
        mGamePintuItems = new ArrayList<ImageView>(mColumn * mColumn);
        for (int i = 0; i < mImageItemList.size(); i++) {
            ImageView item = new ImageView(context);
            item.setOnClickListener(this);
            item.setImageBitmap(mImageItemList.get(i).getBitmap());
            item.setId(i+1);    // 设置view的id
            item.setTag(i + "-" + mImageItemList.get(i).getIndex());    // 设置tag 存储真正的位置信息
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mItemWidth, mItemWidth);

            // 不是第一列
            if (i % mColumn != 0) {
                params.leftMargin = mInnerMargin;
                params.addRule(RelativeLayout.RIGHT_OF, mGamePintuItems.get(i-1).getId());
            }

            // 不是第一行
            if(i / mColumn > 0) {
                params.topMargin = mInnerMargin;
                params.addRule(RelativeLayout.BELOW, mGamePintuItems.get(i - mColumn).getId());
            }
            mGamePintuItems.add(item);

            addView(item, params);
        }

    }


    private ImageView mFirstClickItem;
    private ImageView mBlankViewItem;
    private RelativeLayout mAnimLayout;

    @Override
    public void onClick(View v) {
        if (isExchangeAniming) {
            return;
        }
        // 两次点击同一个Item
        if (mFirstClickItem == v)
        {
            //mFirstClickItem.setColorFilter(null);
            mFirstClickItem = null;
            return;
        }
        if (mFirstClickItem == null) {
            mFirstClickItem = (ImageView) v;
            //mFirstClickItem.setColorFilter(Color.parseColor("#550000FF"));
        }
        ImageView item = (ImageView) v;
        int clickPosition = getItemPosition(getImageIndexByTag(item.getTag()), mImageItemList);
        int blankPosition = GameController.getBlankItemPosition(mImageItemList);

        mBlankViewItem = mGamePintuItems.get(blankPosition);
        boolean canMove = GameController.isMoveable(clickPosition, blankPosition, mColumn);
//        Toast.makeText(mContext, "clickposi =" + clickPosition
//                + " blankPosi="+blankPosition + " move is = " + canMove, Toast.LENGTH_SHORT).show();

        if (canMove) {
            exchangeImageView(clickPosition, blankPosition);
            boolean isGameSucceed = GameController.isSuccess(mImageItemList);
            if (isGameSucceed) {
                if (mListener != null) {
                    mListener.whenGameSucceed();
                }
            }
        }else {
            //mFirstClickItem.setColorFilter(null);
            mFirstClickItem = null;
        }
    }

    private void exchangeImageView(final int clickPosition, int blankPosition) {
        mFirstClickItem.setColorFilter(null);
        mAnimLayout = new RelativeLayout(getContext());
        addView(mAnimLayout);
        ImageView clickView = new ImageView(mContext);
        String data = (String)mFirstClickItem.getTag();
        final Bitmap bitmapOfClickView = mImageItemList.get(clickPosition).getBitmap();
        clickView.setImageBitmap(bitmapOfClickView);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(mItemWidth, mItemWidth);
        lp.leftMargin = mFirstClickItem.getLeft() - getPaddingLeft();
        lp.topMargin = mFirstClickItem.getTop() - getPaddingTop();
        clickView.setLayoutParams(lp);
        mAnimLayout.addView(clickView);

        final ImageView tempBlankView = new ImageView(getContext());
        final Bitmap blankBitmap = mImageItemList.get(blankPosition).getBitmap();
        tempBlankView.setImageBitmap(blankBitmap);
        LayoutParams lp2 = new LayoutParams(mItemWidth, mItemWidth);
        lp2.leftMargin = mBlankViewItem.getLeft() - getPaddingLeft();
        lp2.topMargin = mBlankViewItem.getTop() - getPaddingTop();
        tempBlankView.setLayoutParams(lp2);
        mAnimLayout.addView(tempBlankView);

        // animation
        TranslateAnimation animation = new TranslateAnimation(
                0, mBlankViewItem.getLeft() - mFirstClickItem.getLeft(),
                0, mBlankViewItem.getTop() - mFirstClickItem.getTop());
        animation.setDuration(300);
        animation.setFillAfter(true);
        clickView.startAnimation(animation);

        TranslateAnimation animation2 = new TranslateAnimation(
                0, mFirstClickItem.getLeft() - mBlankViewItem.getLeft(),
                0, mFirstClickItem.getTop() - mBlankViewItem.getTop());
        animation2.setDuration(300);
        animation2.setFillAfter(true);
        tempBlankView.startAnimation(animation2);

        // 交换ImageItem中的信息
        ImageItem firstImageItem = mImageItemList.get(clickPosition);   // 点击的view
        ImageItem tempImageItem = new ImageItem(firstImageItem.getIndex(),
                firstImageItem.getBitmap(), firstImageItem.isBlank());
        ImageItem secondImageItem = mImageItemList.get(blankPosition);  // 空白的view
        mImageItemList.set(clickPosition, secondImageItem);
        mImageItemList.set(blankPosition, tempImageItem);


        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mFirstClickItem.setVisibility(View.INVISIBLE);
                mBlankViewItem.setVisibility(View.INVISIBLE);
                isExchangeAniming = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                String firstTag = (String)mFirstClickItem.getTag();
                String secondTag = (String)mBlankViewItem.getTag();

                // 交换ImageView相关信息
                mFirstClickItem.setImageBitmap(blankBitmap);
                mBlankViewItem.setImageBitmap(bitmapOfClickView);
                mFirstClickItem.setTag(secondTag);
                mBlankViewItem.setTag(firstTag);

                mFirstClickItem.setVisibility(View.VISIBLE);
                mBlankViewItem.setVisibility(View.VISIBLE);

                mFirstClickItem = null;
                mBlankViewItem = null;
                mAnimLayout.removeAllViews();
                isExchangeAniming = false;

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    public void changeGameResource(Context context, int newResId) {
        mSourceBitmap = BitmapFactory.decodeResource(context.getResources(), newResId);
        mImageItemList.clear();
        mImageItemList = ImageSplitUtil.splitImageToPieces(context, mSourceBitmap, mColumn);
    }

    public void changeGameResource(Context context, Bitmap bitmap) {
        mSourceBitmap = bitmap;
        mImageItemList.clear();
        mImageItemList = ImageSplitUtil.splitImageToPieces(context, bitmap, mColumn);
    }

    private void resetGameBitmap() {
        if (mSourceBitmap != null) {
            GameController.elementsGenerator(mImageItemList, mColumn);
        }
    }

    public void showWholeGameResource(Context context, ImageView imageview) {
        Animation animShow = AnimationUtils.loadAnimation(
                context, R.anim.image_show_anim);
        Animation animHide = AnimationUtils.loadAnimation(
                context, R.anim.image_hide_anim);
        imageview.setImageBitmap(mSourceBitmap);
        if (mWholeImgShowing) {
            imageview.startAnimation(animHide);
            imageview.setVisibility(View.GONE);
            mWholeImgShowing = false;
        } else {
            imageview.startAnimation(animShow);
            imageview.setVisibility(View.VISIBLE);
            mWholeImgShowing = true;
        }
    }

    public void resetGameAllInfo() {
        resetGameBitmap();
        removeGameItemViews();
        initGameItemViews(mContext);
    }

    // 移除之前背景图添加到父控件上的view
    private void removeGameItemViews() {
        if (mGamePintuItems != null) {
            for (ImageView view : mGamePintuItems) {
                removeView(view);
            }
            mGamePintuItems.clear();
        }
    }

    public void setResultActionListener(ResultActionListener listener) {
        mListener = listener;
    }

    private int getItemPosition(int index, List<ImageItem> itemList){
        int position = 0;
        for(ImageItem item : itemList) {
            if (index == item.getIndex()) {
                break;
            }
            position++;
        }
        return position;
    }

    public int getImageIndexByTag(Object tag) {
        String[] split = ((String)tag).split("-");
        return Integer.parseInt(split[1]);
    }
}
