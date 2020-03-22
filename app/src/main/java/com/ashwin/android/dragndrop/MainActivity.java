package com.ashwin.android.dragndrop;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "drag-drop-demo";

    private ViewGroup rootLayout;
    private ImageView dragImageView;
    private ImageView dropImageView;

    private int rootWidth;
    private int rootHeight;

    private int xDiff;
    private int yDiff;

    private int xDelta;
    private int yDelta;

    private int dragWidth = 0;
    private int dragHeight = 0;

    private int xDropStart;
    private int yDropStart;

    private int dropWidth;
    private int dropHeight;
    private int dropWidthPadding;  // 20% of dropWidth
    private int dropHeightPadding;  // 20% of dropHeight

    private final class DragTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            final int x = (int) event.getRawX();
            final int y = (int) event.getRawY();

            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    Log.w(TAG, "action_down");

                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
                    xDelta = x - layoutParams.leftMargin;
                    yDelta = y - layoutParams.topMargin;
                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                    Log.w(TAG, "action_pointer_down");
                    break;

                case MotionEvent.ACTION_POINTER_UP:
                    Log.w(TAG, "action_pointer_up");
                    break;

                case MotionEvent.ACTION_MOVE:
                    Log.w(TAG, "action_move");
                    RelativeLayout.LayoutParams newLayoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
                    newLayoutParams.leftMargin = Math.max(0, Math.min(rootWidth, x - xDelta));
                    newLayoutParams.topMargin = Math.max(0, Math.min(rootHeight, y - yDelta));
                    v.setLayoutParams(newLayoutParams);
                    break;

                case MotionEvent.ACTION_UP:
                    Log.w(TAG, "action_up");
                    if (x > xDropStart + dropWidthPadding
                            && x < (xDropStart + dropWidth) - dropWidthPadding
                            && y > yDropStart + dropHeightPadding
                            && y < (yDropStart + dropHeight) - dropHeightPadding
                    ) {
                        Log.w(TAG, "drop success");

                        int dragLeftPadding = (dropWidth - dragWidth) / 2;
                        int dragTopPadding = (dropHeight - dragHeight) / 2;

                        int dragLeftMargin = (xDropStart - xDiff) + dragLeftPadding;
                        int dragTopMargin = (yDropStart - yDiff) + dragTopPadding;

                        // Set left and top margins
                        RelativeLayout.LayoutParams dragLayoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
                        dragLayoutParams.leftMargin = Math.max(0, dragLeftMargin);
                        dragLayoutParams.topMargin = Math.max(0, dragTopMargin);
                        v.setLayoutParams(dragLayoutParams);
                    } else {
                        Log.w(TAG, "drop fail");
                    }
                    v.performClick();
                    break;
            }
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootLayout = (ViewGroup) findViewById(R.id.root_layout);
        dragImageView = (ImageView) findViewById(R.id.drag_imageview);
        dropImageView = (ImageView) findViewById(R.id.drop_imageview);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(150, 150);
        layoutParams.leftMargin = 200;
        layoutParams.topMargin = 200;
        dragImageView.setLayoutParams(layoutParams);
        dragImageView.setOnTouchListener(new DragTouchListener());

        dragImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w(TAG, "on-click");
            }
        });

        // Get root height
        final ViewGroup relativeLayout = rootLayout;
        ViewTreeObserver rootViewTreeObserver = relativeLayout.getViewTreeObserver();
        rootViewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.w(TAG, "root-layout: on-global-layout-listener");
                rootHeight = relativeLayout.getMeasuredHeight();
                rootWidth = relativeLayout.getMeasuredWidth();
            }
        });

        // Get drop-imageview width and height
        final ImageView dropView = dropImageView;
        ViewTreeObserver dropViewTreeObserver = dropView.getViewTreeObserver();
        dropViewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.w(TAG, "drop-view: on-global-layout-listener");
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    dropView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    dropView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                dropWidth  = dropView.getMeasuredWidth();
                dropHeight = dropView.getMeasuredHeight();
                dropWidthPadding = (int) (0.20 * dropWidth);
                dropHeightPadding = (int) (0.20 * dropHeight);
                Log.w(TAG, "drop-width: " + dropWidth + ", drop-height: " + dropHeight);

                int[] location = new int[2];
                dropView.getLocationInWindow(location);
                xDropStart = location[0];
                yDropStart = location[1];

                DisplayMetrics dm = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(dm);
                xDiff = dm.widthPixels - rootWidth;
                yDiff = dm.heightPixels - rootHeight;
            }
        });

        // Get drag-imageview width and height
        final ImageView dragView = dragImageView;
        ViewTreeObserver dragViewTreeObserver = dragView.getViewTreeObserver();
        dragViewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.w(TAG, "drag-view: on-global-layout-listener");
                if (dragWidth == 0 && dragHeight == 0) {
                    dragWidth = dragView.getMeasuredWidth();
                    dragHeight = dragView.getMeasuredHeight();
                }
            }
        });
    }
}
