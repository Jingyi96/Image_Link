package com.coen268.recommendapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coen268.recommendapp.ebay.EbayResponse;
import com.coen268.recommendapp.ebay.NetworkHandler;
import com.coen268.recommendapp.ebay.ProductsRequest;
import com.coen268.recommendapp.ebay.ProductsService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.WebDetection;
import com.google.api.services.vision.v1.model.WebImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.coen268.recommendapp.ProductWebActivity.Extra_PRODUCT_WEB_URL;

public class PicActivity extends AppCompatActivity {

    public static final String Extra_PICTURE = "imageUrl1";


    private static final int REQ_CODE = 1001;
    protected RecyclerView mPicturesRv;
    protected RecyclerView.LayoutManager mPicRvLayoutManager;
    protected List<ItemData> mPicList = new ArrayList<>();
    protected FlowAdapter mPicRvAdapter;
    protected ProductsDialog mProductsDialog;
    protected RecyclerView mProductsRv;
    protected RecyclerView.LayoutManager mProductRvLayoutManager;
    protected FlowAdapter mProductRvAdapter;
    protected List<ItemData> mProductList = new ArrayList<>();
    protected FloatingActionButton mFloatingBtn;
    public boolean isNewImage = false;


    //CloudVision Activity
    private static final String CLOUD_VISION_API_KEY = "AIzaSyB2wz1_2FCq3EhBcuwbf6iClmRpv8Ja0kQ";
    public static final String FILE_NAME = "temp.jpg";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final int MAX_LABEL_RESULTS = 10;
    private static final int MAX_DIMENSION = 1200;

    private static final String TAG = PicActivity.class.getSimpleName();
    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;

    String currentPhotoPath;

    private TextView mImageDetails;
    private ImageView mMainImage;

    public WebDetection mWebDetection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        setContentView(R.layout.layout_main);
        mPicturesRv = findViewById(R.id.rv_main);
        mPicRvLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mPicturesRv.setLayoutManager(mPicRvLayoutManager);
        mPicRvAdapter = new FlowAdapter(mPicList, true, this);
        mPicturesRv.setAdapter(mPicRvAdapter);
        mPicturesRv.addItemDecoration(new FlowSpaceItemDecoration(true));

