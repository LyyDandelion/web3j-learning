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
package org.web3j.abi;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Array;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Bytes;
import org.web3j.abi.datatypes.BytesType;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.NumericType;
import org.web3j.abi.datatypes.StaticArray;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Ufixed;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.primitive.PrimitiveType;
import org.web3j.utils.Numeric;

import static org.web3j.abi.datatypes.Type.MAX_BIT_LENGTH;
import static org.web3j.abi.datatypes.Type.MAX_BYTE_LENGTH;

/**
 * 类型的以太坊合约应用程序二进制接口 (ABI) 编码。
 *
 * 详细信息 参考：https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI
 * Ethereum Contract Application Binary Interface (ABI) encoding for types. Further details are
 * available <a href="https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI">here</a>.
 */
public class TypeEncoder {
    //私有构造
    private TypeEncoder() {}

    //是否动态
    static boolean isDynamic(Type parameter) {
        //class.isAssignableFrom ：判断两个类的之间的关联关系，一个类是否可以被强制转换为另外一个实例对象
        return parameter instanceof DynamicBytes
                || parameter instanceof Utf8String
                || parameter instanceof DynamicArray
                || (parameter instanceof StaticArray
                        && DynamicStruct.class.isAssignableFrom(
                                ((StaticArray) parameter).getComponentType()));
    }

    /**
     * 编码
     */
    @SuppressWarnings("unchecked")
    public static String encode(Type parameter) {

        if (parameter instanceof NumericType) {//是否为常见数值类型
            return encodeNumeric(((NumericType) parameter));
        } else if (parameter instanceof Address) { //是否地址类型
            return encodeAddress((Address) parameter);
        } else if (parameter instanceof Bool) { //是否布尔类型
            return encodeBool((Bool) parameter);
        } else if (parameter instanceof Bytes) { //是否静态分配的字节序列
            return encodeBytes((Bytes) parameter);
        } else if (parameter instanceof DynamicBytes) { //是否动态分配的字节序列。
            return encodeDynamicBytes((DynamicBytes) parameter);
        } else if (parameter instanceof Utf8String) { //是否UTF-8 编码的字符串类型。
            return encodeString((Utf8String) parameter);
        } else if (parameter instanceof StaticArray) { //是否静态数组
            //class.isAssignableFrom ：判断两个类的之间的关联关系，一个类是否可以被强制转换为另外一个实例对象
            if (DynamicStruct.class.isAssignableFrom(
                    ((StaticArray) parameter).getComponentType())) {
                return encodeStaticArrayWithDynamicStruct((StaticArray) parameter);
            } else {
                return encodeArrayValues((StaticArray) parameter);
            }
        } else if (parameter instanceof DynamicStruct) { //是否动态结构
            return encodeDynamicStruct((DynamicStruct) parameter);
        } else if (parameter instanceof DynamicArray) { //是否动态数组
            return encodeDynamicArray((DynamicArray) parameter);
        } else if (parameter instanceof PrimitiveType) { //是否原始类型
            return encode(((PrimitiveType) parameter).toSolidityType());
        } else {
            //抛出不支持类型异常
            throw new UnsupportedOperationException(
                    "Type cannot be encoded: " + parameter.getClass());
        }
    }

    /**
     * Encodes a static array containing a dynamic struct type. In this case, the array items are
     * decoded as dynamic values and have their offsets at the beginning of the encoding. Example:
     * For the following static array containing three elements: <code>StaticArray3</code>
     * enc([struct1, struct2, struct2]) = offset(enc(struct1)) offset(enc(struct2))
     * offset(enc(struct3)) enc(struct1) enc(struct2) enc(struct3)
     *
     * @param
     * @return
     */
    private static <T extends Type> String encodeStaticArrayWithDynamicStruct(Array<T> value) {
        String valuesOffsets = encodeStructsArraysOffsets(value);
        String encodedValues = encodeArrayValues(value);

        StringBuilder result = new StringBuilder();
        result.append(valuesOffsets);
        result.append(encodedValues);
        return result.toString();
    }

    //编码地址
    static String encodeAddress(Address address) {
        //直接获取address的数值，进行数值类型编码
        return encodeNumeric(address.toUint());
    }

