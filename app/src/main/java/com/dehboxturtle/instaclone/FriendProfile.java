package com.dehboxturtle.instaclone;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

public class FriendProfile extends AppCompatActivity {

    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.INVISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        android.support.v4.app.FragmentManager man = getSupportFragmentManager();
        uid = getIntent().getStringExtra("uid");
        Log.i("FriendProfile", uid);
        ProfileFragment frag = new ProfileFragment();
        Bundle bun = new Bundle();
        bun.putString("uid", uid + "");
        frag.setArguments(bun);
        man.beginTransaction()
                .replace(R.id.friend_profile_frag, frag)
                .commit();
    }

}
