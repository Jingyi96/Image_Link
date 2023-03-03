package com.coen268.recommendapp;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class FlowSpaceItemDecoration extends RecyclerView.ItemDecoration {
    private final boolean mIs1stFullSpan;

    public FlowSpaceItemDecoration(boolean is1stFullSpan) {
        mIs1stFullSpan = is1stFullSpan;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {

        float density = view.getContext().getResources().getDisplayMetrics().density;
        int position = parent.getChildAdapterPosition(view); // item position
        if (position != 0 || !mIs1stFullSpan) {
            StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
            boolean isLeft = params.getSpanIndex() % 2 == 0;
            if (isLeft) {
                outRect.left = (int) (18 * density);
                outRect.right = (int) (6 * density);
            } else {
                outRect.left = (int) (6 * density);
                outRect.right = (int) (18 * density);
            }
        }

        if (mIs1stFullSpan) {
            if (position > 2) {
                outRect.top = (int) (8 * density);
            }
        } else {
            if (position > 1) {
                outRect.top = (int) (8 * density);
            }
        }
    }
}
