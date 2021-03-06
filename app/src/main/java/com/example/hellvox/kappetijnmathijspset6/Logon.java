package com.example.hellvox.kappetijnmathijspset6;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;


public class Logon extends AppCompatActivity {

    // Initialize variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ProgressBar mProgressBar;
    private ListView topScores;
    private TextView mTopUs;
    private TextView mGuestText;
    private Button mStartTrivia;
    private LinearLayout mHeader;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logon);
        mContext =getApplicationContext();

        // Setup the user and database connection.
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = mAuth.getCurrentUser();

        // Assign the views to the variables.
        assignViews();

        // Check for internet to inform user.
        if (!Functions.isOnline(mContext)) {
            Snackbar.make(findViewById(android.R.id.content), "No internet connection",
                    Snackbar.LENGTH_LONG).show();
        }

        // If user is logged in, setup the activity, else logout and go to login screen.
        if (user != null) {
            mProgressBar.setVisibility(View.VISIBLE);
            String userId = user.getUid();
            readUserFromDB(userId);
            getTopScores();
            getGuest(userId);
        } else goToHome();

        // Set listeners.
        mStartTrivia.setOnClickListener(new startListener());
    }

    // Function to assign views to the variables.
    private void assignViews() {
        mStartTrivia = findViewById(R.id.Start);
        mProgressBar = findViewById(R.id.progressBar2);
        topScores = findViewById(R.id.Logon_top);
        mTopUs = findViewById(R.id.logon_top);
        mGuestText = findViewById(R.id.textView5);
        mHeader = findViewById(R.id.list_header);
    }

    // Function to get the user info from the database and set views accordingly.
    private void readUserFromDB(final String id) {
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User aUser = dataSnapshot.child("users").child(id).getValue(User.class);

                TextView tv = findViewById(R.id.Logon_begin);
                TextView karma = findViewById(R.id.Logon_karma);

                tv.setText(getString(R.string.hello_message)+aUser.username + getString(R.string.Ex));
                karma.setText(getString(R.string.your_karma)+aUser.karma);
                mTopUs.setText(getString(R.string.top_users));
                mHeader.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabase.addValueEventListener(postListener);
    }

    // Function to check if the user is a guest and set views accordingly.
    private void getGuest(final String id) {
        DatabaseReference reference = mDatabase.child("users").child(id);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User aUser = dataSnapshot.getValue(User.class);
                if (aUser.guest == 1) {
                    mGuestText.setText(Html.fromHtml("You are playing as guest, " +
                            "karma will not be saved! " +
                            "<font color=#2196F3>Register now!</font>"));
                    mGuestText.setVisibility(View.VISIBLE);
                    mGuestText.setOnClickListener(new registerListener());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        reference.addListenerForSingleValueEvent(postListener);
    }

    // Inspired by: https://stackoverflow.com/questions/38965731/how-to-get-all-childs-data-in-firebase-database
    // Function to get topScores from all users via a user object.
    private void getTopScores() {
        DatabaseReference reference = mDatabase.child("users");
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                collectTopScores((Map<String,Object>) dataSnapshot.getValue());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        reference.addListenerForSingleValueEvent(postListener);
    }

    // Function to collect all the top scores in a loop from the object of the previous function.
    private void collectTopScores(Map<String,Object> users) {
        ArrayList<UserTop> KarmaList = new ArrayList<>();
        int totalKarmaAll = 0;
        //iterate through each user, ignoring their UID
        for (Map.Entry<String, Object> entry : users.entrySet()){
            //Get user map
            Map singleUser = (Map) entry.getValue();
            if (((Long) singleUser.get("guest")).intValue() == 0) {
                KarmaList.add(new UserTop((String) singleUser.get("username"),
                        (Long) singleUser.get("karma")));
                totalKarmaAll = totalKarmaAll +  ((Long) singleUser.get("karma")).intValue();
            }
        }
        TextView textView = findViewById(R.id.logon_total);
        textView.setText(getString(R.string.AllUserKarma)+totalKarmaAll);
        // Sort the list from high to low.
        Collections.sort(KarmaList, new Comparator<UserTop>() {
            @Override
            public int compare(UserTop userTop, UserTop t1) {
                return t1.getKarma().compareTo(userTop.getKarma());
            }
        });
        ArrayAdapter<UserTop> adapter = new UserTopAdapter(getApplicationContext(),
                R.layout.row_user, KarmaList);
        topScores.setAdapter(adapter);
    }

    // Function to got to the register page.
    private void goToHome() {
        startActivity(new Intent(Logon.this, RegLog.class));
        finish();
    }

    // Listener if the user is a guest.
    private class registerListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            FirebaseAuth.getInstance().signOut();
            goToHome();
        }
    }

    // Listener to start a trivia game.
    private class startListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Logon.this, SelectTrivia.class);
            startActivity(intent);
        }
    }

    // Function to refresh the topscores after a user finished a trivia.
    public void onResume() {
        super.onResume();
        getTopScores();
    }

    // Function to check if the user is really logged in.
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            goToHome();
        }
    }

    // Functions to create the menu
    @Override
    public boolean onCreateOptionsMenu(Menu option) {
        MenuInflater inflater = getMenuInflater();
        if (Functions.userState(mAuth)) {
            inflater.inflate(R.menu.menu_logout, option);
        } else {
            inflater.inflate(R.menu.menu_login, option);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem option) {
        // Handle item selection
        switch (option.getItemId()) {
            case R.id.Logout:
                Functions.Logout(mContext, mAuth);
                return true;
            case R.id.Login:
                FragmentTransaction fragtrans = getSupportFragmentManager().beginTransaction();
                LoginDialog fragment = new LoginDialog();
                fragment.show(fragtrans, "dialog");
                return true;
            case R.id.Rules:
                FragmentTransaction fragRules = getSupportFragmentManager().beginTransaction();
                InfoDialog infofragment = new InfoDialog();
                infofragment.show(fragRules, "info");
                return true;
            default:
                return super.onOptionsItemSelected(option);
        }
    }
}
