package com.google.android.exoplayer2.ext.idea;

import android.util.SparseArray;

import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.ParsableByteArray;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by QXTX-GOSPELL on 2018/10/22 0019.
 */

public class PsshParser {
    private static final boolean CAN_DEBUG = true;
    private static final int PSSH_DONE = 0;//done
    private static final int PSSH_KEY_IDS = 18;//2
    private static final int PSSH_PROVIDER = 26;//3
    private static final int PSSH_CONTENT_ID = 34;//4
    private static final int PSSH_POLICY = 50;//6
    private static final int PSSH_CRYPTO_PERIOD_INDEX = 56;//7
    private static final int PSSH_PROTECTION_SCHEME = 72;//9
    private static final int PSSH_CRYPTO_PERIOD_SECONDS = Integer.MAX_VALUE;//10
    private static final int PSSH_SUB_LICENSES = 66;//11
    private static final int PSSH_GROUP_IDS = Integer.MAX_VALUE;//12
    private static final int PSSH_ENTITLED_KEYS = Integer.MAX_VALUE;//13
    private static final int PSSH_KEY_SEQUENCE = Integer.MAX_VALUE;//14
    
//          //pssh-parser test
//        byte[] data = new byte[] {
//                18, 16, 102, 121, -13, 107, 55, -127, -128, 93,
//                54, -36, 56, 59, -95, 88, -55, 44, 26, 13,
//                119, 105, 100, 101, 118, 105, 110, 101, 95, 116,
//                101, 115, 116, 34, 8, 83, -121, 18, 18, -6,
//                66, 83, 32};

    public static PsshBean parsePsshAtom(byte[] psshAtom) {
        ParsableByteArray buffer = new ParsableByteArray(psshAtom);

        long size = buffer.readUnsignedInt(); // atom size
        Assertions.checkState(size == buffer.limit(), "The size is not equal buffer's limit!");
        buffer.skipBytes(4); // atom type

        int ver = buffer.readUnsignedByte(); // version
        buffer.skipBytes(19); // flags + SystemID

        if (ver > 0) {
            long kid_count = buffer.readUnsignedInt();
            buffer.skipBytes(16 * (int)kid_count);
        }

        long dataSize = buffer.readUnsignedInt();
        Assertions.checkState(dataSize == (buffer.limit() - buffer.getPosition()), "The dataSize is not equal (buffer.limit()-buffer.getPosition())!");

        ParsableByteArray psshData = new ParsableByteArray(Arrays.copyOfRange(buffer.data, buffer.getPosition(), buffer.getPosition() + (int)dataSize));

        //遍历psshData
        return parsePsshData(psshData);
    }

    private static PsshBean parsePsshData(ParsableByteArray psshData) {
        //遍历psshData
        PsshBean psshBean = new PsshBean();
        while (psshData.getPosition() < psshData.limit()) {
            if (!parseTag(psshBean, psshData)) {
                break;
            }
        }

        return psshBean;
    }

    private static boolean parseTag(PsshBean psshBean, ParsableByteArray psshData) {
        //需要处理负值
        int tag = readRawVarint32(psshData);

        int size = 0;
        if (psshData.getPosition() < psshData.limit()) {
            size = readRawVarint32(psshData);
        }

        switch (tag) {
            case PSSH_DONE:
                return false;
            case PSSH_KEY_IDS: //repeated bytes key_ids = 2    bytes(16)
                psshBean.setKids(readByteArrays(psshData, size, 16));
                break;
            case PSSH_PROVIDER: //optional string provider = 3
                psshBean.setProvider(psshData.readString(size));
                break;
            case PSSH_CONTENT_ID: //optional bytes content_id = 4
                psshBean.setContentId(readByteArray(psshData, size));
                break;
            case PSSH_CRYPTO_PERIOD_INDEX: //optional unint32 crypto_period_index = 7
                psshBean.setCrypto_period_index(size);
                break;
            case PSSH_PROTECTION_SCHEME: //optional uint32 protection_scheme = 9 [default = 0]
                psshBean.setProtection_scheme(size);
                break;
            case PSSH_SUB_LICENSES: //repeated SubLicense sub_licenses = 11
                psshBean.setSub_license(readByteArray(psshData, size));
                break;
            default:
                if (psshData.getPosition() + size >= psshData.limit()) {
                    return false;
                }
                psshData.skipBytes(size);
                break;
        }

        return true;
    }

    private static SparseArray<byte[]> readByteArrays(ParsableByteArray psshData, int size, int unitSize) {
        SparseArray<byte[]> bytes = new SparseArray<>();
        byte[] subByte = new byte[unitSize];
        for (int i=0,sequenceNum=0; i<size && i<psshData.limit(); i+=unitSize,sequenceNum++) {
            psshData.readBytes(subByte, 0, subByte.length);
            bytes.append(sequenceNum, subByte);
        }

        return bytes;
    }

    private static ParsableByteArray readByteArray(ParsableByteArray psshData, int size) {
        byte[] bytes = new byte[size];
        psshData.readBytes(bytes, 0, bytes.length);
        return new ParsableByteArray(bytes);
    }

    private static int readRawVarint32(ParsableByteArray psshData) {
        int limit = psshData.limit();
        // See implementation notes for readRawVarint64
        fastpath:
        {
            int tempPos = psshData.getPosition();

            if (limit == tempPos) {
                break fastpath;
            }

            final byte[] buffer = psshData.data;
            int x;
            if ((x = buffer[tempPos++]) >= 0) {
                psshData.setPosition(tempPos);
                return x;
            } else if (limit - tempPos < 9) {
                break fastpath;
            } else if ((x ^= (buffer[tempPos++] << 7)) < 0) {
                x ^= (~0 << 7);
            } else if ((x ^= (buffer[tempPos++] << 14)) >= 0) {
                x ^= (~0 << 7) ^ (~0 << 14);
            } else if ((x ^= (buffer[tempPos++] << 21)) < 0) {
                x ^= (~0 << 7) ^ (~0 << 14) ^ (~0 << 21);
            } else {
                int y = buffer[tempPos++];
                x ^= y << 28;
                x ^= (~0 << 7) ^ (~0 << 14) ^ (~0 << 21) ^ (~0 << 28);
                if (y < 0
                        && buffer[tempPos++] < 0
                        && buffer[tempPos++] < 0
                        && buffer[tempPos++] < 0
                        && buffer[tempPos++] < 0
                        && buffer[tempPos++] < 0) {
                    break fastpath; // Will throw malformedVarint()
                }
            }
            psshData.setPosition(tempPos);
            return x;
        }

        int value = 0;
        try {
            value = (int)readRawVarint64SlowPath(psshData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return value;
    }

    private static long readRawVarint64SlowPath(ParsableByteArray psshData) throws IOException {
        long result = 0L;

        for(int shift = 0; shift < 64; shift += 7) {
            byte b = psshData.data[psshData.getPosition()];
            psshData.skipBytes(1);
            result |= (long)(b & 0x7F) << shift;
            if((b & 0x80) == 0) {
                return result;
            }
        }

        throw new IOException("readRawVarint64SlowPath error.");
    }
}