    //编码数值类型
    static String encodeNumeric(NumericType numericType) {
        byte[] rawValue = toByteArray(numericType);
        //填充数值
        byte paddingValue = getPaddingValue(numericType);
        byte[] paddedRawValue = new byte[MAX_BYTE_LENGTH];
        if (paddingValue != 0) {
            for (int i = 0; i < paddedRawValue.length; i++) {
                paddedRawValue[i] = paddingValue;
            }
        }

        //复制 及0填充 rawValue从0开始全部填充到paddedRawValue， 从MAX_BYTE_LENGTH - rawValue.length开始
        System.arraycopy(
                rawValue, 0, paddedRawValue, MAX_BYTE_LENGTH - rawValue.length, rawValue.length);
        //byte数组 转hex（无前缀 0x）
        return Numeric.toHexStringNoPrefix(paddedRawValue);
    }

    private static byte getPaddingValue(NumericType numericType) {
        //signum() 1：正 0：零 -1：负
        if (numericType.getValue().signum() == -1) {//是否为负数
            return (byte) 0xff;
        } else {
            return 0;
        }
    }

    private static byte[] toByteArray(NumericType numericType) {
        BigInteger value = numericType.getValue();
        if (numericType instanceof Ufixed || numericType instanceof Uint) {
            if (value.bitLength() == MAX_BIT_LENGTH) {
                // As BigInteger is signed, if we have a 256 bit value, the resultant byte array
                // will contain a sign byte in it's MSB, which we should ignore for this unsigned
                // integer type.
                byte[] byteArray = new byte[MAX_BYTE_LENGTH];
                System.arraycopy(value.toByteArray(), 1, byteArray, 0, MAX_BYTE_LENGTH);
                return byteArray;
            }
        }
        return value.toByteArray();
    }
    //编码bool类型
    static String encodeBool(Bool value) {
        byte[] rawValue = new byte[MAX_BYTE_LENGTH];
        //为true
        if (value.getValue()) {
            //最后一位填充1
            rawValue[rawValue.length - 1] = 1;
        }
        //byte数组 转hex（无前缀 0x）
        return Numeric.toHexStringNoPrefix(rawValue);
    }

    static String encodeBytes(BytesType bytesType) {
        byte[] value = bytesType.getValue();
        int length = value.length;
        int mod = length % MAX_BYTE_LENGTH;

        byte[] dest;
        if (mod != 0) {
            int padding = MAX_BYTE_LENGTH - mod;
            dest = new byte[length + padding];
            System.arraycopy(value, 0, dest, 0, length);
        } else {
            dest = value;
        }
        return Numeric.toHexStringNoPrefix(dest);
    }

    static String encodeDynamicBytes(DynamicBytes dynamicBytes) {
        int size = dynamicBytes.getValue().length;
        String encodedLength = encode(new Uint(BigInteger.valueOf(size)));
        String encodedValue = encodeBytes(dynamicBytes);

        StringBuilder result = new StringBuilder();
        result.append(encodedLength);
        result.append(encodedValue);
        return result.toString();
    }

    static String encodeString(Utf8String string) {
        byte[] utfEncoded = string.getValue().getBytes(StandardCharsets.UTF_8);
        return encodeDynamicBytes(new DynamicBytes(utfEncoded));
    }

    static <T extends Type> String encodeArrayValues(Array<T> value) {
        StringBuilder result = new StringBuilder();
        for (Type type : value.getValue()) {
            result.append(encode(type));
        }
        return result.toString();
    }

    static String encodeDynamicStruct(final DynamicStruct value) {
        String encodedValues = encodeDynamicStructValues(value);

        StringBuilder result = new StringBuilder();
        result.append(encodedValues);
        return result.toString();
    }

    private static String encodeDynamicStructValues(final DynamicStruct value) {
        int staticSize = 0;
        for (int i = 0; i < value.getValue().size(); ++i) {
            final Type type = value.getValue().get(i);
            if (isDynamic(type)) {
                staticSize += 32;
            } else {
                staticSize += type.bytes32PaddedLength();
            }
        }
        int dynamicOffset = staticSize;
        final List<String> offsetsAndStaticValues = new ArrayList<>();
        final List<String> dynamicValues = new ArrayList<>();
        for (int i = 0; i < value.getValue().size(); ++i) {
            final Type type = value.getValue().get(i);
            if (isDynamic(type)) {
                offsetsAndStaticValues.add(
                        Numeric.toHexStringNoPrefix(
                                Numeric.toBytesPadded(
                                        new BigInteger(Long.toString(dynamicOffset)),
                                        MAX_BYTE_LENGTH)));
                String encodedValue = encode(type);
                dynamicValues.add(encodedValue);
                dynamicOffset += encodedValue.length() >> 1;
            } else {
                offsetsAndStaticValues.add(encode(value.getValue().get(i)));
            }
        }
        final List<String> data = new ArrayList<>();
        data.addAll(offsetsAndStaticValues);
        data.addAll(dynamicValues);
        return String.join("", data);
    }

