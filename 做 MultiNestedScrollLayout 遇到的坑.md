# 做 MultiNestedScrollLayout 遇到的难点

### 一、嵌套滑动机制

嵌套滑动，一般实现有一下几种方法：

* 重写 onIntercepterTouchEvent & requestDisallowIntercepterTouchEvent 方法

  这种方法属于粗糙的手动处理，通用性不强

* 使用 NestedScrollParent & NestedScrollChild 接口组合，搭配使用

  本项目采用这个方法。打造通用易用的带优先级的多级嵌套滑动。

### 二、应该继承自 ScrollView 还普通的 Layout？

* #### 继承自普通 Layout

  尝试过 FrameLayout、LinearLayout、ConstraintLayout，有如下问题：

  * 外层 layout 的高度如果设置为 `match_parent`，那么绘制时高度定死了，对于超过改高度部分的 view 不绘制。也就是说，往上滑动 RV 时，虽然外层 layout 整体上滑了，但是下方会出现一大片空白。

    设置成 `wrap_content` 也不行，查看 ViewGroup 的`onMeasure`方法，如下：

    ```java
    public static int getChildMeasureSpec(int spec, int padding, int childDimension) {
            int specMode = MeasureSpec.getMode(spec);
            int specSize = MeasureSpec.getSize(spec);
    
            int size = Math.max(0, specSize - padding);
    
            int resultSize = 0;
            int resultMode = 0;
    
            switch (specMode) {
                    case MeasureSpec.EXACTLY:
                if (childDimension >= 0) {
                    resultSize = childDimension;
                    resultMode = MeasureSpec.EXACTLY;
                } else if (childDimension == LayoutParams.MATCH_PARENT) {
                    // Child wants to be our size. So be it.
                    resultSize = size;
                    resultMode = MeasureSpec.EXACTLY;
                } else if (childDimension == LayoutParams.WRAP_CONTENT) {
                    // Child wants to determine its own size. It can't be
                    // bigger than us.
                    resultSize = size;
                    resultMode = MeasureSpec.AT_MOST;
                }
                break;
           ...
            //noinspection ResourceType
            return MeasureSpec.makeMeasureSpec(resultSize, resultMode);
        }
    ```

    注意到当 childDimension 为 `LayoutParams.WRAP_CONTENT`时，它最大的尺寸还是不能超过父 View。如果想要外层 layout 的高度超过屏幕，只有在 xml 中写死一个固定的超过屏幕的数值，比如 3000dp，这样才可以。但是，将 layout 的高度设定为 `屏幕高度 + 超出屏幕部分的高度(一般来说是 RV 超出屏幕的部分)`，因为各种机型尺寸不一，所以没办法在 xml 里面直接设置，只能在代码里动态设置高度。~~通过重写外层 Layout 的 `onMeasure` 方法来写死高度也达不到想要的效果。（其实是可以的，和 ScrollView 类似，在 onMeasure 中写死高度为屏幕高度）~~

  * 直接滑动外层 layout 时，不能滑动。需要自己实现 scroll 与 fling，如果继承自 `ScrollView`，就不需要自己实现。

* #### 继承自 ScrollView

  * ScrollView 最大的坑点在于，它虽然不会像普通 View 一样对于子 View 超出父 ScrollView 的部分不绘制，但是在其内部的 `scrollTo` 方法，有一个 `clamp` 的行为来修改滑动的入参：

    ```java
    public void scrollTo(int x, int y) {
            // we rely on the fact the View.scrollBy calls scrollTo.
            if (getChildCount() > 0) {
                View child = getChildAt(0);
                x = clamp(x, getWidth() - mPaddingRight - mPaddingLeft, child.getWidth());
                y = clamp(y, getHeight() - mPaddingBottom - mPaddingTop, child.getHeight());
                if (x != mScrollX || y != mScrollY) {
                    super.scrollTo(x, y);
                }
            }
        }
    
    // 坑点就是这个方法
    private static int clamp(int n, int my, int child) {
            if (my >= child || n < 0) {
                /* my >= child is this case:
                 *                    |--------------- me ---------------|
                 *     |------ child ------|
                 * or
                 *     |--------------- me ---------------|
                 *            |------ child ------|
                 * or
                 *     |--------------- me ---------------|
                 *                                  |------ child ------|
                 *
                 * n < 0 is this case:
                 *     |------ me ------|
                 *                    |-------- child --------|
                 *     |-- mScrollX --|
                 */
                return 0;
            }
            if ((my+n) > child) {
                /* this case:
                 *                    |------ me ------|
                 *     |------ child ------|
                 *     |-- mScrollX --|
                 */
                return child-my;
            }
            return n;
        }
    ```

    可以看到，在 `clmap` 方法中，如果你的 ScrollView 本身的尺寸大于内部的 content 的尺寸，这个时候调用 `scrollTo` 是没效果的，因为传入的滑动距离被修正为 0 了。

    所以，继承自 ScrollView 时，需要特别注意高度的设置，如果设置成 `wrap_content`，那么它就滑不动了。

    一般来说，所有的 layout 高度都设置成屏幕高度就好了。在 xml 中，最外层的 Layout 高度可以设置成 `match_parent`，这个没问题，但是里面的 Layout 在 xml 中就不好设置了。比如：

    ```xml
    <MultiNestedScrollView
        ...
    	// 最外层可以设置成 match_parent 来填满整个屏幕                       
    	android:layout_height="match_parent"
    	...>
        
        <LinearLayout>
            ...
            <MultiNestedScrollView
    		...
            // 这里要设置成多少去让 MultiNestedScrollView 填满屏幕呢？
            android:layout_height="?????"
            ...>
            </MultiNestedScrollView>
            ...
        </LinearLayout>
        
    </MultiNestedScrollView>
    ```

    这个问题在 xml 里不是很好解决。

    最后决定重写 ScrollView 的 `onMeasure` 方法，直接设置为屏幕高度(当然，这里是指减去 statusBar 和 navBar 之后的高度，毕竟大部分场景下 RV 不会直接怼到这两个地方去)。

  * 继承自 ScrollView 的另外一个好处是，直接滑动 ScrollView 的话，不需要去自己重写滑动事件来处理外层的滑动。只需要关注滑动事件的分发。

~~出于上述考虑，所以还是决定继承自 ScrollView~~

* #### 参考 CoordinatorLayout 的做法

  以上两种继承，最大的问题，在于没有考虑到，内部的 RecyclerView 的高度应该怎么设置。
  
  不管继承自哪个 View，如果重写 `onMeasure`使 RecycelerView 的直接父 layout 高度为屏幕高度，那么 xml 中 RecyclerView 的高度应该设置成什么?
  
  正常来说，一般也要设置 RecyclerView 的高度为屏幕高度，因为大多数场景下 RecyclerView 会全部显示在整个屏幕上。然而在 xml 中没办法设置这个高度。如果重写 RecyclerView 的 `onMeasure`方法，这样侵入性太高了。
  
  研究了一下 CoordinatorLayout ，发现 CoordinatorLayout  和其中的 RecyclerView 不管在 xml 中怎样设置高度，最终高度两者都一样。研究一下怎么做到的。。。。
