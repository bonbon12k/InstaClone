package com.dehboxturtle.instaclone;


import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {
    EditText searchField;
    ListView friends;
    Firebase mFirebaseRef;
    FriendAdapter mAdapter;
    ArrayList<Friend> myFriends;

    public FriendsFragment() {
        // Required empty public constructor
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
        mFirebaseRef = new Firebase(getString(R.string.firebase_root));

        myFriends = new ArrayList<>();

        mAdapter = new FriendAdapter(getContext(), myFriends);

        friends.setAdapter(mAdapter);

        Firebase friendlist = mFirebaseRef.child("userdata/" + mFirebaseRef.getAuth().getUid() + "/friends");
        friendlist.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> uids = dataSnapshot.getChildren();
                for (DataSnapshot d : uids) {
                    String uid = d.getKey();
                    mFirebaseRef.child("users/" + uid).addListenerForSingleValueEvent();


                    //TODO: I AM HERE ^

                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        })
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }

    public class FriendAdapter extends ArrayAdapter<Friend> {
        public FriendAdapter(Context context, ArrayList<Friend> friends) {
            super(context, 0, friends);
        }


    }

}
