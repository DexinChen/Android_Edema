package com.example.dexin.myapplication;

import android.content.Context;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.regions.Regions;

/**
 * Created by Dexin on 2/17/2018.
 */

public class CognitoHelper {
    private static CognitoUserPool userPool;
    private static CognitoHelper helper;
    private static final String userPoolId = "us-east-1_APms0VppS";
    private static final String clientId = "4637b54d68ib5o1sq5jkcaqm56";
    private static final String clientSecret = "1fk0mkpd10b2jacj7ciludi8rsa0nvmrlhfkh50mcav6809ajo82";
    private static final Regions cognitoRegion = Regions.US_EAST_1;
    public static void init(Context context) {
        if (helper != null && userPool != null) {
            return;
        }
        if (helper == null) {
            helper = new CognitoHelper();
        }
        if (userPool == null) {
            // Create a user pool with default ClientConfiguration
            userPool = new CognitoUserPool(context, userPoolId, clientId, clientSecret, cognitoRegion);
        }
    }
    public static CognitoUserPool getPool() {
        return userPool;
    }
}
