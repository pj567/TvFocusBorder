# 欢迎使用Android TV端焦点框框架 TvFocusBorder

群   号：484790001 [群二维码](https://github.com/zhousuqiang/TvRecyclerView/blob/master/images/qq.png)

>* 支持[TvRecyclerView](https://github.com/zhousuqiang/TvRecyclerView)焦点移动;
>* 支持颜色或图片作为焦点框;
>* 支持焦点框圆角变化;

### 效果

![](https://github.com/zhousuqiang/TvFocusBorder/blob/master/images/focus3.gif)

### 使用
    ```java
    /** 颜色焦点框 */
    FocusBorder mColorFocusBorder = new FocusBorder.Builder().asColor()
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
            .noShimmer()
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
                        float scale = 1.2f;
                        return FocusBorder.OptionsFactory.get(scale, scale, dp2px(radius) * scale);

                    default:
                        break;
                }
            }
            //返回null表示不使用焦点框框架
            return null;
        }
    });


    /** 图片焦点框 */
    FocusBorder mDrawableFocusBorder = new FocusBorder.Builder().asDrawable()
            .borderDrawableRes(R.mipmap.focus)
            .build(this);

    //焦点监听 方式二:单个的焦点监听事件
    view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if(hasFocus) {
                mDrawableFocusBorder.onFocus(v, FocusBorder.OptionsFactory.get(1.2f, 1.2f));
            }
        }
    });

    ```

### 更详细的使用请见exmaple

------


作者 [owen](https://github.com/zhousuqiang)
