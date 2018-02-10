package com.example.mohammadali.commonexpenditure;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String ANONYMOUS = "anonymous";
    public static final int RC_SIGN_IN = 1;
    public static final String USERNAME_KEY = "username";
    private static final int DEFAULT_COST_LIMIT = 7;
    private static final int DEFAULT_EXPENSE_LIMIT = 100;
    private static final String USERS_FILE = "usersInfo";
    private static final String DEVICE_TOKEN = "token";
    public static DatabaseReference mUserTokenReference;
    private ImageView mNetworkStatus;
    private String[] mUsersList;
    private boolean[] mIsUserSelected;
    private ArrayList<Integer> mUsersSelectedForGift;
    private ExpenseAdapter mExpenseAdapter;
    private ProgressBar mProgressBar;
    private AutoCompleteTextView mExpenseEditTextView;
    private EditText mCostEditTextView;
    private ImageButton mSendButton;
    private ImageButton mGiftButton;
    private TextView mTotalTextView;
    private String mUsername;
    private float mTotal;
    private float safdarTotal;
    private float laraibTotal;
    private float shanuTotal;
    private float zainTotal;
    // Firebase instance variables
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mExpensesDatabaseReference;
    private DatabaseReference mUserDatabaseTotalReference;
    private DatabaseReference mSafdarTotalReference;
    private DatabaseReference mSafdarGiftReference;
    private DatabaseReference mLaraibTotalReference;
    private DatabaseReference mLaraibGiftReference;
    private DatabaseReference mShanuTotalReference;
    private DatabaseReference mShanuGiftReference;
    private DatabaseReference mZainTotalReference;
    private DatabaseReference mZainGiftReference;
    private ChildEventListener mChildEventListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getSupportActionBar().setTitle("MyExpense");
        // Initialize Firebase components
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        mUsername = getUsername();

        mExpensesDatabaseReference = mFirebaseDatabase.getReference().child("commonExpenditure/" + mUsername);
        mUserDatabaseTotalReference = mFirebaseDatabase.getReference().child("users/" + mUsername + "/total");
        mUserTokenReference = mFirebaseDatabase.getReference().child("users/" + mUsername + "/token");

        mSafdarTotalReference = mFirebaseDatabase.getReference().child("users/Syed Safdar Ali/total");
        mLaraibTotalReference = mFirebaseDatabase.getReference().child("users/syed mohd Lareb/total");
        mShanuTotalReference = mFirebaseDatabase.getReference().child("users/SHAHNAWAZ KHAN/total");
        mZainTotalReference = mFirebaseDatabase.getReference().child("users/Zainul Mirza/total");

        mSafdarGiftReference = mFirebaseDatabase.getReference().child("commonExpenditure/Syed Safdar Ali");
        mLaraibGiftReference = mFirebaseDatabase.getReference().child("commonExpenditure/syed mohd Lareb");
        mShanuGiftReference = mFirebaseDatabase.getReference().child("commonExpenditure/SHAHNAWAZ KHAN");
        mZainGiftReference = mFirebaseDatabase.getReference().child("commonExpenditure/Zainul Mirza");

        // Initialize references to views
        mProgressBar = findViewById(R.id.progressBar);
        ListView mExpenseListView = findViewById(R.id.expenseListView);
        mExpenseEditTextView = findViewById(R.id.expenseEditText);
        mCostEditTextView = findViewById(R.id.costEditText);
        mSendButton = findViewById(R.id.sendButton);
        mGiftButton = findViewById(R.id.giftButton);
        mNetworkStatus = findViewById(R.id.network_status);

        // Initialize message ListView and its adapter
        List<ExpenseDetails> expenses = new ArrayList<>();
        mExpenseAdapter = new ExpenseAdapter(this, R.layout.item_expense, expenses);
        mExpenseListView.setAdapter(mExpenseAdapter);

        ArrayAdapter<String> mItemsAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, getResources().getStringArray(R.array.items_list));
        mExpenseEditTextView.setAdapter(mItemsAdapter);
        mExpenseEditTextView.setThreshold(1);

        mUsersSelectedForGift = new ArrayList<>();
        mUsersList = getResources().getStringArray(R.array.users_list);
        mIsUserSelected = new boolean[mUsersList.length];

        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.VISIBLE);

        // Enable Send button when there's text to send
        TextWatcher mTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (mExpenseEditTextView.getText().toString().trim().length() > 0 && mCostEditTextView.getText().toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                    mSendButton.setBackgroundResource(R.drawable.ic_send_button_active);
                    mGiftButton.setEnabled(true);
                    mGiftButton.setBackgroundResource(R.drawable.ic_new_gift_red);
                } else {
                    mSendButton.setEnabled(false);
                    mSendButton.setBackgroundResource(R.drawable.ic_send_button_inactive);
                    mGiftButton.setEnabled(false);
                    mGiftButton.setBackgroundResource(R.drawable.ic_new_gift_icon);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };
        mExpenseEditTextView.addTextChangedListener(mTextWatcher);
        mExpenseEditTextView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_EXPENSE_LIMIT)});
        mCostEditTextView.addTextChangedListener(mTextWatcher);
        mCostEditTextView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_COST_LIMIT)});

        if (mUsername.compareTo(ANONYMOUS) != 0) {
            storeUsername(mUsername);
            String mToken = FirebaseInstanceId.getInstance().getToken();
            if (mToken.compareTo(getDeviceToken()) != 0) {
                storeDeviceToken(mToken);
                sendTokenInDataBase(mToken);
            }
        }
        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendExpenseToDatabase(new String[0]);
            }
        });

        mGiftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askForAnyGifts();
            }
        });
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    onSignedInInitialize(user.getDisplayName());
                } else {
                    // User is signed out
                    onSignedOutCleanup();
                    startActivityForResult(AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(
                                            Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    private void sendTokenInDataBase(String token) {
        mUserTokenReference.setValue(token).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(MainActivity.this, "NEW TOKEN SENT", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Sign-in succeeded, set up the UI
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        new CheckingActiveNetwork().execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        mExpenseAdapter.clear();
        detachDatabaseReadListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        mTotalTextView = new TextView(this);
        mTotalTextView.setText("0");
        mTotalTextView.setPadding(10, 10, 10, 10);
        mTotalTextView.setTypeface(null, Typeface.BOLD);
        mTotalTextView.setTextSize(24);
        mTotalTextView.setTextColor(Color.WHITE);
        menu.add("Total").setActionView(mTotalTextView).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        mSafdarTotalReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                safdarTotal = Float.parseFloat(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mLaraibTotalReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                laraibTotal = Float.parseFloat(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mShanuTotalReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                shanuTotal = Float.parseFloat(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mZainTotalReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                zainTotal = Float.parseFloat(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mUserDatabaseTotalReference.addValueEventListener(new ValueEventListener() {
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
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(this, OtherUsersActivity.class);
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                return true;
            case R.id.see_other_user:
                return true;
            case R.id.syed_safdar_ali:
                if (mUsername.toLowerCase().compareTo("syed safdar ali") == 0) {
                    Toast.makeText(this, "You are " + mUsername, Toast.LENGTH_SHORT).show();
                } else {
                    intent.putExtra(USERNAME_KEY, "Syed Safdar Ali");
                    startActivity(intent);
                }
                return true;
            case R.id.syed_mohd_lareb:
                if (mUsername.toLowerCase().compareTo("syed mohd lareb") == 0) {
                    Toast.makeText(this, "You are " + mUsername, Toast.LENGTH_SHORT).show();
                } else {
                    intent.putExtra(USERNAME_KEY, "syed mohd Lareb");
                    startActivity(intent);
                }
                return true;
            case R.id.shahnawaz_khan:
                if (mUsername.toLowerCase().compareTo("shahnawaz khan") == 0) {
                    Toast.makeText(this, "You are " + mUsername, Toast.LENGTH_SHORT).show();
                } else {
                    intent.putExtra(USERNAME_KEY, "SHAHNAWAZ KHAN");
                    startActivity(intent);
                }
                return true;
            case R.id.zainul_mirza:
                if (mUsername.toLowerCase().compareTo("zainul mirza") == 0) {
                    Toast.makeText(this, "You are " + mUsername, Toast.LENGTH_SHORT).show();
                } else {
                    intent.putExtra(USERNAME_KEY, "Zainul Mirza");
                    startActivity(intent);
                }
                return true;
            case R.id.reset:
                confirmResettingOfDatabase();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void confirmResettingOfDatabase() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Do you really want to reset the app?");

        builder.setCancelable(true);

        builder.setPositiveButton("Sure!!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                resetDatabase();
                dialogInterface.cancel();
            }
        });

        builder.setNegativeButton("Sorry!!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void resetDatabase() {
        float balances[] = {safdarTotal, laraibTotal, shanuTotal, zainTotal};
        float minBalance = 9999;
        for (int i = 0; i < 4; i++){
            if (minBalance > balances[i]){
                minBalance = balances[i];
            }
        }
        savePreviousDatabase();

        ExpenseDetails resetDetails;
        safdarTotal = safdarTotal - minBalance;

        mSafdarTotalReference.setValue(safdarTotal);
        resetDetails = new ExpenseDetails("Resetting", String.valueOf(safdarTotal), "Syed Safdar Ali", getCurrentTime(), "false");
        mSafdarGiftReference.push().setValue(resetDetails);

        laraibTotal = laraibTotal - minBalance;
        mLaraibTotalReference.setValue(laraibTotal);
        resetDetails = new ExpenseDetails("Resetting", String.valueOf(laraibTotal), "syed mohd Lareb", getCurrentTime(), "false");
        mLaraibGiftReference.push().setValue(resetDetails);

        shanuTotal = shanuTotal - minBalance;
        mShanuTotalReference.setValue(shanuTotal);
        resetDetails = new ExpenseDetails("Resetting", String.valueOf(shanuTotal), "SHAHNAWAZ KHAN", getCurrentTime(), "false");
        mShanuGiftReference.push().setValue(resetDetails);

        zainTotal = zainTotal - minBalance;
        mZainTotalReference.setValue(zainTotal);
        resetDetails = new ExpenseDetails("Resetting", String.valueOf(zainTotal), "Zainul Mirza", getCurrentTime(), "false");
        mZainGiftReference.push().setValue(resetDetails);
    }

    private void savePreviousDatabase() {
        final DatabaseReference fromPath = mFirebaseDatabase.getReference().child("commonExpenditure");
        final DatabaseReference toPath = mFirebaseDatabase.getReference().child("commonExpenditure_till_" + getCurrentTime());
        fromPath.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                toPath.setValue(dataSnapshot.getValue(), new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        Toast.makeText(MainActivity.this, "Database Reset successful, stored as previous!", Toast.LENGTH_SHORT).show();
                        fromPath.removeValue();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Database Reset unsuccessful!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onSignedInInitialize(String username) {
        storeUsername(username);
        attachDatabaseReadListener();
    }

    private void onSignedOutCleanup() {
        mUsername = ANONYMOUS;
        mExpenseAdapter.clear();
        detachDatabaseReadListener();
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
            mExpensesDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mExpensesDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    private String getCurrentTime() {
        Date currentTimeDate = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMM yyyy hh:mm:ss aa");
        return dateFormat.format(currentTimeDate);
    }

    private void askForAnyGifts() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Send Gift to anyone?");
        builder.setMultiChoiceItems(mUsersList, mIsUserSelected, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
                if (isChecked) {
                    if (!mUsersSelectedForGift.contains(position)) {
                        mUsersSelectedForGift.add(position);
                    } else {
                        mUsersSelectedForGift.remove(position);
                    }
                }
            }
        });

        builder.setCancelable(false);

        builder.setPositiveButton("SEND", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                String[] array = showSelectedUsers();
                sendExpenseToDatabase(array);
                cleanupUsersListSelectedForGift();
                //showSelectedUsers();
                dialogInterface.cancel();
            }
        });

        builder.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                cleanupUsersListSelectedForGift();
                dialogInterface.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addGiftsToOtherUsers(float gift, String[] array) {
        ExpenseDetails giftDetails;
        for (String anArray : array) {
            if (anArray.compareTo("Safdar") == 0) {
                safdarTotal = safdarTotal + gift;
                mSafdarTotalReference.setValue(String.valueOf(safdarTotal));

                giftDetails = new ExpenseDetails("Gift " + mUsername, String.valueOf(gift), "Syed Safdar Ali", getCurrentTime(), "true");
                mSafdarGiftReference.push().setValue(giftDetails);

                Toast.makeText(this, "Gift sent to Safdar", Toast.LENGTH_SHORT).show();

            } else if (anArray.compareTo("Laraib") == 0) {
                laraibTotal = laraibTotal + gift;
                mLaraibTotalReference.setValue(String.valueOf(laraibTotal));

                giftDetails = new ExpenseDetails("Gift " + mUsername, String.valueOf(gift), "syed mohd Lareb", getCurrentTime(), "true");
                mLaraibGiftReference.push().setValue(giftDetails);

                Toast.makeText(this, "Gift sent to Laraib", Toast.LENGTH_SHORT).show();
            } else if (anArray.compareTo("Shanu Bhai") == 0) {

                shanuTotal = shanuTotal + gift;
                mShanuTotalReference.setValue(String.valueOf(shanuTotal));

                giftDetails = new ExpenseDetails("Gift " + mUsername, String.valueOf(gift), "SHAHNAWAZ KHAN", getCurrentTime(), "true");
                mShanuGiftReference.push().setValue(giftDetails);

                Toast.makeText(this, "Gift sent to Shanu Bhai", Toast.LENGTH_SHORT).show();

            } else if (anArray.compareTo("Zainul") == 0) {

                zainTotal = zainTotal + gift;
                mZainTotalReference.setValue(String.valueOf(zainTotal));

                giftDetails = new ExpenseDetails("Gift " + mUsername, String.valueOf(gift), "Zainul Mirza", getCurrentTime(), "true");
                mZainGiftReference.push().setValue(giftDetails);

                Toast.makeText(this, "Gift sent to Zain", Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void cleanupUsersListSelectedForGift() {
        mUsersSelectedForGift.clear();
        for (int i = 0; i < mUsersList.length; i++) {
            mIsUserSelected[i] = false;
        }
    }

    private String[] showSelectedUsers() {
        String array[] = new String[mUsersSelectedForGift.size()];
        for (int i = 0; i < mUsersSelectedForGift.size(); i++) {
            array[i] = mUsersList[mUsersSelectedForGift.get(i)];
        }
        return array;
    }

    private void storeUsername(String username) {
        SharedPreferences sharedPreferences = getSharedPreferences(USERS_FILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USERNAME_KEY, username);
        editor.apply();
    }

    private String getUsername() {
        SharedPreferences sharedPreferences = getSharedPreferences(USERS_FILE, MODE_PRIVATE);
        return sharedPreferences.getString(USERNAME_KEY, ANONYMOUS);
    }

    private void storeDeviceToken(String token) {
        Toast.makeText(this, "NEW TOKEN SAVED", Toast.LENGTH_SHORT).show();
        SharedPreferences sharedPreferences = getSharedPreferences(USERS_FILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DEVICE_TOKEN, token);
        editor.apply();
    }

    private String getDeviceToken() {
        SharedPreferences sharedPreferences = getSharedPreferences(USERS_FILE, MODE_PRIVATE);
        return sharedPreferences.getString(DEVICE_TOKEN, ANONYMOUS);
    }

    private void sendExpenseToDatabase(final String[] array) {
        mUserDatabaseTotalReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mTotal = Float.parseFloat(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        final float cost = Float.parseFloat(mCostEditTextView.getText().toString());
        String time = getCurrentTime();
        ExpenseDetails expenseDetails = new ExpenseDetails(mExpenseEditTextView.getText().toString(), String.valueOf(cost), mUsername, time, "false");
        mExpensesDatabaseReference.push().setValue(expenseDetails).addOnSuccessListener(MainActivity.this, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mTotal = mTotal + cost;
                mUserDatabaseTotalReference.setValue(String.valueOf(mTotal));
                if (array.length != 0) {
                    addGiftsToOtherUsers(cost / 4, array);
                }
            }
        });
        // Clear input box

        mCostEditTextView.setText("");
        mExpenseEditTextView.setText("");
    }

    @SuppressLint("StaticFieldLeak")
    public class CheckingActiveNetwork extends AsyncTask<Void, Void, Boolean> {

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
            if (status) {
                Toast.makeText(MainActivity.this, "You are connected", Toast.LENGTH_SHORT).show();
            }
            if (!status) {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                Toast.makeText(MainActivity.this, "You are not connected!!", Toast.LENGTH_SHORT).show();
                mNetworkStatus.setVisibility(View.VISIBLE);
            }
        }
    }

}