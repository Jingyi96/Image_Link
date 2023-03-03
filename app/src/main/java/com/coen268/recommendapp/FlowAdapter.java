package com.coen268.recommendapp;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.util.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

import static com.coen268.recommendapp.ItemData.TYPE_PIC;
import static com.coen268.recommendapp.PicActivity.Extra_PICTURE;
import static com.coen268.recommendapp.ProductWebActivity.Extra_PRODUCT_WEB_URL;


public class FlowAdapter extends RecyclerView.Adapter<FlowAdapter.ViewHolder> {
    private final boolean mIs1stFullSpan;
    private final PicActivity mPicActivity;
    private List<ItemData> mDataSet;
    public Bitmap cameraImage;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView itemIv;
        private final ImageView mainIv;
        private final TextView itemTv;
        private final View productsBtn;
        private final View linkBtn;
        private final View linkBtnBig;
        private final View shareBtn;
        private final View downloadBtn;
        private final TextView titleTv;

        public ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            itemIv = v.findViewById(R.id.iv_item);
            mainIv = v.findViewById(R.id.iv_main);
            itemTv = v.findViewById(R.id.tv_similarity);
            productsBtn = v.findViewById(R.id.products);
            linkBtn = v.findViewById(R.id.iv_link);
            linkBtnBig = v.findViewById(R.id.iv_big_link);
            shareBtn = v.findViewById(R.id.iv_share);
            downloadBtn = v.findViewById(R.id.iv_download);
            titleTv = v.findViewById(R.id.tv_title);
        }
    }


    public FlowAdapter(List<ItemData> dataSet, boolean is1stFullSpan, PicActivity picActivity) {
        mDataSet = dataSet;
        mIs1stFullSpan = is1stFullSpan;
        mPicActivity = picActivity;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(viewType == 0 && mIs1stFullSpan ? R.layout.layout_first_item : R.layout.layout_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemData itemData = mDataSet.get(position);

        StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
        if (position == 0 && mIs1stFullSpan) {
            layoutParams.setFullSpan(true);
            holder.productsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPicActivity.showProductsDialog();
                }
            });
        }

        if (holder.mainIv != null) {
//            if (cameraImage == null) {
//                holder.mainIv.setImageResource(mPicActivity.isNewImage ? R.drawable.p5dog : R.drawable.p1);
//            } else {
//                holder.mainIv.setImageBitmap(cameraImage);
//            }
            Glide.with(mPicActivity)
                    .load(itemData.getUrl())
                    .crossFade()
                    .into(holder.mainIv);
        }


        if (holder.itemIv != null) {
            ConstraintLayout.LayoutParams ivLayoutParams = (ConstraintLayout.LayoutParams) holder.itemIv.getLayoutParams();

            if (mDataSet.get(position).getSizeType() == 0) {
                ivLayoutParams.dimensionRatio = "16:31  ";
//                if (mIs1stFullSpan) {
//                    holder.itemIv.setImageResource(new Random().nextBoolean() ? R.drawable.p6 : R.drawable.dog);
//                }else {
//                    holder.itemIv.setImageResource(new Random().nextBoolean() ? R.drawable.p7 : R.drawable.glass);
//                }

                holder.itemIv.setLayoutParams(ivLayoutParams);
            } else {
//                if (mIs1stFullSpan) {
//                    holder.itemIv.setImageResource(new Random().nextBoolean() ? R.drawable.p3 : R.drawable.p4);
//                } else {
//                    holder.itemIv.setImageResource(new Random().nextBoolean() ? R.drawable.p8 : R.drawable.p9);
//                }
            }
            Glide.with(mPicActivity)
                    .load(itemData.getUrl())
                    .crossFade()
                    .into(holder.itemIv);

            holder.titleTv.setText(itemData.desc);
            if (itemData.jumpType == TYPE_PIC) {
                if (itemData.score != null) {
                    if (itemData.score > 1) {
                        itemData.score = 0.99f;
                    }
                    DecimalFormat decimalFormat = new DecimalFormat("0.0%");
                    holder.itemTv.setText(decimalFormat.format(itemData.score));
                }
            } else {
                holder.itemTv.setText("$" + itemData.price);
            }

            holder.linkBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ProductWebActivity.class);
                    intent.putExtra(Extra_PRODUCT_WEB_URL, (String) itemData.webUrl);
                    mPicActivity.startActivity(intent);
                }
            });

