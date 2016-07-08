package com.example.pixuredlinux3.simplecamera2demo;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.google.common.base.Optional;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MyIO {

	private static final int BITMAP_SIZE_LIMIT_ON_SERVER = 1280;

	private static final String TAG = "MyIO";
	private static final String JPEG_FILE_PREFIX = "JPEG_";
	private static final String PIXURED_DIRECTROY = "/Pixured";
	private static final String JPEG_FILE_SUFFIX = ".jpg";

	/**
	 * Creates a JPEG file with time stamp as its name in the pictures directory
	 * of the external file directory. It should be called in a background
	 * thread.
	 *
	 * All
	 * @param context
	 * @return
	 * @throws IOException
	 */
	public static File createPublicImageFile(Context context) throws IOException {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.getDefault())
				.format(new Date());
		String fileName = JPEG_FILE_PREFIX + timeStamp;

		String publicDirectory = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES).getAbsolutePath();

		File privateDirectory = new File(publicDirectory, PIXURED_DIRECTROY);
		privateDirectory.mkdir();

		File imgFile = File.createTempFile(fileName, JPEG_FILE_SUFFIX, privateDirectory);

		Log.d(TAG, "Path to saveVideo captured image: " + imgFile.getAbsolutePath());
		return imgFile;
	}

	private static File createUniquePrivateImageFile(Context context, String suffix) {

		String timeStamp = String.valueOf(System.currentTimeMillis());
		String fileName = JPEG_FILE_PREFIX + timeStamp + suffix;

		File imgFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
				fileName + JPEG_FILE_SUFFIX);

		return imgFile;
	}

	/**
	 * This function creates a thread and tries to delete the file associated
	 * with the URL.
	 * 
	 * @param uri
	 * @param context
	 */
	public static void deleteUri_async(Context context, Uri uri) {
		new DeleteURITask().execute(new DeleteURITaskParam(context, uri));
	}

	public static void deleteFile_async(String filePath) {
		new DeleteFileTask().execute(filePath);
	}

	public static void deleteFiles_async(String... filePaths) {
		for (int i = 0; i < filePaths.length; i++) {
			deleteFile_async(filePaths[i]);
		}
	}

	/**
	 * Gets real path from a URI of a media image.
	 * 
	 * @param context
	 * @param imgUri
	 * @return
	 */
	private static String getRealPathFromImageUri(Context context, Uri imgUri) {
		String path = null;
		Cursor cursor = null;
		String[] proj = { MediaStore.Images.Media.DATA };
		cursor = context.getContentResolver().query(imgUri, proj, null, null, null);
		if (cursor == null) {
			return imgUri.getPath();
		} else {
			int colInd = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			path = cursor.getString(colInd);
			cursor.close();
			return path;
		}
	}

	public static void retrieveBitmapFromFile_async(File file, boolean deleteAfterRead,
			BitmapRetrieverListener listener) {
		new ReadBitmapFromFileTask().execute(new ReadBitmapFromFileTaskParam(file, deleteAfterRead,
				listener));
	}

	public static void retrieveBitmapFromUri_async(Context context, Uri uri,
			BitmapRetrieverListener listener) {
		try {
			context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
			InputStream in = context.getContentResolver().openInputStream(uri);
			new ReadBitmapFromInStreamTask().execute(new ReadBitmapFromInStreamTaskParam(in, uri.getPath(),
					listener));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			String path = getRealPathFromImageUri(context, uri);
			if (path != null) {
				retrieveBitmapFromFile_async(new File(path), false, listener);
			} else {
				listener.onBitmapRetrieveFailed();
			}
		}
	}

	/**
	 * 
	 * Saves a bitmap to file in private directory.
	 * 
	 * @param context
	 * @param bm
	 * @param listener
	 *            - will be called with the saved file, when saving finished
	 */
	public static void saveBitmapToFile_async(Context context, Bitmap bm,
			SaveBitmapToFileListener listener) {
		new SaveBitmapToFileTask().execute(new SaveBitmapToFileTaskParam(context, bm, listener));
	}

	public static void saveBitmapsToFile_async(Context context, Bitmap[] bms,
			SaveBitmapsToFileListener listener) {
		new SaveBitmapsToFileTask().execute(new SaveBitmapsToFileTaskParam(context, bms, listener));
	}

	private static File saveBitmapToFile_sync(Context context, Bitmap bm, String fileNameSuffix) {
		try {

			File f = createUniquePrivateImageFile(context, fileNameSuffix);

			Log.d(TAG, "saveBitmapToFile_sync() - " + f.getAbsolutePath());

			FileOutputStream out = new FileOutputStream(f);
			bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.close();
			return f;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static class DeleteFileTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			String path = params[0];

			if (path != null) {
				File f = new File(path);
				if (f.delete()) {
					Log.d(TAG, "DELETED: " + f.getPath());
				} else {
					Log.e(TAG, "FAILED ON DELETE: " + f.getPath());
				}
			}

			return null;
		}

	}

	private static class DeleteURITaskParam {
		final Context mContext;
		final Uri mUri;

		DeleteURITaskParam(Context context, Uri uri) {
			mContext = context;
			mUri = uri;
		}
	}

	private static class DeleteURITask extends AsyncTask<DeleteURITaskParam, Void, Void> {

		private DeleteURITaskParam param;

		@Override
		protected Void doInBackground(DeleteURITaskParam... params) {
			param = params[0];

			String path = getRealPathFromImageUri(param.mContext, param.mUri);
			if (path != null) {
				File f = new File(path);
				if (f.delete()) {
					Log.d(TAG, "DELETED: " + f.getPath());
				} else {
					Log.e(TAG, "FAILED ON DELETE: " + f.getPath());
				}
			}

			return null;
		}

	}

	private static class ReadBitmapFromFileTaskParam {
		final File mFile;
		final boolean mDeleteAfterRead;
		final BitmapRetrieverListener mListener;

		ReadBitmapFromFileTaskParam(File file, boolean deleteAfterRead, BitmapRetrieverListener l) {
			mFile = file;
			mDeleteAfterRead = deleteAfterRead;
			mListener = l;
		}
	}

	private static class ReadBitmapFromFileTask extends
			AsyncTask<ReadBitmapFromFileTaskParam, Void, Bitmap> {

		ReadBitmapFromFileTaskParam param;

		@Override
		protected Bitmap doInBackground(ReadBitmapFromFileTaskParam... params) {
			param = params[0];

			Bitmap bm = null;
			Optional<Bitmap> op = PixuredBitmapDecoder.getDecoder()
					.setLongerEdgeMax(BITMAP_SIZE_LIMIT_ON_SERVER).decode(param.mFile);
			if (op.isPresent()) {
				bm = op.get();
			}

			if (param.mDeleteAfterRead) {
				param.mFile.delete();
			}

			return bm;
		}

		@Override
		protected void onPostExecute(Bitmap bm) {
			if (bm != null) {
				param.mListener.onBitmapRetrieved(bm);
			} else {
				param.mListener.onBitmapRetrieveFailed();
			}
		}
	}

	private static class ReadBitmapFromInStreamTaskParam {
		final InputStream mIn;
		final BitmapRetrieverListener mListener;
        final String mPath;
		ReadBitmapFromInStreamTaskParam(InputStream in, String uriPath, BitmapRetrieverListener l) {
			mIn = in;
			mListener = l;
            mPath = uriPath;
		}
	}

	private static class ReadBitmapFromInStreamTask extends
			AsyncTask<ReadBitmapFromInStreamTaskParam, Void, Bitmap> {

		ReadBitmapFromInStreamTaskParam param;

		@Override
		protected Bitmap doInBackground(ReadBitmapFromInStreamTaskParam... params) {
			param = params[0];
            int exifOrientation = 1;
            try {
                ExifInterface exif = new ExifInterface(param.mPath);
                exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                Log.i("yolo", "the exit data is " + exifOrientation);
            } catch (IOException e) {
                e.printStackTrace();
            }
			Bitmap bm = null;
			Optional<Bitmap> op = PixuredBitmapDecoder.getDecoder()
					.setLongerEdgeMax(BITMAP_SIZE_LIMIT_ON_SERVER).decode(param.mIn, exifOrientation);
			if (op.isPresent()) {
				bm = op.get();
			}

			return bm;
		}

		@Override
		protected void onPostExecute(Bitmap bm) {
			if (bm != null) {
				param.mListener.onBitmapRetrieved(bm);
			} else {
				param.mListener.onBitmapRetrieveFailed();
			}
		}
	}

	private static class SaveBitmapToFileTaskParam {
		final Context mContext;
		final Bitmap mBitmap;
		final SaveBitmapToFileListener mListener;

		SaveBitmapToFileTaskParam(Context context, Bitmap bitmap, SaveBitmapToFileListener listener) {
			mContext = context;
			mBitmap = bitmap;
			mListener = listener;
		}
	}

	private static class SaveBitmapToFileTask extends
			AsyncTask<SaveBitmapToFileTaskParam, Void, File> {

		private SaveBitmapToFileTaskParam param;

		@Override
		protected File doInBackground(SaveBitmapToFileTaskParam... params) {
			param = params[0];
			return saveBitmapToFile_sync(param.mContext, param.mBitmap, "");
		}

		@Override
		protected void onPostExecute(File file) {
			if (file != null) {
				param.mListener.onSaved(file);
			} else {
				param.mListener.onFailed();
			}
		}
	}

	private static class SaveBitmapsToFileTaskParam {
		final Context mContext;
		final Bitmap[] mBitmaps;
		final SaveBitmapsToFileListener mListener;

		SaveBitmapsToFileTaskParam(Context context, Bitmap[] bms, SaveBitmapsToFileListener listener) {
			mContext = context;
			mBitmaps = bms;
			mListener = listener;
		}
	}

	private static class SaveBitmapsToFileTask extends
			AsyncTask<SaveBitmapsToFileTaskParam, Void, ArrayList<File>> {

		private SaveBitmapsToFileTaskParam param;

		@Override
		protected ArrayList<File> doInBackground(SaveBitmapsToFileTaskParam... params) {
			param = params[0];

			ArrayList<File> files = new ArrayList<File>();
			for (int i = 0; i < param.mBitmaps.length; i++) {
				files.add(saveBitmapToFile_sync(param.mContext, param.mBitmaps[i],
						String.valueOf(i)));
			}
			return files;
		}

		@Override
		protected void onPostExecute(ArrayList<File> files) {
			param.mListener.onSaved(files);
		}
	}

	public interface BitmapRetrieverListener {
		void onBitmapRetrieved(Bitmap bm);

		void onBitmapRetrieveFailed();
	}

	public interface SaveBitmapToFileListener {
		void onSaved(File f);

		void onFailed();
	}

	public interface SaveBitmapsToFileListener {
		void onSaved(ArrayList<File> files);
	}
}