    static <T extends Type> String encodeDynamicArray(DynamicArray<T> value) {
        int size = value.getValue().size();
        String encodedLength = encode(new Uint(BigInteger.valueOf(size)));
        String valuesOffsets = encodeArrayValuesOffsets(value);
        String encodedValues = encodeArrayValues(value);

        StringBuilder result = new StringBuilder();
        result.append(encodedLength);
        result.append(valuesOffsets);
        result.append(encodedValues);
        return result.toString();
    }

    /**
     * Encodes the array values offsets of the to be encrypted dynamic array, which are in our case
     * the heads of the encryption. Refer to
     *
     * @see <a
     *     href="https://docs.soliditylang.org/en/v0.5.3/abi-spec.html#formal-specification-of-the-encoding">encoding
     *     formal specification</a>
     *     <h2>Dynamic structs array encryption</h2>
     *     <p>An array of dynamic structs (ie, structs containing dynamic datatypes) is encoded in
     *     the following way: Considering X = [struct1, struct2] for example enc(X) = head(struct1)
     *     head(struct2) tail(struct1) tail(struct2) with: - tail(struct1) = enc(struct1) -
     *     tail(struct2) = enc(struct2) - head(struct1) = enc(len( head(struct1) head(struct2))) =
     *     enc(64), because the heads are 256bits - head(struct2) = enc(len( head(struct1)
     *     head(struct2) tail(struct1)))
     */
    private static <T extends Type> String encodeArrayValuesOffsets(DynamicArray<T> value) {
        StringBuilder result = new StringBuilder();
        boolean arrayOfBytes =
                !value.getValue().isEmpty() && value.getValue().get(0) instanceof DynamicBytes;
        boolean arrayOfString =
                !value.getValue().isEmpty() && value.getValue().get(0) instanceof Utf8String;
        boolean arrayOfDynamicStructs =
                !value.getValue().isEmpty() && value.getValue().get(0) instanceof DynamicStruct;
        if (arrayOfBytes || arrayOfString) {
            long offset = 0;
            for (int i = 0; i < value.getValue().size(); i++) {
                if (i == 0) {
                    offset = value.getValue().size() * MAX_BYTE_LENGTH;
                } else {
                    int bytesLength =
                            arrayOfBytes
                                    ? ((byte[]) value.getValue().get(i - 1).getValue()).length
                                    : ((String) value.getValue().get(i - 1).getValue()).length();
                    int numberOfWords = (bytesLength + MAX_BYTE_LENGTH - 1) / MAX_BYTE_LENGTH;
                    int totalBytesLength = numberOfWords * MAX_BYTE_LENGTH;
                    offset += totalBytesLength + MAX_BYTE_LENGTH;
                }
                result.append(
                        Numeric.toHexStringNoPrefix(
                                Numeric.toBytesPadded(
                                        new BigInteger(Long.toString(offset)), MAX_BYTE_LENGTH)));
            }
        } else if (arrayOfDynamicStructs) {
            result.append(encodeStructsArraysOffsets(value));
        }
        return result.toString();
    }

    /**
     * Encodes arrays of structs elements offsets. To be used when encoding a dynamic array or a
     * static array containing dynamic structs,
     *
     * @param value DynamicArray or StaticArray containing dynamic structs
     * @return encoded array offset
     */
    private static <T extends Type> String encodeStructsArraysOffsets(Array<T> value) {
        StringBuilder result = new StringBuilder();
        long offset = value.getValue().size();
        List<String> tailsEncoding =
                value.getValue().stream().map(TypeEncoder::encode).collect(Collectors.toList());
        for (int i = 0; i < value.getValue().size(); i++) {
            if (i == 0) {
                offset = offset * MAX_BYTE_LENGTH;
            } else {
                offset += tailsEncoding.get(i - 1).length() / 2;
            }
            result.append(
                    Numeric.toHexStringNoPrefix(
                            Numeric.toBytesPadded(
                                    new BigInteger(Long.toString(offset)), MAX_BYTE_LENGTH)));
        }
        return result.toString();
    }
}
