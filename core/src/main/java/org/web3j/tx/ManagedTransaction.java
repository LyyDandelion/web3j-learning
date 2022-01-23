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
package org.web3j.tx;

import java.io.IOException;
import java.math.BigInteger;

import org.web3j.ens.EnsResolver;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;

/**
 *
 * Generic transaction manager.
 *
 *
 * 通用事务管理器
 * */
public abstract class ManagedTransaction {

    /**
     * @see org.web3j.tx.gas.DefaultGasProvider
     * @deprecated use ContractGasProvider
     */
    public static final BigInteger GAS_PRICE = BigInteger.valueOf(22_000_000_000L);

    protected Web3j web3j;

    protected TransactionManager transactionManager;

    protected EnsResolver ensResolver;

    protected ManagedTransaction(Web3j web3j, TransactionManager transactionManager) {
        this(new EnsResolver(web3j), web3j, transactionManager);
    }

    protected ManagedTransaction(
            EnsResolver ensResolver, Web3j web3j, TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        this.ensResolver = ensResolver;
        this.web3j = web3j;
    }

    /**
     * This should only be used in case you need to get the {@link EnsResolver#getSyncThreshold()}
     * parameter, which dictates the threshold in milliseconds since the last processed block
     * timestamp should be to considered in sync the blockchain.
     *
     * <p>It is currently experimental and only used in ENS name resolution, but will probably be
     * made available for read calls in the future.
     *
     * @return sync threshold value in milliseconds
     */
    public long getSyncThreshold() {
        return ensResolver.getSyncThreshold();
    }

    /**
     * This should only be used in case you need to modify the {@link EnsResolver#getSyncThreshold}
     * parameter, which dictates the threshold in milliseconds since the last processed block
     * timestamp should be to considered in sync the blockchain.
     *
     * <p>It is currently experimental and only used in ENS name resolution, but will probably be
     * made available for read calls in the future.
     *
     * @param syncThreshold the sync threshold in milliseconds
     */
    public void setSyncThreshold(long syncThreshold) {
        ensResolver.setSyncThreshold(syncThreshold);
    }

    /**
     * Return the current gas price from the ethereum node.
     *
     * 从以太坊节点返回当前的 gas price。
     *
     * <p>Note: this method was previously called {@code getGasPrice} but was renamed to distinguish
     * it when a bean accessor method on {@link Contract} was added with that name. If you have a
     * Contract subclass that is calling this method (unlikely since those classes are usually
     * generated and until very recently those generated subclasses were marked {@code final}), then
     * you will need to change your code to call this method instead, if you want the dynamic
     * behavior.
     *
     * @return the current gas price, determined dynamically at invocation
     * @throws IOException if there's a problem communicating with the ethereum node
     */
    public BigInteger requestCurrentGasPrice() throws IOException {
        EthGasPrice ethGasPrice = web3j.ethGasPrice().send();

        return ethGasPrice.getGasPrice();
    }

    //发送交易
    protected TransactionReceipt send(
            String to, String data, BigInteger value, BigInteger gasPrice, BigInteger gasLimit)
            throws IOException, TransactionException {

        return transactionManager.executeTransaction(gasPrice, gasLimit, to, data, value);
    }

    //发送EIP1559 交易
    protected TransactionReceipt sendEIP1559(
            long chainId,
            String to,
            String data,
            BigInteger value,
            BigInteger gasLimit,
            BigInteger maxPriorityFeePerGas,
            BigInteger maxFeePerGas)
            throws IOException, TransactionException {

        return transactionManager.executeTransactionEIP1559(
                chainId, maxPriorityFeePerGas, maxFeePerGas, gasLimit, to, data, value);
    }

    /**
     * 发送交易
     * @param to  to,如果执行的合约交易则为合约地址
     * @param data 数据
     * @param value bnb
     * @param gasPrice gas price
     * @param gasLimit gas limit
     * @param constructor 是否构造
     */
    protected TransactionReceipt send(
            String to,
            String data,
            BigInteger value,
            BigInteger gasPrice,
            BigInteger gasLimit,
            boolean constructor)
            throws IOException, TransactionException {

        return transactionManager.executeTransaction(
                gasPrice, gasLimit, to, data, value, constructor);
    }

    protected String call(String to, String data, DefaultBlockParameter defaultBlockParameter)
            throws IOException {

        return transactionManager.sendCall(to, data, defaultBlockParameter);
    }
}
