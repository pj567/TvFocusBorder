package com.owen.focus.example;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import com.owen.focus.FocusBorder;

/**
 * @author ZhouSuQiang
 */
public class MainActivity extends AppCompatActivity implements View.OnFocusChangeListener {
    /** 颜色焦点框 */
    private FocusBorder mColorFocusBorder;
    /** 图片焦点框 */
    private FocusBorder mDrawableFocusBorder;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initBorder();
    }
    
    private void initBorder() {
        /** 颜色焦点框 */
        mColorFocusBorder = new FocusBorder.Builder().asColor()
                //阴影宽度(方法shadowWidth(18f)也可以设置阴影宽度)
                .shadowWidth(TypedValue.COMPLEX_UNIT_DIP, 20f)
                //阴影颜色
                .shadowColor(Color.parseColor("#3FBB66"))
                //边框宽度(方法borderWidth(2f)也可以设置边框宽度)
                .borderWidth(TypedValue.COMPLEX_UNIT_DIP, 3.2f)
                //边框颜色
                .borderColor(Color.parseColor("#00FF00"))
                //padding值
                .padding(2f)
                //动画时长
                .animDuration(300)
                //不要闪光动画
//                .noShimmer()
                //闪光颜色
                .shimmerColor(Color.parseColor("#66FFFFFF"))
                //闪光动画时长
                .shimmerDuration(1000)
                .build(this);
        
        //焦点监听 方式一:绑定整个页面的焦点监听事件
        mColorFocusBorder.boundGlobalFocusListener(new FocusBorder.OnFocusCallback() {
            @Override
            public FocusBorder.Options onFocus(View oldFocus, View newFocus) {
                if(null != newFocus) {
                    switch (newFocus.getId()) {
                        case R.id.round_frame_layout_1:
                        case R.id.round_frame_layout_6:
                            return createColorBorderOptions(0);
                        case R.id.round_frame_layout_2:
                        case R.id.round_frame_layout_7:
                            return createColorBorderOptions(20);
                        case R.id.round_frame_layout_3:
                        case R.id.round_frame_layout_8:
                            return createColorBorderOptions(40);
                        case R.id.round_frame_layout_4:
                        case R.id.round_frame_layout_9:
                            return createColorBorderOptions(60);
                        case R.id.round_frame_layout_5:
                        case R.id.round_frame_layout_10:
                            return createColorBorderOptions(90);
        
                        default:
                            break;
                    }
                }
                mColorFocusBorder.setVisible(false);
                //返回null表示不使用焦点框框架
                return null;
            }
        });
        
    
        /** 图片焦点框 */
        mDrawableFocusBorder = new FocusBorder.Builder().asDrawable()
                .borderDrawableRes(R.mipmap.focus)
                .build(this);
    
        //焦点监听 方式一:绑定整个页面的焦点监听事件
        ViewGroup layout2 = findViewById(R.id.layout_2);
        for (int i = 0; i < layout2.getChildCount(); i++) {
            View view = layout2.getChildAt(i);
            if(null != view) {
                view.setOnFocusChangeListener(this);
            }
        }
    }
    
    private FocusBorder.Options createColorBorderOptions(int radius) {
        mDrawableFocusBorder.setVisible(false);
        float scale = 1.2f;
        return FocusBorder.OptionsFactory.get(scale, scale, dp2px(radius) * scale);
    }
    
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus) {
            mDrawableFocusBorder.onFocus(v, FocusBorder.OptionsFactory.get(1.2f, 1.2f));
        }
    }
    
    private float dp2px(int dp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
    
}
