package com.aitsuki.aipermission.sample;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.aitsuki.aipermission.annotation.RequirePermissions;
import com.aitsuki.aipermission.annotation.ScanFragment;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

/**
 * Create by AItsuki on 2019/3/27.
 */
@ScanFragment
public class CameraFragment extends Fragment {

    private ImageView iv_preview;
    private File tempFile;
    private static final int REQUEST_CODE_TAKE_PICTURE = 101;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        iv_preview = view.findViewById(R.id.iv_preview);
        openCamera();
    }

    @SuppressWarnings("WeakerAccess")
    @RequirePermissions(
            permissions = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
            rationaleId = R.string.app_name,
            strategy = TestStrategy.class)
    void openCamera() {
        tempFile = createTempFile();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri uri = getFileUri(tempFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri); //指定图片存放位置，指定后，在onActivityResult里得到的Data将为null
        startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_TAKE_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(tempFile.getAbsolutePath(), options);
                int srcH = options.outHeight;
                options.inJustDecodeBounds = false;
                options.inScaled = true;
                options.inDensity = srcH;
                options.inTargetDensity = iv_preview.getHeight();
                Bitmap bitmap = BitmapFactory.decodeFile(tempFile.getAbsolutePath(), options);
                iv_preview.setImageBitmap(bitmap);
            } else {
                requireActivity().onBackPressed();
            }
        }
    }


    private File createTempFile() {
        File cacheDir = requireActivity().getExternalCacheDir();
        if (cacheDir == null) {
            cacheDir = requireActivity().getCacheDir();
        }
        return new File(cacheDir, "temp.jpg");
    }

    private Uri getFileUri(File file) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return Uri.fromFile(file);
        } else {
            return FileProvider.getUriForFile(requireActivity(),
                    requireActivity().getApplicationContext().getPackageName() + ".fileprovide",
                    file);
        }
    }
}
