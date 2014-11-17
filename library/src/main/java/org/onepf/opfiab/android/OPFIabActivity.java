/*
 * Copyright 2012-2014 One Platform Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onepf.opfiab.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.ManagedOPFIabHelper;
import org.onepf.opfiab.OPFIab;

public class OPFIabActivity extends Activity {

    @NonNull
    public static Intent newIntent(@NonNull final Context context) {
        final Intent intent = new Intent(context.getApplicationContext(), OPFIabActivity.class);
        return intent;
    }


    @NonNull
    private final ManagedOPFIabHelper opfIabHelper = OPFIab.getManagedInstance();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        opfIabHelper.onCreate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        opfIabHelper.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        opfIabHelper.onPause();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    @Nullable final Intent data) {
        opfIabHelper.onActivityResult(requestCode, resultCode, data);
    }
}
