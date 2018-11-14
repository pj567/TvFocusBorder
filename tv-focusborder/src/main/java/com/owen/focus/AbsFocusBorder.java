package com.owen.focus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by owen on 2017/7/20.
 */

public abstract class AbsFocusBorder extends View implements FocusBorder, ViewTreeObserver.OnGlobalFocusChangeListener{
    private static final long DEFAULT_ANIM_DURATION_TIME = 300;
    private static final long DEFAULT_SHIMMER_DURATION_TIME = 1000;
    
    protected long mAnimDuration = DEFAULT_ANIM_DURATION_TIME;
    protected long mShimmerDuration = DEFAULT_SHIMMER_DURATION_TIME;
    protected RectF mFrameRectF = new RectF();
    protected RectF mPaddingRectF = new RectF();
    protected RectF mPaddingOfsetRectF = new RectF();
    protected RectF mTempRectF = new RectF();

    private LinearGradient mShimmerLinearGradient;
    private Matrix mShimmerGradientMatrix;
    private Paint mShimmerPaint;
    private int mShimmerColor = 0x66FFFFFF;
    private float mShimmerTranslate = 0;
    private boolean mShimmerAnimating = false;
    private boolean mIsShimmerAnim = true;
    private boolean mReAnim = false; //修复RecyclerView焦点临时标记

    private ObjectAnimator mTranslationXAnimator;
    private ObjectAnimator mTranslationYAnimator;
    private ObjectAnimator mWidthAnimator;
    private ObjectAnimator mHeightAnimator;
    private ObjectAnimator mShimmerAnimator;
    private AnimatorSet mAnimatorSet;

    private RecyclerViewScrollListener mRecyclerViewScrollListener;
    private WeakReference<RecyclerView> mWeakRecyclerView;
    private WeakReference<View> mOldFocusView;
    private OnFocusCallback mOnFocusCallback;
    private boolean mIsVisible = false;
    
    private float mScaleX;
    private float mScaleY;
    
    protected AbsFocusBorder(Context context, int shimmerColor, long shimmerDuration, boolean isShimmerAnim, long animDuration, RectF paddingOfsetRectF) {
        super(context);
        
        this.mShimmerColor = shimmerColor;
        this.mShimmerDuration = shimmerDuration;
        this.mIsShimmerAnim = isShimmerAnim;
        this.mAnimDuration = animDuration;
        if(null != paddingOfsetRectF) {
            this.mPaddingOfsetRectF.set(paddingOfsetRectF);
        }
        //关闭硬件加速
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        setVisibility(VISIBLE);
        setAlpha(0f);
        
        mShimmerPaint = new Paint();
        mShimmerGradientMatrix = new Matrix();
    }
    
    @Override
    public boolean isInEditMode() {
        return true;
    }