        mProductsDialog = new ProductsDialog(this);
        mProductsDialog.setContentView(R.layout.layout_products_dialog);
        mProductsRv = mProductsDialog.findViewById(R.id.rv_flow);
        mProductsDialog.findViewById(R.id.iv_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProductsDialog.dismiss();
            }
        });
        mProductRvLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mProductsRv.setLayoutManager(mProductRvLayoutManager);
        mProductRvAdapter = new FlowAdapter(mProductList, false, this);
        mProductsRv.setAdapter(mProductRvAdapter);
        mProductsRv.addItemDecoration(new FlowSpaceItemDecoration(false));

        mFloatingBtn = findViewById(R.id.floating_btn);
        mFloatingBtn.setOnClickListener(v -> {
//                dispatchTakePictureIntent();
            AlertDialog.Builder builder = new AlertDialog.Builder(PicActivity.this);
            builder
                    .setMessage(R.string.dialog_select_prompt)
                    .setPositiveButton(R.string.dialog_select_gallery, (dialog, which) -> startGalleryChooser())
                    .setNegativeButton(R.string.dialog_select_camera, (dialog, which) -> startCamera());
            builder.create().show();
        });

        String stringExtra = getIntent().getStringExtra(Extra_PICTURE);
        findViewById(R.id.iv_back).setVisibility(stringExtra == null ? View.GONE : View.VISIBLE);
        setEmptyViewVisibility(stringExtra == null);
        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if (stringExtra != null) {
            isNewImage = true;
            mPicList.add(new ItemData(new Random().nextInt(2), ItemData.TYPE_PIC, stringExtra,getIntent().getStringExtra(Extra_PRODUCT_WEB_URL)));
            mPicRvAdapter.notifyDataSetChanged();

            new Thread(new Runnable() {
                public void run() {
                    try {
                        final Bitmap bitmap;

                        bitmap = Glide.with(getApplicationContext())
                                .load(stringExtra)
                                .asBitmap()
                                .into(500, 500)
                                .get();
                        Bitmap bitmap1 = scaleBitmapDown(bitmap, MAX_DIMENSION);
                        callCloudVision(bitmap1);
                        requestProducts(bitmap);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();


        }

    }

    private void setEmptyViewVisibility(boolean flag) {
        findViewById(R.id.iv_empty_hint).setVisibility(flag ? View.VISIBLE : View.GONE);
    }

//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        StringBuilder builder = new StringBuilder();
//        if (intent.getExtras() != null) {
//            for (String key : intent.getExtras().keySet()) {
//                Object value = intent.getExtras().get(key);
//                builder.append("键：" + key + "-值：" + value + "\n");
//            }
//            Toast.makeText(getApplicationContext(),builder.toString(),Toast.LENGTH_LONG).show();
//        }
//    }

    private void getProducts(Uri uri) throws IOException {
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        InputStream is = null;
//        byte[] data = null;
//        String result = null;
//        try {
//            is = new FileInputStream(uri.toString());
//            data = new byte[is.available()];
//            //写入数组
//            is.read(data);
//            //用默认的编码格式进行编码
//            result = Base64.encodeToString(data, Base64.NO_CLOSE);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            is = this.getContentResolver().openInputStream(uri);
//            data = new byte[is.available()];
//            result = Base64.encodeToString(data, Base64.NO_CLOSE);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        mProductList.clear();
        mProductRvAdapter.notifyDataSetChanged();
        Bitmap bitmap =
                scaleBitmapDown(
                        MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                        MAX_DIMENSION);


        requestProducts(bitmap);

    }

    private void requestProducts(Bitmap bitmap) {
        ProductsService service = NetworkHandler.getRetrofit().create(ProductsService.class);
        Call<EbayResponse> call1 = service.productList(new ProductsRequest(encodeImage(bitmap)));
        new Thread(new Runnable() {
            @Override
            public void run() {
                //                    EbayResponse ebayResponse = call1.execute().body();
                call1.enqueue(new Callback<EbayResponse>() {
                    @Override
                    public void onResponse(Call<EbayResponse> call, Response<EbayResponse> response) {
                        if (response != null && response.body() != null && response.body().getItemSummaries() != null) {
                            for (EbayResponse.ItemSummariesBean itemSummary : response.body().getItemSummaries()) {
                                mProductList.add(new ItemData(new Random().nextInt(2), ItemData.TYPE_PRODUCT, itemSummary.getImage().getImageUrl(), itemSummary.getItemWebUrl(),itemSummary.getPrice().getValue(),itemSummary.getTitle()));
                            }
                        } else {
                            mProductList.clear();
                            Toast.makeText(getApplicationContext(), "No recommended products for now, maybe try later", Toast.LENGTH_LONG).show();
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProductRvAdapter.notifyDataSetChanged();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<EbayResponse> call, Throwable t) {

                    }
                });
                int a = 1;
            }
        }).start();
    }


    private String encodeImage(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.DEFAULT);

        return encImage;
    }

    private void initDataSet(String mainUriStr) {
//        for (int i = 0; i < 30; i++) {
//            mPicList.add(new ItemData(new Random().nextInt(2), ItemData.TYPE_PIC));
//        }
//
//        mProductList = new ArrayList<>();
//        for (int i = 0; i < 30; i++) {
//            mProductList.add(new ItemData(new Random().nextInt(2), ItemData.TYPE_PRODUCT));
//        }
        mPicList.clear();
        mPicList.add(new ItemData(new Random().nextInt(2), ItemData.TYPE_PIC, mainUriStr));
        mPicRvAdapter.notifyDataSetChanged();
    }

    public void showProductsDialog() {
        mProductsDialog.show();
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            mPicRvAdapter.cameraImage = (Bitmap) extras.get("data");
//            mPicRvAdapter.notifyDataSetChanged();
////            imageView.setImageBitmap(imageBitmap);
//        }
//    }
//
//    public static final int REQUEST_IMAGE_CAPTURE = 1;
//
//    private void dispatchTakePictureIntent() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//        }
//    }


    //CloudVision Activity
    public void startGalleryChooser() {
        if (PermissionUtils.requestPermission(this, GALLERY_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                    GALLERY_IMAGE_REQUEST);
        }
    }

    //这部分修改了！
    public void startCamera() {
        if (PermissionUtils.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
            File photoFile = null;

            try {
                photoFile = getCameraFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
            }

        }
    }

    public File getCameraFile() throws IOException {
//        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        return new File(dir, FILE_NAME);
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile("FILE_NAME",".jpg", dir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setEmptyViewVisibility(false);

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            initDataSet(data.getData().toString());
            uploadImage(data.getData());
            try {
                getProducts(data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
//            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            File f = new File(currentPhotoPath);
            Uri photoUri  = Uri.fromFile(f);
            initDataSet(photoUri.toString());
            uploadImage(photoUri);
            try {
                getProducts(photoUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
                    startCamera();
                }
                break;
            case GALLERY_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
                    startGalleryChooser();
                }
                break;
        }
    }

    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                                MAX_DIMENSION);

                callCloudVision(bitmap);
//                mMainImage.setImageBitmap(bitmap);

            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }

    private Vision.Images.Annotate prepareAnnotationRequest(Bitmap bitmap) throws IOException {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        VisionRequestInitializer requestInitializer =
                new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                    /**
                     * We override this so we can inject important identifying fields into the HTTP
                     * headers. This enables use of a restricted cloud platform API key.
                     */
                    @Override
                    protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                            throws IOException {
                        super.initializeVisionRequest(visionRequest);

                        String packageName = getPackageName();
                        visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                        String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                        visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                    }
                };

        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(requestInitializer);

        Vision vision = builder.build();

        BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                new BatchAnnotateImagesRequest();
        batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

            // Add the image
            Image base64EncodedImage = new Image();
            // Convert the bitmap to a JPEG
            // Just in case it's a format that Android understands but Cloud Vision
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            // Base64 encode the JPEG
            base64EncodedImage.encodeContent(imageBytes);
            annotateImageRequest.setImage(base64EncodedImage);

            // add the features we want
            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                Feature labelDetection = new Feature();
                labelDetection.setType("WEB_DETECTION");
                labelDetection.setMaxResults(MAX_LABEL_RESULTS);
                add(labelDetection);
            }});

            // Add the list of one thing to the request
            add(annotateImageRequest);
        }});

        Vision.Images.Annotate annotateRequest =
                vision.images().annotate(batchAnnotateImagesRequest);
        // Due to a bug: requests to Vision API containing large images fail when GZipped.
        annotateRequest.setDisableGZipContent(true);
        Log.d(TAG, "created Cloud Vision request object, sending request");

        return annotateRequest;
    }

    private static class LableDetectionTask extends AsyncTask<Object, Void, String> {
        private final WeakReference<PicActivity> mActivityWeakReference;
        private Vision.Images.Annotate mRequest;

        LableDetectionTask(PicActivity activity, Vision.Images.Annotate annotate) {
            mActivityWeakReference = new WeakReference<>(activity);
            mRequest = annotate;
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                Log.d(TAG, "created Cloud Vision request object, sending request");
                BatchAnnotateImagesResponse response = mRequest.execute();

//                return convertResponseToString(response);
                if (response != null && response.getResponses() != null && response.getResponses().get(0) != null && response.getResponses().get(0).getWebDetection() != null) {
                    WebDetection webDetection = response.getResponses().get(0).getWebDetection();
                    PicActivity activity = mActivityWeakReference.get();
                    if (activity != null && !activity.isFinishing()) {
                        activity.onWebDetectionResult(webDetection);
                    }
                } else {
                    PicActivity activity = mActivityWeakReference.get();
                    if (activity != null && !activity.isFinishing()) {
                        activity.showNoPicResultToast();
                    }
                }


            } catch (GoogleJsonResponseException e) {
                Log.d(TAG, "failed to make API request because " + e.getContent());
            } catch (IOException e) {
                Log.d(TAG, "failed to make API request because of other IOException " +
                        e.getMessage());
            }
            return "Cloud Vision API request failed. Check logs for details.";
        }

        protected void onPostExecute(String result) {
//            PicActivity activity = mActivityWeakReference.get();
//            if (activity != null && !activity.isFinishing()) {
////                TextView imageDetail = activity.findViewById(R.id.image_details);
////                imageDetail.setText(result);
//                Gson gson = new Gson();
//                WebDetection webDetection = gson.fromJson(result, WebDetection.class);
//                activity.onWebDetectionResult(webDetection);
//            }
        }
    }

    private void showNoPicResultToast() {
        mPicList.clear();
        Toast.makeText(getApplicationContext(), "No recommended pictures for now, maybe try later", Toast.LENGTH_LONG).show();

    }

    private void callCloudVision(final Bitmap bitmap) {
        // Switch text to loading
//        mImageDetails.setText(R.string.loading_message);

        // Do the real work in an async task, because we need to use the network anyway
        try {
            AsyncTask<Object, Void, String> labelDetectionTask = new PicActivity.LableDetectionTask(this, prepareAnnotationRequest(bitmap));
            labelDetectionTask.execute();
        } catch (IOException e) {
            Log.d(TAG, "failed to make API request because of other IOException " +
                    e.getMessage());
        }
    }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private static String convertResponseToString(BatchAnnotateImagesResponse response) {
        StringBuilder message = new StringBuilder("I found these things:\n\n");

        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {
                message.append(String.format(Locale.US, "%.3f: %s", label.getScore(), label.getDescription()));
                message.append("\n");
            }
        } else {
            message.append("nothing");
        }

        return message.toString();
    }

    public void onWebDetectionResult(WebDetection webDetection) {
        mWebDetection = webDetection;
        for (WebImage similarImage : webDetection.getVisuallySimilarImages()) {
            mPicList.add(new ItemData(new Random().nextInt(2), ItemData.TYPE_PIC, similarImage.getUrl(), similarImage.getScore()));
        }
        for (int i = 1; i < mPicList.size(); i++) {
            if (i-1 < mWebDetection.getWebEntities().size()) {
                mPicList.get(i).setScore(mWebDetection.getWebEntities().get(i-1).getScore());
                    mPicList.get(i).setDesc(mWebDetection.getWebEntities().get(i-1).getDescription());
                if (mWebDetection.getPagesWithMatchingImages()!=null&&mWebDetection.getPagesWithMatchingImages().size()>0) {
                    mPicList.get(i).setWebUrl(mWebDetection.getPagesWithMatchingImages().get(0).getUrl());
                }
//                Log.d(TAG, mWebDetection.getWebEntities().get(i).getDescription());
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPicRvAdapter.notifyDataSetChanged();
            }
        });
    }

}