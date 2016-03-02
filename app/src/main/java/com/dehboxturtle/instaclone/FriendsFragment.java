package com.dehboxturtle.instaclone;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {
    EditText searchField;
    RecyclerView friends;
    Firebase mFirebaseRef;
    FriendAdapter mAdapter;
    ArrayList<Friend> myFriends;
    ArrayList<Friend> allFriends;
    RecyclerView.LayoutManager mLayoutMaganager;
    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        friends = null;
        searchField = null;
        mAdapter = null;
        mFirebaseRef = null;
        myFriends = null;

        Log.i("friendfrag", "onDestoryView");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mFirebaseRef = new Firebase(getString(R.string.firebase_root));

        myFriends = new ArrayList<>();
        allFriends = new ArrayList<>();

        friends = (RecyclerView) getView().findViewById(R.id.friendList);
        friends.setHasFixedSize(true);

        mLayoutMaganager = new LinearLayoutManager(getContext());
        friends.setLayoutManager(mLayoutMaganager);

        mAdapter = new FriendAdapter(myFriends);

        friends.setAdapter(mAdapter);

        searchField = (EditText) getView().findViewById(R.id.searchField);
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<Friend> temp = filter(allFriends, s.toString());
                mAdapter.animateTo(temp);
                friends.scrollToPosition(0);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Firebase friendlist = mFirebaseRef.child("userdata/" + mFirebaseRef.getAuth().getUid() + "/friends");
        friendlist.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> uids = dataSnapshot.getChildren();
                for (DataSnapshot d : uids) {
                    String uid = d.getKey();
                    mFirebaseRef.child("users/" + uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Friend item = dataSnapshot.getValue(Friend.class);
                            item.setUid(dataSnapshot.getKey());
                            myFriends.add(item);
                            allFriends.add(item);
                            mAdapter.notifyDataSetChanged();
                            Log.i("friend added to list", item.getDisplay_name());
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        Log.i("friendfrag", "OnActivityCreated");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replaced with my own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        Log.i("friendfrag", "OnCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("friendfrag", "OnCreateView");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }

    public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {
        private ArrayList<Friend> dataset;

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView avatar;
            TextView name;
            ProgressBar progress;
            String uid;

            public ViewHolder(View view) {
                super(view);

                avatar = (ImageView) view.findViewById(R.id.avatar);
                name = (TextView) view.findViewById(R.id.name);
                progress = (ProgressBar) view.findViewById(R.id.progress);
                view.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FriendProfile.class);
                intent.putExtra("uid", uid);
                startActivity(intent);
            }
        }

        public FriendAdapter(ArrayList<Friend> friends) {
            dataset = friends;
            Log.i("friendfrag", "Made Friend Adapter");
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getContext())
                        .inflate(R.layout.friend_list_item, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final Friend f = myFriends.get(position);
            holder.name.setText(f.getDisplay_name());
            holder.uid = f.getUid();
            new AsyncTask<ViewHolder, Void, Bitmap>() {
                private ViewHolder v;

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    super.onPostExecute(bitmap);
                    v.progress.setVisibility(View.GONE);
                    v.avatar.setVisibility(View.VISIBLE);
                    v.avatar.setImageBitmap(bitmap);
                }

                @Override
                protected Bitmap doInBackground(ViewHolder... params) {
                    v = params[0];
                    String url = f.getAvatar();
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
            return dataset.size();
        }

        public void setDataset(ArrayList<Friend> data) {
            dataset = data;
        }

        public Friend removeItem(int position) {
            final Friend f = dataset.remove(position);
            notifyItemRemoved(position);
            return f;
        }

        public void addItem(int position, Friend f) {
            dataset.add(position, f);
            notifyItemInserted(position);
        }

        public void moveItem(int fromPosition, int toPosition) {
            final Friend f = dataset.remove(fromPosition);
            dataset.add(toPosition, f);
            notifyItemMoved(fromPosition, toPosition);
        }

        public void animateTo(List<Friend> fs) {
            applyAndAnimateRemovals(fs);
            applyAndAnimateAdditions(fs);
            applyAndAnimateMovedItems(fs);
        }

        private void applyAndAnimateRemovals(List<Friend> newfriends) {
            for (int i = dataset.size()-1; i >=0; i--) {
                final Friend f = dataset.get(i);
                if (!newfriends.contains(f)) {
                    removeItem(i);
                }
            }
        }

        private void applyAndAnimateAdditions(List<Friend> newFriends) {
            for (int i = 0, count = newFriends.size(); i < count; i++) {
                final Friend f = newFriends.get(i);
                if (!dataset.contains(f)) {
                    addItem(i, f);
                }
            }
        }

        private void applyAndAnimateMovedItems(List<Friend> newFriends) {
            for (int toPosition = newFriends.size() - 1; toPosition >=0; toPosition--) {
                final Friend f = newFriends.get(toPosition);
                final int fromPosition = dataset.indexOf(f);
                if (fromPosition >= 0 && fromPosition != toPosition) {
                    moveItem(fromPosition, toPosition);
                }
            }
        }
    }

    private List<Friend> filter(List<Friend> fl, String query) {
        query = query.toLowerCase();

        final List<Friend> filteredModelList = new ArrayList<>();
        for (Friend f : fl) {
            final String text = f.getDisplay_name().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(f);
            }
        }
        return filteredModelList;
    }
}