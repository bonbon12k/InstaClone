package com.dehboxturtle.instaclone;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.cloudinary.Cloudinary;
import com.cloudinary.android.Utils;
import com.cloudinary.utils.ObjectUtils;
import com.firebase.client.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class ProfileFragment extends Fragment {

    Firebase mFirebaseRef;
    Firebase mPhotosRef;
    private String uid;
    Cloudinary cloudinary;
    ArrayList<String> imageurls;
    MyAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView mRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, 0);
            }
        });
        mFirebaseRef = new Firebase(getString(R.string.firebase_root));
        uid = mFirebaseRef.getAuth().getUid();
        mPhotosRef = mFirebaseRef.child("photos/" + uid);
        cloudinary = new Cloudinary(Utils.cloudinaryUrlFromContext(getContext()));
        imageurls = new ArrayList<>();
        mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.photos);

        mLayoutManager = new StaggeredGridLayoutManager(3, 1);
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
                mAdapter.notifyItemInserted(Integer.parseInt("" + dataSnapshot.getKey()));
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
        final Bitmap photo = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();
        final InputStream bs = new ByteArrayInputStream(bitmapdata);
        ImageView imv2 = (ImageView) getActivity().findViewById(R.id.imageView2);
        imv2.setImageBitmap(photo);
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
                            cloudinary.uploader().upload(bs, ObjectUtils.asMap("public_id", uid + "" + imageindex));
                            String imageurl = cloudinary.url().generate(uid + "" + imageindex);
                            mPhotosRef.child(imageindex + "").setValue(imageurl);
                        } catch (IOException e) {
                            e.printStackTrace();

                        }
                        return null;
                    }
                }.execute();
            }
        });
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ImVH> {
        private ArrayList<String> mDataset;

        public class ImVH extends RecyclerView.ViewHolder {
            public ImageView mImageView;
            public ProgressBar mProgress;

            public ImVH (View v) {
                super(v);
                v.setClickable(true);
                v.setFocusable(true);
                mImageView = (ImageView) v.findViewById(R.id.imageView3);
                mProgress = (ProgressBar) v.findViewById(R.id.progressBar2);
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
}