    /**
     * 绘制闪光
     * @param canvas
     */
    protected void onDrawShimmer(Canvas canvas) {
        if (mShimmerAnimating) {
            canvas.save();
            mTempRectF.set(mFrameRectF);
            mTempRectF.intersect(mPaddingOfsetRectF);
            float shimmerTranslateX = mTempRectF.width() * mShimmerTranslate;
            float shimmerTranslateY = mTempRectF.height() * mShimmerTranslate;
            mShimmerGradientMatrix.setTranslate(shimmerTranslateX, shimmerTranslateY);
            mShimmerLinearGradient.setLocalMatrix(mShimmerGradientMatrix);
            canvas.drawRoundRect(mTempRectF, getRoundRadius(), getRoundRadius(), mShimmerPaint);
            canvas.restore();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(w != oldw || h != oldh) {
            mFrameRectF.set(mPaddingRectF.left, mPaddingRectF.top, w - mPaddingRectF.right, h - mPaddingRectF.bottom);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        onDrawShimmer(canvas);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        unBoundGlobalFocusListener();
        super.onDetachedFromWindow();
    }

    private void setShimmerAnimating(boolean shimmerAnimating) {
        mShimmerAnimating = shimmerAnimating;
        if(mShimmerAnimating) {
            mTempRectF.set(mFrameRectF);
            mTempRectF.left += mPaddingOfsetRectF.left;
            mTempRectF.top += mPaddingOfsetRectF.top;
            mTempRectF.right -= mPaddingOfsetRectF.right;
            mTempRectF.bottom -= mPaddingOfsetRectF.bottom;
            mShimmerLinearGradient = new LinearGradient(
                    0, 0, mTempRectF.width(), mTempRectF.height(),
                    new int[]{0x00FFFFFF, 0x1AFFFFFF, mShimmerColor, 0x1AFFFFFF, 0x00FFFFFF},
                    new float[]{0f, 0.2f, 0.5f, 0.8f, 1f}, Shader.TileMode.CLAMP);
            mShimmerPaint.setShader(mShimmerLinearGradient);
        }
    }

    protected void setShimmerTranslate(float shimmerTranslate) {
        if(mIsShimmerAnim && mShimmerTranslate != shimmerTranslate) {
            mShimmerTranslate = shimmerTranslate;
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    protected float getShimmerTranslate() {
        return mShimmerTranslate;
    }

    protected void setWidth(int width) {
        if(getLayoutParams().width != width) {
            getLayoutParams().width = width;
            requestLayout();
        }
    }

    protected void setHeight(int height) {
        if(getLayoutParams().height != height) {
            getLayoutParams().height = height;
            requestLayout();
        }
    }

    @Override
    public void setVisible(boolean visible) {
        if(mIsVisible != visible) {
            mIsVisible = visible;
            
            animate().alpha(visible ? 1f : 0f).setDuration(mAnimDuration).start();
            
            if(!visible && null != mOldFocusView && null != mOldFocusView.get()) {
                runFocusScaleAnimation(mOldFocusView.get(), 1f, 1f);
                mOldFocusView.clear();
                mOldFocusView = null;
            }
        }
    }
    
    @Override
    public boolean isVisible() {
        return mIsVisible;
    }

    private void registerScrollListener(RecyclerView recyclerView) {
        if(null != mWeakRecyclerView && mWeakRecyclerView.get() == recyclerView) {
            return;
        }

        if(null == mRecyclerViewScrollListener) {
            mRecyclerViewScrollListener = new RecyclerViewScrollListener(this);
        }
        
        if(null != mWeakRecyclerView && null != mWeakRecyclerView.get()) {
            mWeakRecyclerView.get().removeOnScrollListener(mRecyclerViewScrollListener);
            mWeakRecyclerView.clear();
        }
        
        recyclerView.removeOnScrollListener(mRecyclerViewScrollListener);
        recyclerView.addOnScrollListener(mRecyclerViewScrollListener);
        mWeakRecyclerView = new WeakReference<>(recyclerView);   
    }

    protected Rect findLocationWithView(View view) {
        return findOffsetDescendantRectToMyCoords(view);
    }
    
    protected Rect findOffsetDescendantRectToMyCoords(View descendant) {
        final ViewGroup root = (ViewGroup) getParent();
        final Rect rect = new Rect();
        mReAnim = false;
        if (descendant == root) {
            return rect;
        }
        
        final View srcDescendant = descendant;

        ViewParent theParent = descendant.getParent();
        Object tag;
        Point point;

        // search and offset up to the parent
        while ((theParent != null)
                && (theParent instanceof View)
                && (theParent != root)) {

            rect.offset(descendant.getLeft() - descendant.getScrollX(),
                    descendant.getTop() - descendant.getScrollY());

            //兼容TvRecyclerView
            if (theParent instanceof RecyclerView) {
                final RecyclerView rv = (RecyclerView)theParent;
                registerScrollListener(rv);
                tag = rv.getTag();
                if (null != tag && tag instanceof Point) {
                    point = (Point) tag;
                    rect.offset(-point.x, -point.y);
                }
                if(null == tag && rv.getScrollState() != RecyclerView.SCROLL_STATE_IDLE
                        && (mRecyclerViewScrollListener.mScrolledX != 0 || mRecyclerViewScrollListener.mScrolledY != 0)) {
                    mReAnim = true;
                }
            }

            descendant = (View) theParent;
            theParent = descendant.getParent();
        }

        // now that we are up to this view, need to offset one more time
        // to get into our coordinate space
        if (theParent == root) {
            rect.offset(descendant.getLeft() - descendant.getScrollX(),
                    descendant.getTop() - descendant.getScrollY());
        } 
        
        rect.right = rect.left + srcDescendant.getMeasuredWidth();
        rect.bottom = rect.top + srcDescendant.getMeasuredHeight();
        
        return rect;
    }

    @Override
    public void onFocus(@NonNull View focusView, FocusBorder.Options options) {
        View oldFocus = null != mOldFocusView ? mOldFocusView.get() : null;
        if(null != oldFocus) {
            runFocusScaleAnimation(oldFocus, 1f, 1f);
            mOldFocusView.clear();
        }
        
        if(options instanceof Options) {
            restoreFocusBorder(oldFocus, focusView, (Options) options);
            setVisible(true);
            runFocusAnimation(focusView, (Options) options);
            mOldFocusView = new WeakReference<>(focusView);
        }
    }
    
    private void restoreFocusBorder(View oldFocus, View newFocus, Options options) {
        if(null == oldFocus) {
            final float paddingWidth = mPaddingRectF.left + mPaddingRectF.right + mPaddingOfsetRectF.left + mPaddingOfsetRectF.right;
            final float paddingHeight = mPaddingRectF.top + mPaddingRectF.bottom + mPaddingOfsetRectF.top + mPaddingOfsetRectF.bottom;
            final Rect toRect = findLocationWithView(newFocus);
            toRect.inset((int)(-paddingWidth/2), (int)(-paddingHeight/2));
            setWidth(toRect.width());
            setHeight(toRect.height());
            setTranslationX(toRect.left);
            setTranslationY(toRect.top);
        }
    }
    
    @Override
    public void boundGlobalFocusListener(@NonNull OnFocusCallback callback) {
        mOnFocusCallback = callback;
        getViewTreeObserver().addOnGlobalFocusChangeListener(this);
    }
    
    @Override
    public void unBoundGlobalFocusListener() {
        if(null != mOnFocusCallback) {
            mOnFocusCallback = null;
            getViewTreeObserver().removeOnGlobalFocusChangeListener(this);
        }
    }

    @Override
    public void onGlobalFocusChanged(View oldFocus, View newFocus) {
        final Options options = null != mOnFocusCallback ? (Options) mOnFocusCallback.onFocus(oldFocus, newFocus) : null;
        if(null != options) {
            onFocus(newFocus, options);
        }
    }

    private void runFocusAnimation(View focusView, Options options) {
        mScaleX = options.scaleX;
        mScaleY = options.scaleY;
        // 焦点缩放动画
        runFocusScaleAnimation(focusView, mScaleX, mScaleY);
        // 移动边框的动画
        runBorderAnimation(focusView, options);
    }
    
    protected void runBorderAnimation(View focusView, Options options) {
        if(null == focusView) {
            return;
        }
        if(null != mAnimatorSet) {
            mAnimatorSet.cancel();
        }
        
        createBorderAnimation(focusView, options);

        mAnimatorSet.start();
    }

    /**
     * 焦点VIEW缩放动画
     * @param oldOrNewFocusView
     * @param 
     */
    protected void runFocusScaleAnimation(@Nullable final View oldOrNewFocusView, final float scaleX, final float scaleY) {
        if(null == oldOrNewFocusView) {
            return;
        }
        oldOrNewFocusView.animate().scaleX(scaleX).scaleY(scaleY).setDuration(mAnimDuration).start();
    }

    protected void createBorderAnimation(View focusView, Options options) {

        final float paddingWidth = mPaddingRectF.left + mPaddingRectF.right + mPaddingOfsetRectF.left + mPaddingOfsetRectF.right;
        final float paddingHeight = mPaddingRectF.top + mPaddingRectF.bottom + mPaddingOfsetRectF.top + mPaddingOfsetRectF.bottom;
        final int offsetWidth = (int) (focusView.getMeasuredWidth() * (options.scaleX - 1f) + paddingWidth);
        final int offsetHeight = (int) (focusView.getMeasuredHeight() * (options.scaleY - 1f) + paddingHeight);
    
        final Rect fromRect = findLocationWithView(this);
        final Rect toRect = findLocationWithView(focusView);
        toRect.inset(-offsetWidth/2, -offsetHeight/2);
        
        final int newWidth = toRect.width();
        final int newHeight = toRect.height();
        final int newX = toRect.left - fromRect.left;
        final int newY = toRect.top - fromRect.top;
        
        final List<Animator> together = new ArrayList<>();
        final List<Animator> appendTogether = getTogetherAnimators(newX, newY, newWidth, newHeight, options);
        together.add(getTranslationXAnimator(newX));
        together.add(getTranslationYAnimator(newY));
        together.add(getWidthAnimator(newWidth));
        together.add(getHeightAnimator(newHeight));

        if(null != appendTogether && !appendTogether.isEmpty()) {
            together.addAll(appendTogether);
        }

        final List<Animator> sequentially = new ArrayList<>();
        final List<Animator> appendSequentially = getSequentiallyAnimators(newX, newY, newWidth, newHeight, options);
        if(mIsShimmerAnim) { 
            sequentially.add(getShimmerAnimator());
        } 
        if(null != appendSequentially && !appendSequentially.isEmpty()) {
            sequentially.addAll(appendSequentially);
        }

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.setInterpolator(new DecelerateInterpolator(1));
        mAnimatorSet.playTogether(together);
        mAnimatorSet.playSequentially(sequentially);
    }
    
    private ObjectAnimator getTranslationXAnimator(float x) {
        if(null == mTranslationXAnimator) {
            mTranslationXAnimator = ObjectAnimator.ofFloat(this, "translationX", x)
                    .setDuration(mAnimDuration);
        } else {
            mTranslationXAnimator.setFloatValues(x);
        }
        return mTranslationXAnimator;
    }

    private ObjectAnimator getTranslationYAnimator(float y) {
        if(null == mTranslationYAnimator) {
            mTranslationYAnimator = ObjectAnimator.ofFloat(this, "translationY", y)
                    .setDuration(mAnimDuration);
        } else {
            mTranslationYAnimator.setFloatValues(y);
        }
        return mTranslationYAnimator;
    }

    private ObjectAnimator getHeightAnimator(int height) {
        if(null == mHeightAnimator) {
            mHeightAnimator = ObjectAnimator.ofInt(this, "height", getMeasuredHeight(), height)
                    .setDuration(mAnimDuration);
        } else {
            mHeightAnimator.setIntValues(getMeasuredHeight(), height);
        }
        return mHeightAnimator;
    }
    
    private ObjectAnimator getWidthAnimator(int width) {
        if(null == mWidthAnimator) {
            mWidthAnimator = ObjectAnimator.ofInt(this, "width", getMeasuredWidth(), width)
                    .setDuration(mAnimDuration);
        } else {
            mWidthAnimator.setIntValues(getMeasuredWidth(), width);
        }
        return mWidthAnimator;
    }
    
    private ObjectAnimator getShimmerAnimator() {
        if(null == mShimmerAnimator) {
            mShimmerAnimator = ObjectAnimator.ofFloat(this, "shimmerTranslate", -1f, 1f);
            mShimmerAnimator.setInterpolator(new LinearInterpolator());
            mShimmerAnimator.setDuration(mShimmerDuration);
            mShimmerAnimator.setStartDelay(400);
            mShimmerAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    setShimmerAnimating(true);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    setShimmerAnimating(false);
                }
            });
        }
        return mShimmerAnimator;
    }

    abstract float getRoundRadius();

    abstract List<Animator> getTogetherAnimators(float newX, float newY, int newWidth, int newHeight, Options options);

    abstract List<Animator> getSequentiallyAnimators(float newX, float newY, int newWidth, int newHeight, Options options);

    private static class RecyclerViewScrollListener extends RecyclerView.OnScrollListener {
        private WeakReference<AbsFocusBorder> mFocusBorder;
        private int mScrolledX = 0, mScrolledY = 0;

        public RecyclerViewScrollListener(AbsFocusBorder border){
            mFocusBorder = new WeakReference<>(border);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            mScrolledX = Math.abs(dx) == 1 ? 0 : dx;
            mScrolledY = Math.abs(dy) == 1 ? 0 : dy;
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                final AbsFocusBorder border = mFocusBorder.get();
                final View focused = recyclerView.getFocusedChild();
                if(null != border && null != focused) {
                    if (border.mReAnim || mScrolledX != 0 || mScrolledY != 0) {
                        border.runBorderAnimation(focused, Options.get(border.mScaleX, border.mScaleY));
                    }
                }
                mScrolledX = mScrolledY = 0;
            }
        }
    }
    
