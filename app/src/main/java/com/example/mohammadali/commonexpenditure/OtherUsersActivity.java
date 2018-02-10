package com.example.mohammadali.commonexpenditure;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.y;

public class OtherUsersActivity extends AppCompatActivity {


    private ImageView mNetworkStatus;
    private ExpenseAdapter mExpenseAdapter;
    private TextView mTotalTextView;
    private ProgressBar mProgressBar;

    private DatabaseReference mExpenseDatabaseReference;
    private ChildEventListener mChildEventListener;
    private DatabaseReference mOtherUserDatabaseTotalReference;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.other_users_activity_menu, menu);
        mTotalTextView = new TextView(this);
        mTotalTextView.setText("0");
        mTotalTextView.setPadding(10, 10, 10, 10);
        mTotalTextView.setTypeface(null, Typeface.BOLD);
        mTotalTextView.setTextSize(24);
        mTotalTextView.setTextColor(Color.WHITE);
        menu.add("Total").setActionView(mTotalTextView).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        mOtherUserDatabaseTotalReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mTotalTextView.setText(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_users);
        Bundle extra = getIntent().getExtras();
        assert extra != null;
        String mOtherUser = extra.getString(MainActivity.USERNAME_KEY);

        getSupportActionBar().setTitle(mOtherUser);

        mNetworkStatus = findViewById(R.id.network_status2);
        ListView mExpenseListView = findViewById(R.id.expenseListView2);
        mProgressBar = findViewById(R.id.progressBar2);

        List<ExpenseDetails> expenses = new ArrayList<>();
        mExpenseAdapter = new ExpenseAdapter(this, R.layout.item_expense, expenses);
        mExpenseListView.setAdapter(mExpenseAdapter);

        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        mExpenseDatabaseReference = mFirebaseDatabase.getReference().child("commonExpenditure/" + mOtherUser);
        mOtherUserDatabaseTotalReference = mFirebaseDatabase.getReference().child("users/" + mOtherUser + "/total");

        new CheckingActiveNetwork2().execute();

        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.VISIBLE);
        attachDatabaseReadListener();

    }


    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    ExpenseDetails expenseDetails = dataSnapshot.getValue(ExpenseDetails.class);
                    assert expenseDetails != null;
                    String time = expenseDetails.getDateTime();
                    int pos = time.indexOf(',');
                    String day = time.substring(0, pos);
                    expenseDetails.setDay(day);
                    mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                    mNetworkStatus.setVisibility(View.INVISIBLE);
                    mExpenseAdapter.add(expenseDetails);
                }

                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                public void onCancelled(DatabaseError databaseError) {
                }
            };
            mExpenseDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class CheckingActiveNetwork2 extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            Boolean status = false;
            try {
                status = NetworkUtils.hasActiveInternet();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return status;
        }

        @Override
        protected void onPostExecute(Boolean status) {
            if (!status) {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                Toast.makeText(OtherUsersActivity.this, "You are not connected", Toast.LENGTH_SHORT).show();
                mNetworkStatus.setVisibility(View.VISIBLE);
            }
        }
    }
}
