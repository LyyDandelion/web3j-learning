/*
 * Copyright 2019 Web3 Labs Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.web3j.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jcajce.provider.digest.Blake2b;
import org.bouncycastle.jcajce.provider.digest.Keccak;

import org.web3j.utils.Numeric;

/**
 *
 * Cryptographic hash functions.
 *
 * 加密hash类
 * */
public class Hash {

    /**
     * 私有
     */
    private Hash() {}

    /**
     * Generates a digest for the given {@code input}.
     * 输入参数生成摘要
     * @param input The input to digest 摘要数据
     * @param algorithm The hash algorithm to use 使用的hash算法
     * @return The hash value for the given input 返回摘要的hash值
     * @throws RuntimeException If we couldn't find any provider for the given algorithm 没找到该算法会抛错异常
     */
    public static byte[] hash(byte[] input, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm.toUpperCase());
            return digest.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Couldn't find a " + algorithm + " provider", e);
        }
    }

    /**
     * Keccak-256 hash function.
     *
     * Keccak-256 哈希函数
     * @param hexInput hex encoded input data with optional 0x prefix  输入 带 0x 前缀的16进制编码数据
     * @return hash value as hex encoded string 返回16进制的字符串
     */
    public static String sha3(String hexInput) {
        byte[] bytes = Numeric.hexStringToByteArray(hexInput);
        byte[] result = sha3(bytes);
        return Numeric.toHexString(result);
    }

    /**
     * Keccak-256 hash function.
     * Keccak-256 哈希函数
     * @param input binary encoded input data 输入二进制数据
     * @param offset of start of data 开始
     * @param length of data 长度
     * @return hash value hash值
     */
    public static byte[] sha3(byte[] input, int offset, int length) {
        Keccak.DigestKeccak kecc = new Keccak.Digest256();
        kecc.update(input, offset, length);
        return kecc.digest();
    }

    /**
     * Keccak-256 hash function.
     * Keccak-256 哈希函数
     * @param input binary encoded input data 输入二进制数据
     * @return hash value   hash值
     */
    public static byte[] sha3(byte[] input) {
        return sha3(input, 0, input.length);
    }

    /**
     * Keccak-256 hash function that operates on a UTF-8 encoded String.
     *Keccak-256 哈希函数，针对UTF-8 字符串处理
     *
     * 例如：deposit(uint256)  编码处理后得到 0xb6b55f256c3eb337f96418d59e773db6e805074f5e574a2bebb7d71394043619
     * 前4个字节就代表了这个函数 “b6b55f25”
     *
     * @param utf8String UTF-8 encoded string UTF-8编码字符串
     * @return hash value as hex encoded string hash值
     */
    public static String sha3String(String utf8String) {
        return Numeric.toHexString(sha3(utf8String.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Generates SHA-256 digest for the given {@code input}.
     * 给定的输入数据生成 SHA-256消息摘要
     * @param input The input to digest 输入的摘要
     * @return The hash value for the given input 给定输入的hash值
     * @throws RuntimeException If we couldn't find any SHA-256 provider
     */
    public static byte[] sha256(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Couldn't find a SHA-256 provider", e);
        }
    }

    public static byte[] hmacSha512(byte[] key, byte[] input) {
        HMac hMac = new HMac(new SHA512Digest());
        hMac.init(new KeyParameter(key));
        hMac.update(input, 0, input.length);
        byte[] out = new byte[64];
        hMac.doFinal(out, 0);
        return out;
    }

    public static byte[] sha256hash160(byte[] input) {
        byte[] sha256 = sha256(input);
        RIPEMD160Digest digest = new RIPEMD160Digest();
        digest.update(sha256, 0, sha256.length);
        byte[] out = new byte[20];
        digest.doFinal(out, 0);
        return out;
    }

    /**
     * Blake2-256 hash function.
     * Blake2-256 哈希函数
     * @param input binary encoded input data 输入二进制数据
     * @return hash value 返回hash值
     */
    public static byte[] blake2b256(byte[] input) {
        return new Blake2b.Blake2b256().digest(input);
    }

}
