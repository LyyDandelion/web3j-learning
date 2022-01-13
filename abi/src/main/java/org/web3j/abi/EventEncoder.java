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

import java.util.List;
import java.util.stream.Collectors;

import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

/**
 * Ethereum filter encoding. Further limited details are available <a
 * href="https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI#events">here</a>.
 *
 * 以太坊过滤器编码。
 */
public class EventEncoder {

    private EventEncoder() {}
    //编码
    public static String encode(Event event) {
        //构建方法签名
        String methodSignature = buildMethodSignature(event.getName(), event.getParameters());
        //构建事件签名
        return buildEventSignature(methodSignature);
    }
    //构建方法签名
    static <T extends Type> String buildMethodSignature(
            String methodName, List<TypeReference<T>> parameters) {

        StringBuilder result = new StringBuilder();
        //追加方法
        result.append(methodName);
        //追加 （
        result.append("(");
        //组合参数类型
        String params =
                parameters.stream().map(p -> Utils.getTypeName(p)).collect(Collectors.joining(","));
        result.append(params);
        result.append(")");
        return result.toString();
    }
    //构建事件签名
    public static String buildEventSignature(String methodSignature) {
        //获得byte数组
        byte[] input = methodSignature.getBytes();
        //转换Keccak-256 哈希值
        byte[] hash = Hash.sha3(input);
        //转换hex
        return Numeric.toHexString(hash);
    }
}
