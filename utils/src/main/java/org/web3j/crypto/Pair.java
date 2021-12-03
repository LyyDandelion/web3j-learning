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

/**
 * 实体参数对
 */
public class Pair {
    //第一个参数
    private final Object first;
    //第二个参数
    private final Object second;
    //获取第一个参数
    public Object getFirst() {
        return first;
    }
    //获取第二个参数
    public Object getSecond() {
        return second;
    }
    //构造
    public Pair(Object first, Object second) {
        this.first = first;
        this.second = second;
    }
}
