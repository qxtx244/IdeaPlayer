package com.google.android.exoplayer2.ext.idea;

import android.util.SparseArray;

import com.google.android.exoplayer2.util.ParsableByteArray;

/**
 * Created by QXTX-GOSPELL on 2018/10/19 0019.
 * psshData
 */
public class PsshBean {
    private SparseArray<byte[]> kids; //[base64_string][16]
    private String provider;
    private ParsableByteArray contentId; //base64_string int[8]

    private int protection_scheme;
    private int crypto_period_index;
    private int crypto_period_seconds;
    private int key_sequence;
    private ParsableByteArray sub_license;
    private SparseArray<WrappedKey> entitled_keys;

    public class WrappedKey {
        private ParsableByteArray key_id;
        private ParsableByteArray wrapping_key_id;
        private ParsableByteArray wrapping_iv;
        private ParsableByteArray wrapped_key;

        public WrappedKey() {}

        public WrappedKey(ParsableByteArray key_id, ParsableByteArray wrapping_key_id, ParsableByteArray wrapping_iv, ParsableByteArray wrapped_key) {
            this.key_id = key_id;
            this.wrapping_key_id = wrapping_key_id;
            this.wrapping_iv = wrapping_iv;
            this.wrapped_key = wrapped_key;
        }

        public ParsableByteArray getKey_id() {
            return key_id;
        }

        public void setKey_id(ParsableByteArray key_id) {
            this.key_id = key_id;
        }

        public ParsableByteArray getWrapping_key_id() {
            return wrapping_key_id;
        }

        public void setWrapping_key_id(ParsableByteArray wrapping_key_id) {
            this.wrapping_key_id = wrapping_key_id;
        }

        public ParsableByteArray getWrapping_iv() {
            return wrapping_iv;
        }

        public void setWrapping_iv(ParsableByteArray wrapping_iv) {
            this.wrapping_iv = wrapping_iv;
        }

        public ParsableByteArray getWrapped_key() {
            return wrapped_key;
        }

        public void setWrapped_key(ParsableByteArray wrapped_key) {
            this.wrapped_key = wrapped_key;
        }
    }

    public SparseArray<byte[]> getKids() {
        return kids;
    }

    public void setKids(SparseArray<byte[]> kids) {
        this.kids = kids;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public ParsableByteArray getContentId() {
        return contentId;
    }

    public void setContentId(ParsableByteArray contentId) {
        this.contentId = contentId;
    }

    public int getProtection_scheme() {
        return protection_scheme;
    }

    public void setProtection_scheme(int protection_scheme) {
        this.protection_scheme = protection_scheme;
    }

    public int getCrypto_period_index() {
        return crypto_period_index;
    }

    public void setCrypto_period_index(int crypto_period_index) {
        this.crypto_period_index = crypto_period_index;
    }

    public int getCrypto_period_seconds() {
        return crypto_period_seconds;
    }

    public void setCrypto_period_seconds(int crypto_period_seconds) {
        this.crypto_period_seconds = crypto_period_seconds;
    }

    public int getKey_sequence() {
        return key_sequence;
    }

    public void setKey_sequence(int key_sequence) {
        this.key_sequence = key_sequence;
    }

    public ParsableByteArray getSub_license() {
        return sub_license;
    }

    public void setSub_license(ParsableByteArray sub_license) {
        this.sub_license = sub_license;
    }

    public SparseArray<WrappedKey> getEntitled_keys() {
        return entitled_keys;
    }

    public void setEntitled_keys(SparseArray<WrappedKey> entitled_keys) {
        this.entitled_keys = entitled_keys;
    }
}
