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

package org.onepf.opfiab.verification;

import android.support.annotation.NonNull;

import org.onepf.opfiab.model.billing.Purchase;

public interface PurchaseVerifier {

    @NonNull
    PurchaseVerifier STUB = new PurchaseVerifier() {

        @NonNull
        @Override
        public VerificationResult verify(@NonNull final Purchase purchase) {
            return VerificationResult.SUCCESS;
        }
    };

    @NonNull
    VerificationResult verify(@NonNull final Purchase purchase);
}
