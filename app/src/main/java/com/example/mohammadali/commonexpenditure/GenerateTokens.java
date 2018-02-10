package com.example.mohammadali.commonexpenditure;

import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by mohammadali on 9/24/17.
 */

public class GenerateTokens extends FirebaseInstanceIdService{

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        sendRefershedTokenToServer(refreshedToken);
    }

    private void sendRefershedTokenToServer(String token){
        MainActivity.mUserTokenReference.setValue(token);
    }
}
