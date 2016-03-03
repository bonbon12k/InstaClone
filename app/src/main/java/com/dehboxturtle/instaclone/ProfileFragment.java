package com.dehboxturtle.instaclone;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.cloudinary.Cloudinary;
import com.cloudinary.android.Utils;
import com.cloudinary.utils.ObjectUtils;
import com.firebase.client.*;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ProfileFragment extends Fragment {

    Firebase mFirebaseRef;
    Firebase mPhotosRef;
    private String uid;
    Cloudinary cloudinary;
    ArrayList<String> imageurls;
    MyAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView mRecyclerView;
    private Uri mImageUri;
    private String mCurrentPhotoPath;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        mFirebaseRef = new Firebase(getString(R.string.firebase_root));
        if (getArguments() != null && getArguments().containsKey("uid")) {
            uid = getArguments().getString("uid");
            fab.setVisibility(View.INVISIBLE);
        } else {
            fab.setVisibility(View.VISIBLE);
            uid = mFirebaseRef.getAuth().getUid();
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyStoragePermissions(getActivity());
                File photo = null;
                try {
                    photo = createImageFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (photo == null) {
                    Log.i("file creation", "could not make photo file");
                }
                mImageUri = Uri.fromFile(photo);
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
                startActivityForResult(cameraIntent, 0);
            }
        });
        mPhotosRef = mFirebaseRef.child("photos/" + uid);
        cloudinary = new Cloudinary(Utils.cloudinaryUrlFromContext(getContext()));
        imageurls = new ArrayList<>();
        mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.photos);

        mLayoutManager = new GridLayoutManager(getContext(), 3);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyAdapter(imageurls);
        mRecyclerView.setAdapter(mAdapter);

        loadImages();
    }

    private void loadImages() {
        mFirebaseRef.child("photos/" + uid + "/").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String img_url = "" + dataSnapshot.getValue();
                Log.i("Load Images", img_url);
                Log.i("Load Images", dataSnapshot.getKey() + "");
                imageurls.add(img_url);
                int i = Integer.parseInt("" + dataSnapshot.getKey());
                mAdapter.notifyItemInserted(i);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                firebaseError.toException().printStackTrace();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 0) return;
        Toast.makeText(getContext(), "Processing", Toast.LENGTH_SHORT).show();
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                ContentResolver cr = getActivity().getContentResolver();
                cr.notifyChange(mImageUri, null);
                Bitmap bitmap = null;
                Bitmap thumb = null;
                String filepath = mImageUri.getEncodedPath();
                try {
                    thumb = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(filepath), 250, 250);
                    bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(filepath), 1080, 1080);
                    //bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, mImageUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                thumb.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                byte[] bitmapdata = bos.toByteArray();
                final InputStream bs = new ByteArrayInputStream(bitmapdata);

                ByteArrayOutputStream bosfull = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bosfull);
                byte[] bitmapdatafull = bosfull.toByteArray();
                final InputStream bsfull = new ByteArrayInputStream(bitmapdatafull);
                thumb = null;
                bitmap = null;
                mFirebaseRef.child("users/" + uid + "/image_count").runTransaction(new Transaction.Handler() {
                    long imageindex;

                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        if (mutableData.getValue() == null) {
                            mutableData.setValue(1);
                            imageindex = 0;
                            Log.i("Doing Trans IF", imageindex + "");
                            return Transaction.success(mutableData);
                        } else {
                            imageindex = (long) mutableData.getValue();
                            mutableData.setValue(imageindex + 1);
                            Log.i("Doing Trans ELSE", imageindex + "");
                            return Transaction.success(mutableData);
                        }
                    }

                    @Override
                    public void onComplete(FirebaseError firebaseError, boolean b, DataSnapshot dataSnapshot) {
                        if (!b) {
                            firebaseError.toException().printStackTrace();
                            return;
                        }
                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... params) {
                                try {
                                    Log.i("Upload Photo", imageindex + "");
                                    String stamp = System.currentTimeMillis() + "";
                                    cloudinary.uploader().upload(bs, ObjectUtils.asMap("public_id", uid + "_" + imageindex + "_" + stamp));
                                    String imageurl = cloudinary.url().generate(uid + "_" + imageindex + "_" + stamp);
                                    mPhotosRef.child(imageindex + "").setValue(imageurl);
                                    cloudinary.uploader().upload(bsfull, ObjectUtils.asMap("public_id", uid + "_" + imageindex + "_" + stamp + "_full"));
                                } catch (IOException e) {
                                    e.printStackTrace();

                                }
                                return null;
                            }
                        }.execute();
                    }
                });
                return null;
            }
        }.execute();
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ImVH> {
        private ArrayList<String> mDataset;

        public class ImVH extends RecyclerView.ViewHolder implements View.OnClickListener {
            public ImageView mImageView;
            public ProgressBar mProgress;

            public ImVH (View v) {
                super(v);
                v.setClickable(true);
                v.setFocusable(true);
                mImageView = (ImageView) v.findViewById(R.id.imageView3);
                mProgress = (ProgressBar) v.findViewById(R.id.progressBar2);
                v.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                // go fullscreen
                Intent fullscreen = new Intent(getContext(), FullscreenImageActivity.class);
                fullscreen.putExtra("position", getLayoutPosition());
                fullscreen.putExtra("images", mDataset);
                startActivity(fullscreen);
            }
        }
        public MyAdapter(ArrayList<String> myDataset) {
            mDataset = myDataset;
        }

        @Override
        public ImVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.image_list_item, parent, false);
            ImVH vh = new ImVH(v);
            Log.i("CreateVH", "created a view for the list");
            return vh;
        }

        @Override
        public void onBindViewHolder(ImVH holder, final int position) {
            new AsyncTask<ImVH, Void, Bitmap>() {
                private ImVH v;

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    super.onPostExecute(bitmap);
                    v.mProgress.setVisibility(View.GONE);
                    v.mImageView.setVisibility(View.VISIBLE);
                    v.mImageView.setImageBitmap(bitmap);
                }

                @Override
                protected Bitmap doInBackground(ImVH... params) {
                    v = params[0];
                    String url = mDataset.get(position);
                    Bitmap image = null;
                    try {
                        InputStream in = new URL(url).openStream();
                        image = BitmapFactory.decodeStream(in);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return image;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, holder);
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }

    /**
            * Checks if the app has permission to write to device storage
    *
            * If the app does not has permission then the user will be prompted to grant permissions
    *
            * @param activity
    */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}
