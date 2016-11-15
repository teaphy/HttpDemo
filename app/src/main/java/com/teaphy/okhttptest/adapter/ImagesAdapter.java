package com.teaphy.okhttptest.adapter;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.teaphy.okhttptest.R;

import java.util.List;

/**
 *
 */
public class ImagesAdapter extends BaseQuickAdapter<String> {

    Context mContext;

    public ImagesAdapter(Context context,int layoutResId, List<String> data) {
        super(layoutResId, data);
        this.mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, String s) {
        Glide.with(mContext)
                .load(s)
                .into((ImageView) baseViewHolder.getView(R.id.item_img));
    }
}
