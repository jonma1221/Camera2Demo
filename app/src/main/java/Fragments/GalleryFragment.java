package Fragments;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.pixuredlinux3.simplecamera2demo.GPUImageFilterTools;
import com.example.pixuredlinux3.simplecamera2demo.ImageFilterSurfaceView;
import com.example.pixuredlinux3.simplecamera2demo.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageView;
import jp.co.cyberagent.android.gpuimage.OpenGlUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class GalleryFragment extends Fragment {
    private final String TAG = "GalleryFragment";
    private ImageView savedPhoto;
    private String imageUri;
    private Uri galleryImage;
    private final int PICK_IMAGE = 1;

    private GPUImageFilter mFilter;
    private GPUImageFilterTools.FilterAdjuster mFilterAdjuster;
    private GPUImageView mGPUImageView;

    public GalleryFragment() {
    }

    public static GalleryFragment newInstance(String uri) {
        Bundle args = new Bundle();
        args.putString("uri", uri);
        GalleryFragment fragment = new GalleryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        if(bundle != null){
            imageUri = bundle.getString("uri");
            //Log.d(TAG, imageUri);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        /*savedPhoto = (ImageView) view.findViewById(R.id.galleryPhoto);
        Bitmap photo = null;
        try {
            Uri uri = Uri.parse(imageUri);
            photo = MediaStore.Images.Media.getBitmap(
                    getActivity().getContentResolver(), uri);

            Picasso.with(getActivity()).load(uri).into(savedPhoto);
            //savedPhoto.setImageBitmap(photo);
            Log.d("Photo Gallery", "fetched");
        } catch (IOException e) {
            e.printStackTrace();
        }*/


        ImageFilterSurfaceView imageFilterSurfaceView = new ImageFilterSurfaceView(getActivity(),imageUri);
        imageFilterSurfaceView = (ImageFilterSurfaceView) view.findViewById(R.id.filterSurface);

        FloatingActionButton gallery = (FloatingActionButton) view.findViewById(R.id.gallery);
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");

                startActivityForResult(intent, PICK_IMAGE);
            }
        });
        FloatingActionButton filter = (FloatingActionButton) view.findViewById(R.id.Filter);
        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GPUImageFilterTools.showDialog(getActivity(), new GPUImageFilterTools.OnGpuImageFilterChosenListener() {
                    @Override
                    public void onGpuImageFilterChosenListener(GPUImageFilter filter) {
                        /*switchFilterTo(filter);
                        mGPUImageView.requestRender();*/
                    }
                });
            }
        });
        //mGPUImageView = (GPUImageView) view.findViewById(R.id.gpuimage);
        return view;

        //return imageFilterSurfaceView;
    }

    private void switchFilterTo(final GPUImageFilter filter) {
        if (mFilter == null
                || (filter != null && !mFilter.getClass().equals(filter.getClass()))) {
            mFilter = filter;
            mGPUImageView.setFilter(mFilter);
            //mFilterAdjuster = new GPUImageFilterTools.FilterAdjuster(mFilter);
        }
    }

    private void handleImage(final Uri selectedImage) {
        mGPUImageView.setImage(selectedImage);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case PICK_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    handleImage(data.getData());
                    imageUri = data.getData().toString();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }
}