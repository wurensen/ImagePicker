package com.lancewu.imagepickerdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lancewu.imagepicker.ImagePicker;
import com.lancewu.imagepicker.ImagePickerResult;
import com.lancewu.imagepicker.OnImagePickerCallback;
import com.lancewu.imagepicker.util.StreamUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private ImageView mImageView;
    private TextView mInfoTv;
    private ImagePicker mPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = findViewById(R.id.iv);
        mInfoTv = findViewById(R.id.info_tv);
    }

    public void clickCameraCrop(View view) {
        // 从相机拍照并裁剪
        File file = new File(getExternalCacheDir(), "camera_crop.jpg");
        // 动态申请存储权限
//        File file = new File(Environment.getExternalStorageDirectory(), "camera_crop.jpg");
        ImagePicker.CropConfigBuilder cropConfigBuilder = new ImagePicker.CropConfigBuilder()
                .aspect(1, 2)
                .outputSize(200, 400)
                .outputFile(file);
        mPicker = new ImagePicker.Builder(this)
                .fromCamera(file)
                .withCrop(cropConfigBuilder)
                .build();
        mPicker.pick(mCallback);
    }

    public void clickGalleryCrop(View view) {
        File file = new File(getExternalCacheDir(), "gallery_crop.jpg");
        ImagePicker.CropConfigBuilder cropConfigBuilder = new ImagePicker.CropConfigBuilder()
                .aspect(1, 1)
//                .outputSize(100, 100) // 采用默认大小200
                .outputFile(file);
        mPicker = new ImagePicker.Builder(this)
                .fromGallery()
                .withCrop(cropConfigBuilder)
                .build();
        mPicker.pick(mCallback);
    }

    public void clickDocumentCrop(View view) {
        File file = new File(getExternalCacheDir(), "document_crop.jpg");
        ImagePicker.CropConfigBuilder cropConfigBuilder = new ImagePicker.CropConfigBuilder()
                .aspect(1, 1)
                .outputSize(300, 300)
                .outputFile(file);
        mPicker = new ImagePicker.Builder(this)
                .fromDocument()
                .withCrop(cropConfigBuilder)
                .build();
        mPicker.pick(mCallback);
    }

    public void clickCamera(View view) {
//        File file = new File(getExternalCacheDir(), "camera.jpg");
        // SD卡需要动态申请权限
        File file = new File(Environment.getExternalStorageDirectory(), "camera.jpg");
        mPicker = new ImagePicker.Builder(this)
                .fromCamera(file)
                .build();
        mPicker.pick(mCallback);
    }

    public void clickGallery(View view) {
        mPicker = new ImagePicker.Builder(this)
                .fromGallery()
                .build();
        mPicker.pick(mCallback);
    }

    public void clickDocument(View view) {
        mPicker = new ImagePicker.Builder(this)
                .fromDocument()
                .build();
        mPicker.pick(mCallback);
    }

    public void clickCrop(View view) {
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        File file = null;
        try {
            inputStream = getAssets().open("justCrop.jpg");
            file = new File(getExternalCacheDir(), "crop_input.jpg");
            fileOutputStream = new FileOutputStream(file);
            final byte[] buf = new byte[8 * 1024];
            int read;
            while ((read = inputStream.read(buf)) != -1) {
                fileOutputStream.write(buf, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (file == null) {
            return;
        }
        File cropFile = new File(getExternalCacheDir(), "only_crop_output.jpg");
        ImagePicker.CropConfigBuilder cropConfigBuilder = new ImagePicker.CropConfigBuilder()
                .aspect(1, 1)
                .outputSize(700, 700)
                .inputFile(file)
                .outputFile(cropFile);
        mPicker = new ImagePicker.Builder(this)
                .withCrop(cropConfigBuilder)
                .build();
        mPicker.pick(mCallback);
    }

    private OnImagePickerCallback mCallback = new OnImagePickerCallback() {
        @Override
        public void onPickError(@ErrorCode int errorCode) {
            showToast("ImagePicker-发生错误：" + errorCode);
        }

        @Override
        public void onPickSuccess(@NonNull ImagePickerResult result) {
            InputStream inputStream = null;
            try {
                inputStream = getContentResolver().openInputStream(result.getImageUri());
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                mImageView.setImageBitmap(bitmap);
                String text = "图片信息：宽*高=" + bitmap.getWidth() + "*" + bitmap.getHeight();
                mInfoTv.setText(text);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                StreamUtils.close(inputStream);
            }
        }

        @Override
        public void onPickCancel() {
            showToast("ImagePicker-取消选择");
        }

        void showToast(String msg) {
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
        }
    };
}
