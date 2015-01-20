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

package org.onepf.opfiab.model.event.response;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.onepf.opfiab.model.billing.SkusDetails;
import org.onepf.opfiab.model.event.request.SkuDetailsRequest;

public class SkuDetailsResponse extends Response {

    @Nullable
    private final SkusDetails skusDetails;

    public SkuDetailsResponse(@NonNull final SkuDetailsRequest request,
                              @NonNull final Status status,
                              @Nullable final SkusDetails skusDetails) {
        super(request, status);
        this.skusDetails = skusDetails;
    }

    @Nullable
    public SkusDetails getSkusDetails() {
        return skusDetails;
    }

    @NonNull
    @Override
    public SkuDetailsRequest getRequest() {
        return (SkuDetailsRequest) super.getRequest();
    }
}
