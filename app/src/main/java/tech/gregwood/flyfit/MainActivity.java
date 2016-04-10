/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tech.gregwood.flyfit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.flybits.core.api.Flybits;
import com.flybits.core.api.FlybitsOptions;
import com.flybits.core.api.context.FlybitsContext;
import com.flybits.core.api.context.contracts.ContextContract;
import com.flybits.core.api.context.plugins.AvailablePlugins;
import com.flybits.core.api.exceptions.FeatureNotSupportedException;
import com.flybits.core.api.interfaces.IRequestCallback;
import com.flybits.core.api.interfaces.IRequestGeneralCallback;
import com.flybits.core.api.interfaces.IRequestLoggedIn;
import com.flybits.core.api.interfaces.IRequestPaginationCallback;
import com.flybits.core.api.models.Pagination;
import com.flybits.core.api.models.User;
import com.flybits.core.api.models.Zone;
import com.flybits.core.api.models.ZoneMoment;
import com.flybits.core.api.utils.filters.LoginOptions;
import com.flybits.core.api.utils.filters.ZoneOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;

import gcm.play.android.samples.com.gcmquickstart.R;
import tech.gregwood.flyfit.gcm.QuickstartPreferences;
import tech.gregwood.flyfit.gcm.RegistrationIntentService;
import tech.gregwood.flyfit.models.Root;

public class MainActivity extends AppCompatActivity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "FLYFIT_TAG";

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ProgressBar mRegistrationProgressBar;
    private TextView mInformationTextView;
    private boolean isReceiverRegistered;
    public static String zoneName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //GCM sample stuff

//        mRegistrationProgressBar = (ProgressBar) findViewById(R.id.registrationProgressBar);
//        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
//                SharedPreferences sharedPreferences =
//                        PreferenceManager.getDefaultSharedPreferences(context);
//                boolean sentToken = sharedPreferences
//                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
//                if (sentToken) {
//                    mInformationTextView.setText(getString(R.string.gcm_send_message));
//                } else {
//                    mInformationTextView.setText(getString(R.string.token_error_message));
//                }
//            }
//        };
//        mInformationTextView = (TextView) findViewById(R.id.informationTextView);
//
//        // Registering BroadcastReceiver
//        registerReceiver();
//
//        if (checkPlayServices()) {
//            // Start IntentService to register this application with GCM.
//            Intent intent = new Intent(this, RegistrationIntentService.class);
//            startService(intent);
//        }
//
//
//        loginUser();
//        zoneOptions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isReceiverRegistered = false;
        super.onPause();
    }

    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void loginUser() {

        LoginOptions logoptions = new LoginOptions.Builder(this)
                .loginViaEmail("gregoryadamwood@gmail.com", "@l^qh!R6vFI&%0xErrWaf$bq")
                .setDeviceOSVersion() //Optional
                .setRememberMeToken() //Optional
                .build();

        if (!Flybits.include(this).hasLoginSessionSaved()) {
            Flybits.include(this).login(logoptions, new IRequestCallback<User>() {
                @Override
                public void onSuccess(User data) {
                    //Login Successful
                    Log.d(TAG, data.email + " logged in.");
                }

                @Override
                public void onFailed(String reason) {
                    //Unsuccessful Login Make UI Action
                    Log.d(TAG, "Login failed.");
                }

                @Override
                public void onException(Exception exception) {
                    //Unsuccessful Login Make UI Action
                }

                @Override
                public void onCompleted() {
                    //Clean up method
                }
            });
        }
    }

    private void zoneOptions() {
        ZoneOptions options = new ZoneOptions.Builder().build();
        Flybits.include(this).getZones(options, new IRequestPaginationCallback<ArrayList<Zone>>() {

            @Override
            public void onSuccess(ArrayList<Zone> zones, Pagination pagination) {
                for (Zone zone : zones) {
                    Log.d(TAG, "Zone: " + zone.getName());
                    zoneName = zoneName + "\n" + zone.getName();

                    Flybits.include(MainActivity.this).getZoneMomentsForZone(zone.id, new IRequestPaginationCallback<ArrayList<ZoneMoment>>() {
                        @Override
                        public void onSuccess(ArrayList<ZoneMoment> zoneMoments, Pagination pagination) {
                            for (ZoneMoment zm : zoneMoments) {
                                zoneName = zoneName + "\n" + zm.getName();

                                if (zm.getName().equalsIgnoreCase("Website")) {
                                    final ZoneMoment webMoment = zm;
                                    Flybits.include(MainActivity.this).authenticateZoneMomentUsingJWT(webMoment, new IRequestGeneralCallback() {
                                        @Override
                                        public void onSuccess() {
                                            String url = webMoment.launchURL + "/WebsiteBits";
                                            String momentEndpoint = "/WebsiteBits";
                                            Flybits.include(MainActivity.this).getMomentData(webMoment.launchURL + momentEndpoint, null, Root.class, new IRequestCallback<Root>() {
                                                @Override
                                                public void onSuccess(Root data) {
                                                    //ZoneMoment Data stored under the Root Object
                                                    Log.d(TAG, "Key: " + data.localizedKeyValuePairs.Key + "\nValue: " + data.localizedKeyValuePairs.Value);
                                                }

                                                @Override
                                                public void onException(Exception exception) {
                                                    Log.e(TAG, "ZoneMoment exception: " + exception.getMessage());
                                                }
                                                @Override
                                                public void onFailed(String reason) {
                                                    Log.e(TAG, "ZoneMoment failed. Reason: " + reason);
                                                }

                                                @Override
                                                public void onCompleted() {
                                                    Log.d(TAG, "ZoneMoment completed.");
                                                }
                                            });

                                        }

                                        @Override
                                        public void onException(Exception e) {
                                            Log.e(TAG, "ZoneMoment exception: " + e.getMessage());
                                        }

                                        @Override
                                        public void onFailed(String s) {
                                            Log.d(TAG, "ZoneMoment failure.");
                                        }

                                        @Override
                                        public void onCompleted() {
                                            Log.d(TAG, "ZoneMoment complete.");
                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onException(Exception e) {

                        }

                        @Override
                        public void onFailed(String s) {

                        }

                        @Override
                        public void onCompleted() {

                        }
                    });
                }
            }

            @Override
            public void onException(Exception e) {


            }

            @Override
            public void onFailed(String s) {

            }

            @Override
            public void onCompleted() {

            }

        });
    }

    public void launchAboutMe(View view) {
        Intent intent = new Intent(MainActivity.this, AboutMe.class);

        startActivity(intent);
    }
}
