/*
 * Copyright 2012-2015 One Platform Foundation
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

package org.onepf.opfiab.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.onepf.opfiab.model.JsonCompatible;
import org.onepf.opfutils.OPFLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static android.content.pm.PackageManager.GET_SIGNATURES;


/**
 * Collection of handy utility methods.
 * <p>
 * Intended for internal use.
 */
public final class OPFIabUtils {

    private static final int JSON_SPACES = 4;


    private OPFIabUtils() {
        throw new UnsupportedOperationException();
    }


    /**
     * Converts supplied object to human-readable JSON representation.
     *
     * @param jsonCompatible Object to convert.
     *
     * @return Human-readable string, can't be null.
     */
    @NonNull
    public static String toString(@NonNull final JsonCompatible jsonCompatible) {
        try {
            return jsonCompatible.toJson().toString(JSON_SPACES);
        } catch (JSONException exception) {
            OPFLog.e("", exception);
        }
        return "";
    }

    @NonNull
    public static String toString(@NonNull final InputStream inputStream) {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        final StringBuilder builder = new StringBuilder();
        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                builder.append(line);
            }
            return builder.toString();
        } catch (IOException exception) {
            OPFLog.e("", exception);
        }
        return "";
    }

    /**
     * Removes first element from supplied collection.
     *
     * @param collection Collection to remove element from.
     *
     * @return Removed object or null.
     */
    @Nullable
    public static <E> E poll(@NonNull final Collection<E> collection) {
        if (collection.isEmpty()) {
            return null;
        }
        final Iterator<E> iterator = collection.iterator();
        final E e = iterator.next();
        iterator.remove();
        return e;
    }

    @NonNull
    public static <T> List<List<T>> partition(@NonNull final Collection<T> collection,
                                              final int batch) {
        final int size = collection.size();
        final int batches = size / batch + size % batch == 0 ? 0 : 1;
        final List<List<T>> partitioned = new ArrayList<>(batches);
        final List<T> list = new ArrayList<>(collection);
        for (int i = 0; i < batches; i++) {
            final int start = batch * i;
            final int end = size > start + batch ? start + batch : size;
            partitioned.add(list.subList(start, end));
        }
        return partitioned;
    }

    @SuppressWarnings("PMD.LooseCoupling")
    @Nullable
    public static ArrayList<String> getList(@Nullable final Bundle bundle,
                                            @NonNull final String key) {
        if (bundle != null && bundle.containsKey(key)) {
            return bundle.getStringArrayList(key);
        }
        return null;
    }

    @SuppressWarnings("PMD.LooseCoupling")
    @NonNull
    public static Bundle putList(@NonNull final Bundle bundle,
                                 @Nullable final ArrayList<String> list,
                                 @NonNull final String key) {
        if (list != null) {
            bundle.putStringArrayList(key, list);
        }
        return bundle;
    }

    @SuppressWarnings("PMD.LooseCoupling")
    @NonNull
    public static Bundle addList(@NonNull final Bundle bundle,
                                 @Nullable final ArrayList<String> list,
                                 @NonNull final String key) {
        if (list != null) {
            final ArrayList<String> oldList = getList(bundle, key);
            final ArrayList<String> newList;
            if (oldList == null) {
                newList = list;
            } else {
                newList = new ArrayList<>(oldList);
                newList.addAll(list);
            }
            bundle.putStringArrayList(key, newList);
        }
        return bundle;
    }

    /**
     * Retrieves signature form supplied package.
     *
     * @param context     Context object to get {@link PackageManager} from.
     * @param packageName Package to retrieve signature for.
     *
     * @return Signature object if package found, null otherwise.
     */
    @SuppressWarnings("PackageManagerGetSignatures")
    @NonNull
    public static Signature[] getPackageSignatures(@NonNull final Context context,
                                                   @NonNull final String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        try {
            final PackageInfo info = packageManager.getPackageInfo(packageName, GET_SIGNATURES);
            final Signature[] signatures = info.signatures;
            if (signatures != null) {
                return signatures;
            }
        } catch (PackageManager.NameNotFoundException exception) {
            OPFLog.e("", exception);
        }
        return new Signature[0];
    }
}
