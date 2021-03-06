package com.example.hellvox.kappetijnmathijspset6;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Complete extends AppCompatActivity {

    // Initialize variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TextView mScoreText;
    private TextView mScoreNumber;
    private TextView mKarmaEarned;
    private Button mBackbutton;
    private int mScore;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete);
        mContext = getApplicationContext();

        // Get the variables needed from the previous activity.
        Intent intent = getIntent();
        mScore = intent.getIntExtra("score", 0);
        int amount = intent.getIntExtra("amount", 0);
        int correct = intent.getIntExtra("correct", 0);

        // Setup the user and database connection.
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Assign the views to the variables.
        setViews();

        // Calculate the user score for image and percentage.
        float score_image = (float) correct / amount;
        int score_percent = Math.round(score_image*100);

        // Set the content to the views.
        mScoreText.setText(R.string.Complete_score);
        mScoreNumber.setText(score_percent+"%");
        mKarmaEarned.setText("Karma earned: "+mScore);
        setImage(score_image);

        // Update user score in the database.
        FirebaseUser user = mAuth.getCurrentUser();
        String userId = user.getUid();
        readScore(userId);

        // Set listeners.
        mBackbutton.setOnClickListener(new backListener());
    }

    private void setViews() {
        mScoreText = findViewById(R.id.Complete_correct);
        mScoreNumber = findViewById(R.id.Complete_percent);
        mKarmaEarned = findViewById(R.id.Complete_earned);
        mBackbutton = findViewById(R.id.Complete_back);
    }

    // Function to set the image to the view, depending on the user score.
    private void setImage(double score_image) {
        ImageView image = findViewById(R.id.imageView2);
        if (score_image == 1) {
            image.setImageResource(R.drawable.perfect);
        }
        if (score_image >= 0.7 && score_image < 1 ) {
            image.setImageResource(R.drawable.welldone);
        }
        if (score_image > 0.4 && score_image < 0.7 ) {
            image.setImageResource(R.drawable.notbad);
        }
        if (score_image <= 0.4 ) {
            image.setImageResource(R.drawable.badscore);
        }
    }

    // Function to get the old user score.
    public void readScore(final String id) {
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User aUser = dataSnapshot.child("users").child(id).getValue(User.class);
                mScore = mScore + aUser.karma;
                TextView totalKarma = findViewById(R.id.Complete_karma);
                totalKarma.setText("Total Karma: " + mScore);
                updateScore(id);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabase.addListenerForSingleValueEvent(postListener);
    }

    // Function replace the user score with the new one in the database.
    private void updateScore(String userId) {
        mDatabase.child("users").child(userId).child("karma").setValue(mScore);
    }

    // Listener for the button on the layout
    private class backListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            finish();
        }
    }

    // Create the menu.
    @Override
    public boolean onCreateOptionsMenu(Menu log) {
        MenuInflater inflater = getMenuInflater();
        if (Functions.userState(mAuth)) {
            inflater.inflate(R.menu.menu_logout, log);
        } else {
            inflater.inflate(R.menu.menu_login, log);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem log) {
        // Handle item selection
        switch (log.getItemId()) {
            case R.id.Logout:
                Functions.Logout(mContext, mAuth);
                return true;
            case R.id.Login:
                FragmentTransaction fragt = getSupportFragmentManager().beginTransaction();
                LoginDialog fragment = new LoginDialog();
                fragment.show(fragt, "dialog");
                return true;
            case R.id.Rules:
                FragmentTransaction fragtransTwo = getSupportFragmentManager().beginTransaction();
                InfoDialog infofragment = new InfoDialog();
                infofragment.show(fragtransTwo, "info");
                return true;
            default:
                return super.onOptionsItemSelected(log);
        }
    }
}
