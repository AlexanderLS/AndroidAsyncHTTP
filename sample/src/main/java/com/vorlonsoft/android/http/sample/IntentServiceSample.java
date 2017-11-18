/*
 * Copyright 2017 Vorlonsoft LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vorlonsoft.android.http.sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.vorlonsoft.android.http.AsyncHttpClient;
import com.vorlonsoft.android.http.RequestHandle;
import com.vorlonsoft.android.http.ResponseHandlerInterface;
import com.vorlonsoft.android.http.sample.services.ExampleIntentService;
import com.vorlonsoft.android.http.sample.util.IntentUtil;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;

public class IntentServiceSample extends SampleParentActivity {

    public static final String LOG_TAG = "IntentServiceSample";
    public static final String ACTION_START = "SYNC_START";
    public static final String ACTION_RETRY = "SYNC_RETRY";
    public static final String ACTION_CANCEL = "SYNC_CANCEL";
    public static final String ACTION_SUCCESS = "SYNC_SUCCESS";
    public static final String ACTION_FAILURE = "SYNC_FAILURE";
    public static final String ACTION_FINISH = "SYNC_FINISH";
    public static final String[] ALLOWED_ACTIONS = {ACTION_START,
            ACTION_RETRY, ACTION_CANCEL, ACTION_SUCCESS, ACTION_FAILURE, ACTION_FINISH};
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // switch() doesn't support strings in older JDK.
            if (ACTION_START.equals(action)) {
                clearOutputs();
                addView(getColoredView(LIGHTBLUE, "Request started"));
            } else if (ACTION_FINISH.equals(action)) {
                addView(getColoredView(LIGHTBLUE, "Request finished"));
            } else if (ACTION_CANCEL.equals(action)) {
                addView(getColoredView(LIGHTBLUE, "Request cancelled"));
            } else if (ACTION_RETRY.equals(action)) {
                addView(getColoredView(LIGHTBLUE, "Request retried"));
            } else if (ACTION_FAILURE.equals(action) || ACTION_SUCCESS.equals(action)) {
                debugThrowable(LOG_TAG, (Throwable) intent.getSerializableExtra(ExampleIntentService.INTENT_THROWABLE));
                if (ACTION_SUCCESS.equals(action)) {
                    debugStatusCode(LOG_TAG, intent.getIntExtra(ExampleIntentService.INTENT_STATUS_CODE, 0));
                    debugHeaders(LOG_TAG, IntentUtil.deserializeHeaders(intent.getStringArrayExtra(ExampleIntentService.INTENT_HEADERS)));
                    byte[] returnedBytes = intent.getByteArrayExtra(ExampleIntentService.INTENT_DATA);
                    if (returnedBytes != null) {
                        debugResponse(LOG_TAG, new String(returnedBytes));
                    }
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter iFilter = new IntentFilter();
        for (String action : ALLOWED_ACTIONS) {
            iFilter.addAction(action);
        }
        registerReceiver(broadcastReceiver, iFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public ResponseHandlerInterface getResponseHandler() {
        // no response handler on activity
        return null;
    }

    @Override
    public String getDefaultURL() {
        return "https://httpbin.org/get";
    }

    @Override
    public boolean isRequestHeadersAllowed() {
        return false;
    }

    @Override
    public boolean isRequestBodyAllowed() {
        return false;
    }

    @Override
    public int getSampleTitle() {
        return R.string.title_intent_service_sample;
    }

    @Override
    public RequestHandle executeSample(AsyncHttpClient client, String URL, Header[] headers, HttpEntity entity, ResponseHandlerInterface responseHandler) {
        Intent serviceCall = new Intent(this, ExampleIntentService.class);
        serviceCall.putExtra(ExampleIntentService.INTENT_URL, URL);
        startService(serviceCall);
        return null;
    }
}
