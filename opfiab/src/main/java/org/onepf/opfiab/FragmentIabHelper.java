/*
 * Copyright 2012-2014 One Platform Foundation
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

package org.onepf.opfiab;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.android.OPFIabFragment;
import org.onepf.opfiab.android.OPFIabSupportFragment;
import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.opfiab.model.event.FragmentLifecycleEvent;
import org.onepf.opfiab.model.event.SupportFragmentLifecycleEvent;

import static org.onepf.opfiab.OPFIab.FRAGMENT_TAG;

public class FragmentIabHelper extends SelfManagedIabHelper {

    @NonNull
    private final Object eventHandler = new Object() {

        public void onEvent(@NonNull final FragmentLifecycleEvent event) {
            if (opfFragment != event.getFragment()) {
                return;
            }
            switch (event.getType()) {
                case ATTACH:
                    managedIabHelper.subscribe();
                    break;
                case DETACH:
                    managedIabHelper.unsubscribe();
                    dispose();
                    break;
            }
        }

        public void onEvent(@NonNull final SupportFragmentLifecycleEvent event) {
            if (opfFragment != event.getFragment()) {
                return;
            }
            switch (event.getType()) {
                case ATTACH:
                    managedIabHelper.subscribe();
                    break;
                case DETACH:
                    managedIabHelper.unsubscribe();
                    dispose();
                    break;
            }
        }
    };

    @Nullable
    private final android.app.Fragment fragment;

    @Nullable
    private final android.support.v4.app.Fragment supportFragment;

    @NonNull
    private final ManagedIabHelper managedIabHelper;

    @NonNull
    private final Object opfFragment;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    FragmentIabHelper(@NonNull final ManagedIabHelper managedIabHelper,
                      @NonNull final android.app.Fragment fragment) {
        super(managedIabHelper);
        this.managedIabHelper = managedIabHelper;
        this.fragment = fragment;
        this.supportFragment = null;

        android.app.Fragment opfFragment;
        final android.app.FragmentManager fragmentManager = fragment.getChildFragmentManager();
        if ((opfFragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG)) == null) {
            opfFragment = OPFIabFragment.newInstance();
            fragmentManager.executePendingTransactions();
            fragmentManager.beginTransaction()
                    .add(opfFragment, FRAGMENT_TAG)
                    .commit();
        }
        this.opfFragment = opfFragment;
    }

    FragmentIabHelper(@NonNull final ManagedIabHelper managedIabHelper,
                      @NonNull final android.support.v4.app.Fragment supportFragment) {
        super(managedIabHelper);
        this.managedIabHelper = managedIabHelper;
        this.fragment = null;
        this.supportFragment = supportFragment;

        android.support.v4.app.Fragment opfFragment;
        final android.support.v4.app.FragmentManager fragmentManager = supportFragment.getChildFragmentManager();
        if ((opfFragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG)) == null) {
            opfFragment = OPFIabSupportFragment.newInstance();
            fragmentManager.executePendingTransactions();
            fragmentManager.beginTransaction()
                    .add(opfFragment, FRAGMENT_TAG)
                    .commit();
        }
        this.opfFragment = opfFragment;
    }

    @Override
    public void purchase(@NonNull final SkuDetails skuDetails) {
        managedIabHelper.purchase(getActivity(), skuDetails);
    }

    protected void dispose() {
        eventBus.unregister(eventHandler);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @NonNull
    private Activity getActivity() {
        Activity activity = null;
        if (supportFragment != null) {
            activity = supportFragment.getActivity();
        } else if (fragment != null) {
            activity = fragment.getActivity();
        }
        if (activity == null) {
            throw new IllegalStateException("Fragment already detached!");
        }
        return activity;
    }
}