    public static class Options extends FocusBorder.Options{
        protected float scaleX = 1f, scaleY = 1f;

        Options() {
        }

        private static class OptionsHolder {
            private static final Options INSTANCE = new Options();
        }
        
        public static Options get(float scaleX, float scaleY) {
            OptionsHolder.INSTANCE.scaleX = scaleX;
            OptionsHolder.INSTANCE.scaleY = scaleY;
            return OptionsHolder.INSTANCE;
        }
        
        public boolean isScale() {
            return scaleX != 1f || scaleY != 1f;
        }
    }
    
    public static abstract class Builder {
        protected int mShimmerColor = 0x66FFFFFF;
        protected boolean mIsShimmerAnim = true;
        protected long mAnimDuration = AbsFocusBorder.DEFAULT_ANIM_DURATION_TIME;
        protected long mShimmerDuration = AbsFocusBorder.DEFAULT_SHIMMER_DURATION_TIME;
        protected RectF mPaddingOffsetRectF = new RectF();

        public Builder shimmerColor(@ColorInt int color) {
            this.mShimmerColor = color;
            return this;
        }

        public Builder shimmerColorRes(@ColorRes int colorId, Context context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this.shimmerColor(context.getColor(colorId));
            } else {
                this.shimmerColor(context.getResources().getColor(colorId));
            }
            return this;
        }
        
        public Builder shimmerDuration(long duration) {
            this.mShimmerDuration = duration;
            return this;
        }

        public Builder noShimmer() {
            this.mIsShimmerAnim = false;
            return this;
        }
        
        public Builder animDuration(long duration) {
            this.mAnimDuration = duration;
            return this;
        }
        
        public Builder padding(float padding) {
            return padding(padding, padding, padding, padding);
        }
        
        public Builder padding(float left, float top, float right, float bottom) {
            this.mPaddingOffsetRectF.left = left;
            this.mPaddingOffsetRectF.top = top;
            this.mPaddingOffsetRectF.right = right;
            this.mPaddingOffsetRectF.bottom = bottom;
            return this;
        }
        
        public FocusBorder build(android.app.Fragment fragment) {
            if(null != fragment.getActivity()) {
                return build(fragment.getActivity());
            }
            return build((ViewGroup) fragment.getView());
        }
        
        public FocusBorder build(android.support.v4.app.Fragment fragment) {
            if(null != fragment.getActivity()) {
                return build(fragment.getActivity());
            }
            return build((ViewGroup) fragment.getView());
        }

        public abstract FocusBorder build(Activity activity);
        
        public abstract FocusBorder build(ViewGroup viewGroup);
    }
}
