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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.onepf.opfiab.model.billing.ConsumableDetails;
import org.onepf.opfiab.model.billing.EntitlementDetails;
import org.onepf.opfiab.model.billing.Inventory;
import org.onepf.opfiab.model.billing.Purchase;
import org.onepf.opfiab.model.billing.SkuDetails;
import org.onepf.opfiab.model.billing.SkusDetails;
import org.onepf.opfiab.model.billing.SubscriptionDetails;
import org.onepf.opfiab.model.event.ActivityResultEvent;
import org.onepf.opfiab.model.event.BillingEvent;
import org.onepf.opfiab.model.event.request.ConsumeRequest;
import org.onepf.opfiab.model.event.request.InventoryRequest;
import org.onepf.opfiab.model.event.request.PurchaseRequest;
import org.onepf.opfiab.model.event.request.Request;
import org.onepf.opfiab.model.event.request.SkuDetailsRequest;
import org.onepf.opfiab.model.event.response.ConsumeResponse;
import org.onepf.opfiab.model.event.response.InventoryResponse;
import org.onepf.opfiab.model.event.response.PurchaseResponse;
import org.onepf.opfiab.model.event.response.Response;
import org.onepf.opfiab.model.event.response.SkuDetailsResponse;
import org.onepf.opfiab.sku.SkuResolver;
import org.onepf.opfiab.verification.PurchaseVerifier;
import org.onepf.opfutils.OPFUtils;

