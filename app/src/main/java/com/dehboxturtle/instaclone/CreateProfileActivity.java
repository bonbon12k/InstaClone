package com.dehboxturtle.instaclone;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class CreateProfileActivity extends AppCompatActivity {

    Firebase mFirebaseRef;
    String uid;
    EditText FirstName;
    EditText LastName;
    EditText DisplayName;
    LinearLayout Profile;
    TextSwitcher mGuide;
    Button mInputButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);
        FirstName = (EditText) findViewById(R.id.FirstName);
        LastName = (EditText) findViewById(R.id.LastName);
        DisplayName = (EditText) findViewById(R.id.DisplayName);
        mGuide = (TextSwitcher) findViewById(R.id.ProfileGuide);
        mInputButton = (Button) findViewById(R.id.inputButton);
        Profile = (LinearLayout) findViewById(R.id.ProfileInfo);
        Handler scheduler = new Handler();

        // initialize the guide to animate text changing with fading
        mGuide.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView text = new TextView(getApplicationContext());
                text.setGravity(Gravity.CENTER_HORIZONTAL);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER_HORIZONTAL
                );

                text.setLayoutParams(params);

                text.setTextSize(25);
                text.setTextColor(Color.BLACK);
                return text;
            }
        });
        mGuide.setInAnimation(this, android.R.anim.slide_in_left);
        mGuide.setOutAnimation(this, android.R.anim.slide_out_right);

        // set guide initial text
        scheduler.postDelayed(new AnimateText("Welcome to InstaClone :)"), 500);

        scheduler.postDelayed(new AnimateTextWithInput("Please tell us a little about yourself"), 2500);

        // get the firebase root and make sure user is still logged in
        mFirebaseRef = new Firebase(getString(R.string.firebase_root));
        AuthData data = mFirebaseRef.getAuth();
        if (data != null) {
            uid = data.getUid();
        }
        mFirebaseRef.child("users/" + uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("ProfileCreate", "Got User Profile");
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        Profile.setVisibility(View.INVISIBLE);
        mInputButton.setVisibility(View.INVISIBLE);
        View.OnFocusChangeListener listen = new LostFocusHandler();
        FirstName.setOnFocusChangeListener(listen);
        LastName.setOnFocusChangeListener(listen);
        DisplayName.setOnFocusChangeListener(listen);
    }


    public void getInput(View view) {
        String dname = DisplayName.getText().toString();

        // Check for a valid DisplayName
        if (dname.length() < 4) {
            DisplayName.setError(getString(R.string.display_name_too_short));
            DisplayName.requestFocus();
        }

    }

    public class AnimateText implements Runnable {
        AnimateAsync anim;

        public AnimateText(String text) {
            anim = new AnimateAsync(text);
        }

        @Override
        public void run() {
            anim.execute();
        }

        private class AnimateAsync extends AsyncTask<Void, Void, Void> {
            String text;

            public AnimateAsync(String text) {
                super();
                this.text = text;
            }

            @Override
            protected Void doInBackground(Void... params) {
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mGuide.setText(text);
            }
        }
    }

    public class AnimateTextWithInput implements Runnable {
        AnimateAsyncWithInput anim;

        public AnimateTextWithInput(String text) {
            anim = new AnimateAsyncWithInput(text);
        }

        @Override
        public void run() {
            anim.execute();
        }

        private class AnimateAsyncWithInput extends AsyncTask<Void, Void, Void> {
            String text;

            public AnimateAsyncWithInput(String text) {
                super();
                this.text = text;
            }

            @Override
            protected Void doInBackground(Void... params) {
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Profile.setVisibility(View.VISIBLE);
                mGuide.setText(text);
                mInputButton.setVisibility(View.VISIBLE);
            }
        }
    }

    public class LostFocusHandler implements View.OnFocusChangeListener {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
            else {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInputFromWindow(v.getWindowToken(), 0, 0);
            }
        }
    }
}
