package Fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.pixuredlinux3.simplecamera2demo.GPUImageFilterTools;
import com.example.pixuredlinux3.simplecamera2demo.ImageFilterSurfaceView;
import com.example.pixuredlinux3.simplecamera2demo.R;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class GalleryFragment extends Fragment {
    private final String TAG = "GalleryFragment";
    private final int PICK_IMAGE = 1;
    private String imageUri;
    private String filterName;
    private static Bitmap currentBitmap;
    private LruCache<String, Bitmap> mMemoryCache;

    private ImageFilterSurfaceView imageFilterSurfaceView;
    /*private GPUImageFilter mFilter;
    private GPUImageFilterTools.FilterAdjuster mFilterAdjuster;
    private GPUImageView mGPUImageView;*/

    public GalleryFragment() {
    }

    public static GalleryFragment newInstance(String uri, String filterName) {
        Bundle args = new Bundle();
        args.putString("uri", uri);
        args.putString("filter", filterName);
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
            filterName = bundle.getString("filter");
        }

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };

        loadBitmap(getRealPathFromURI(Uri.parse(imageUri), getActivity()));
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public void loadBitmap(String uri) {
        final String imageKey = uri;

        final Bitmap bitmap = getBitmapFromMemCache(imageKey);
        if (bitmap != null) {
            currentBitmap = bitmap;
            Log.d("Bitmap", "Already exists");
        } else {
            Log.d("Bitmap", "Doesn't exist");
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            currentBitmap = BitmapFactory.decodeFile(uri, options);
            //options.inSampleSize = calculateInSampleSize(options, dWidth, dHeight);
            options.inSampleSize = 4;
            options.inJustDecodeBounds = false;
            currentBitmap = BitmapFactory.decodeFile(uri, options);
            addBitmapToMemoryCache(uri, currentBitmap);
        }
    }

    public String getRealPathFromURI(Uri contentUri, Context context) {
        String[] proj = { MediaStore.Images.Media.DATA };

        CursorLoader cursorLoader = new CursorLoader(
                context,
                contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        int column_index =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        imageFilterSurfaceView = (ImageFilterSurfaceView) view.findViewById(R.id.filterSurface);
        imageFilterSurfaceView.setRenderer(imageUri, getActivity());
        //imageFilterSurfaceView.getRenderer().setCurrentFilter(filterName, (float) 1);
        Toast.makeText(getActivity(),filterName,Toast.LENGTH_SHORT).show();

        /*imageFilterSurfaceView.setRenderer(imageUri, getActivity(), currentBitmap);
        imageFilterSurfaceView.getRenderer().setCurrentFilter(filterName, (float) 1);
        Toast.makeText(getActivity(),filterName,Toast.LENGTH_SHORT).show();*/

        //imageFilterSurfaceView = new ImageFilterSurfaceView(getActivity(),imageUri);


        FloatingActionButton filter = (FloatingActionButton) view.findViewById(R.id.Filter);
        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GPUImageFilterTools.showDialog(getActivity(), new GPUImageFilterTools.OnGpuImageFilterChosenListener() {
                    @Override
                    public void onGpuImageFilterChosenListener(GPUImageFilter filter) {
                    }
                });
            }
        });

        return view;
    }

}