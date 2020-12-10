# 做 MultiNestedScrollLayout 遇到的坑

### 一、应该继承自 ScrollView 还普通的 Layout？

* #### 继承自普通 Layout

  尝试过 FrameLayout、LinearLayout、ConstraintLayout，有如下问题：

  * 外层 layout 的高度如果设置为 `match_parent`，那么绘制时高度定死了，对于超过改高度部分的 view 不绘制。也就是说，往上滑动 RV 时，虽然外层 layout 整体上滑了，但是下方会出现一大片空白。

    要解决这个问题，只能将 layout 的高度设定为 `屏幕高度 + 超出屏幕部分的高度(一般来说是 RV 超出屏幕的部分)`。因为各种机型尺寸不一，所以没办法在 xml 里面直接设置，只能在代码里动态设置高度。通过重写外层 Layout 的 `onMeasure` 方法来写死高度也达不到想要的效果。

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

出于上述考虑，所以还是决定继承自 ScrollView