import de.greenrobot.event.EventBus;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public abstract class BaseBillingProvider implements BillingProvider {

    @NonNull
    protected final Context context = OPFIab.getContext();

    @NonNull
    protected final PurchaseVerifier purchaseVerifier;

    @NonNull
    protected final SkuResolver skuResolver;


    @NonNull
    private final EventBus eventBus = OPFIab.getEventBus();

    @NonNull
    private final String name = getName();

    @Nullable
    private final String packageName = getPackageName();

    private Request pendingRequest;

    protected BaseBillingProvider(
            @NonNull final PurchaseVerifier purchaseVerifier,
            @NonNull final SkuResolver skuResolver) {
        this.purchaseVerifier = purchaseVerifier;
        this.skuResolver = skuResolver;
    }

    public final void onEventAsync(@NonNull final Request event) {
        handleRequest(event);
    }

    public final void onEvent(@NonNull final ActivityResultEvent event) {
        final Activity activity = event.getActivity();
        final int requestCode = event.getRequestCode();
        final int resultCode = event.getResultCode();
        final Intent data = event.getData();
        onActivityResult(activity, requestCode, resultCode, data);
    }

    @SuppressFBWarnings({"BC_UNCONFIRMED_CAST", "DLS_DEAD_LOCAL_STORE"})
    @SuppressWarnings("ConstantConditions")
    protected void handleRequest(@NonNull final Request request) {
        switch (request.getType()) {
            case CONSUME:
                final ConsumeRequest consumeRequest = (ConsumeRequest) request;
                consume(consumeRequest.getConsumableDetails());
                break;
            case PURCHASE:
                final PurchaseRequest purchaseRequest = (PurchaseRequest) request;
                final Activity activity = purchaseRequest.getActivity();
                final SkuDetails skuDetails = purchaseRequest.getSkuDetails();
                purchase(activity, skuDetails);
            case SKU_DETAILS:
                final SkuDetailsRequest skuDetailsRequest = (SkuDetailsRequest) request;
                skuDetails(skuDetailsRequest.getSkus());
                break;
            case INVENTORY:
                @SuppressWarnings("UnusedDeclaration")
                final InventoryRequest inventoryRequest = (InventoryRequest) request;
                inventory();
                break;
            default:
                return;
        }
        pendingRequest = request;
    }

    protected void postResponse(@NonNull final Response response) {
        eventBus.post(response);
        pendingRequest = null;
    }

    protected void postResponse(@NonNull final Response.Status status) {
        final Response response;
        final BillingEvent.Type type = pendingRequest.getType();
        switch (type) {
            case CONSUME:
                final ConsumeRequest consumeRequest = (ConsumeRequest) pendingRequest;
                response = new ConsumeResponse(consumeRequest, status, null);
                break;
            case PURCHASE:
                final PurchaseRequest purchaseRequest = (PurchaseRequest) pendingRequest;
                response = new PurchaseResponse(purchaseRequest, status, null);
                break;
            case SKU_DETAILS:
                final SkuDetailsRequest skuDetailsRequest = (SkuDetailsRequest) pendingRequest;
                response = new SkuDetailsResponse(skuDetailsRequest, status, null);
                break;
            case INVENTORY:
                final InventoryRequest inventoryRequest = (InventoryRequest) pendingRequest;
                response = new InventoryResponse(inventoryRequest, status, null);
                break;
            default:
                throw new IllegalStateException("Unknown request type: " + type);
        }
        postResponse(response);
    }

    protected void postResponse(@NonNull final Response.Status status,
                                @NonNull final SkusDetails skusDetails) {
        final SkuDetailsRequest skuDetailsRequest = (SkuDetailsRequest) pendingRequest;
        postResponse(new SkuDetailsResponse(skuDetailsRequest, status, skusDetails));
    }

    protected void postResponse(@NonNull final Response.Status status,
                                @NonNull final Inventory inventory) {
        final InventoryRequest inventoryRequest = (InventoryRequest) pendingRequest;
        postResponse(new InventoryResponse(inventoryRequest, status, inventory));
    }

    protected void postResponse(@NonNull final Response.Status status,
                                @NonNull final Purchase purchase) {
        final PurchaseRequest purchaseRequest = (PurchaseRequest) pendingRequest;
        postResponse(new PurchaseResponse(purchaseRequest, status, purchase));
    }

    protected void postResponse(@NonNull final Response.Status status,
                                @NonNull final ConsumableDetails consumableDetails) {
        final ConsumeRequest consumeRequest = (ConsumeRequest) pendingRequest;
        postResponse(new ConsumeResponse(consumeRequest, status, consumableDetails));
    }

    @SuppressFBWarnings("BC_UNCONFIRMED_CAST")
    @Override
    public void purchase(@NonNull final Activity activity, @NonNull final SkuDetails skuDetails) {
        switch (skuDetails.getType()) {
            case CONSUMABLE:
                purchase(activity, (ConsumableDetails) skuDetails);
                break;
            case ENTITLEMENT:
                purchase(activity, (EntitlementDetails) skuDetails);
                break;
            case SUBSCRIPTION:
                purchase(activity, (SubscriptionDetails) skuDetails);
                break;
        }
    }

    public void purchase(@NonNull final Activity activity,
                         @NonNull final ConsumableDetails skuDetails) { }


    public void purchase(@NonNull final Activity activity,
                         @NonNull final EntitlementDetails skuDetails) { }

    public void purchase(@NonNull final Activity activity,
                         @NonNull final SubscriptionDetails skuDetails) { }

    @Override
    public void onActivityResult(@NonNull final Activity activity, final int requestCode,
                                 final int resultCode, @Nullable final Intent data) { }

    @Override
    public boolean isAvailable() {
        if (TextUtils.isEmpty(packageName)) {
            throw new UnsupportedOperationException(
                    "You must override this method for packageless Billing Providers.");
        }
        return OPFUtils.isInstalled(context, packageName);
    }

    @Nullable
    @Override
    public Intent getStorePageIntent() {
        return null;
    }

    @Nullable
    @Override
    public Intent getRateItIntent() {
        return null;
    }

    //CHECKSTYLE:OFF
    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseBillingProvider)) return false;

        final BaseBillingProvider that = (BaseBillingProvider) o;

        if (!name.equals(that.name)) return false;
        if (packageName != null ? !packageName.equals(that.packageName) : that.packageName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (packageName != null ? packageName.hashCode() : 0);
        return result;
    }
    //CHECKSTYLE:ON

    public abstract static class Builder {

        @NonNull
        protected PurchaseVerifier purchaseVerifier = PurchaseVerifier.STUB;

        @NonNull
        protected SkuResolver skuResolver = SkuResolver.STUB;

        protected Builder purchaseVerifier(@NonNull final PurchaseVerifier purchaseVerifier) {
            this.purchaseVerifier = purchaseVerifier;
            return this;
        }

        protected Builder skuResolver(@NonNull final SkuResolver skuResolver) {
            this.skuResolver = skuResolver;
            return this;
        }

        public abstract BaseBillingProvider build();
    }
}