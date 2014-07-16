/*
 * Copyright (C) 2014 Sebastian Kaspari
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.androidzeitgeist.dashwatch.common;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Partially inspired from http://stackoverflow.com/questions/4895523/java-string-to-sha1
 */
public class Hash {
    private static final String HEXES = "0123456789ABCDEF";

    public static String sha1(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            return toHexadecimal(md.digest(input.getBytes("UTF-8")));
        } catch (NoSuchAlgorithmException e) {
            AssertionError error = new AssertionError("This device does not know what SHA1 is");
            error.initCause(e);
            throw error;
        } catch (UnsupportedEncodingException e) {
            AssertionError error = new AssertionError("This device has never heard of UTF-8");
            error.initCause(e);
            throw error;
        }
    }

    private static String toHexadecimal(byte [] raw) {
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }
}