//            if (mIs1stFullSpan) {
//                File file = Glide.with(mPicActivity)
//                        .load(itemData.getUrl())
//                        .downloadOnly()
//            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (holder.mainIv == null) {
                Intent intent;
                if (itemData.jumpType == TYPE_PIC) {
                    intent = new Intent((v.getContext()), PicActivity.class);
                    intent.putExtra(Extra_PICTURE, itemData.getUrl());
                    intent.putExtra(Extra_PRODUCT_WEB_URL, (String) itemData.webUrl);
                } else {
                    intent = new Intent(v.getContext(), ProductWebActivity.class);
                    intent.putExtra(Extra_PRODUCT_WEB_URL, (String) itemData.webUrl);
                }
                mPicActivity.startActivity(intent);
            }
        });

        if (holder.linkBtnBig != null) {
            if (itemData.webUrl != null && itemData.webUrl != "") {
                holder.linkBtnBig.setVisibility(View.VISIBLE);
                holder.linkBtnBig.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), ProductWebActivity.class);
                        intent.putExtra(Extra_PRODUCT_WEB_URL, (String) itemData.webUrl);
                        mPicActivity.startActivity(intent);
                    }
                });
            } else {
                holder.linkBtnBig.setVisibility(View.GONE);
            }
        }

        if (holder.shareBtn != null) {
            holder.shareBtn.setOnClickListener(v -> {
                //get bitmap from imageView
                holder.mainIv.setDrawingCacheEnabled(true);
                Bitmap bitmap = holder.mainIv.getDrawingCache();

                try {
                    File file = new File(mPicActivity.getExternalCacheDir(),
                            "start_image.png");
                    FileOutputStream fOut = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                    fOut.flush();
                    fOut.close();
                    file.setReadable(true, false);

                    Uri uri = FileProvider.getUriForFile(mPicActivity,
                            BuildConfig.APPLICATION_ID + ".provider", file);

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.setType("image/png");
                    mPicActivity.startActivity(Intent.createChooser(intent, "Sharing Image Via"));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        if (holder.downloadBtn != null) {
            holder.downloadBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //get bitmap from imageView
                    holder.mainIv.setDrawingCacheEnabled(true);
                    Bitmap bitmap = holder.mainIv.getDrawingCache();
                    Toast.makeText(mPicActivity, "Downloading begins", Toast.LENGTH_SHORT).show();

                    if (android.os.Build.VERSION.SDK_INT >= 29) {
                        ContentValues values = contentValues();
                        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + mPicActivity.getString(R.string.app_name));
                        values.put(MediaStore.Images.Media.IS_PENDING, true);

                        Uri uri = mPicActivity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                        if (uri != null) {
                            try {
                                saveImageToStream(bitmap, mPicActivity.getContentResolver().openOutputStream(uri));
                                values.put(MediaStore.Images.Media.IS_PENDING, false);
                                mPicActivity.getContentResolver().update(uri, values, null, null);
                                Toast.makeText(mPicActivity, "Picture saved", Toast.LENGTH_SHORT).show();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
//                    else {
//                        File directory = new File(Environment.getExternalStorageDirectory().toString() + '/' + mPicActivity.getString(R.string.app_name));
//
//                        if (!directory.exists()) {
//                            directory.mkdirs();
//                        }
//                        String fileName = System.currentTimeMillis() + ".png";
//                        File file = new File(directory, fileName);
//                        try {
//                            saveImageToStream(bitmap, new FileOutputStream(file));
//                            ContentValues values = new ContentValues();
//                            values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
//                            mPicActivity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                        }
//
//                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }


    private ContentValues contentValues() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        }
        return values;
    }

    private void saveImageToStream(Bitmap bitmap, OutputStream outputStream) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
