Web3j: Web3 Java Ethereum Ðapp API
==================================

[![Documentation Status](https://img.shields.io/travis/web3j/web3j-docs?label=docs)](https://docs.web3j.io/)
[![Build Status](https://travis-ci.org/web3j/web3j.svg?branch=master)](https://travis-ci.org/web3j/web3j)
[![codecov](https://codecov.io/gh/web3j/web3j/branch/master/graph/badge.svg)](https://codecov.io/gh/web3j/web3j)
[![Join the chat at https://gitter.im/web3j/web3j](https://img.shields.io/discourse/users?server=https%3A%2F%2Fcommunity.web3labs.com)](https://community.web3labs.com)


Web3j is a lightweight, highly modular, reactive, type safe Java and
Android library for working with Smart Contracts and integrating with
clients (nodes) on the Ethereum network:

Web3j 是一种轻量级、高度模块化、反应式、类型安全的 Java 和
用于处理智能合约和集成的 Android 库
以太坊网络上的客户端（节点）：

![image](https://github.com/web3j/web3j-docs/blob/master/docs/img/web3j_network.png)

This allows you to work with the [Ethereum](https://www.ethereum.org/)
blockchain, without the additional overhead of having to write your own
integration code for the platform.

这使您可以使用 [Ethereum](https://www.ethereum.org/)
区块链，无需编写自己的额外开销
平台的集成代码。

The [Java and the Blockchain](https://www.youtube.com/watch?v=ea3miXs_P6Y) talk provides
an overview of blockchain, Ethereum and Web3j.

[Java 和区块链](https://www.youtube.com/watch?v=ea3miXs_P6Y) 演讲提供
区块链、以太坊和 Web3j 的概述。

Features 特点
--------

-   Complete implementation of Ethereum's
    [JSON-RPC](https://github.com/ethereum/wiki/wiki/JSON-RPC) client
    API over HTTP and IPC  以太坊的完整实现基于 HTTP 和 IPC 的 API客户端 
    
-   Ethereum wallet support 以太坊钱包支持
-   Auto-generation of Java smart contract wrappers to create, deploy,
    transact with and call smart contracts from native Java code 
    ([Solidity](http://solidity.readthedocs.io/en/latest/using-the-compiler.html#using-the-commandline-compiler)
    and
    [Truffle](https://github.com/trufflesuite/truffle-contract-schema)
    definition formats supported)  自动生成 Java 智能合约包装器以创建、部署、
                                  从原生 Java 代码处理和调用智能合约 ([Solidity]  和[Truffle] 支持的定义格式）
-   Reactive-functional API for working with filters
-   [Ethereum Name Service (ENS)](https://ens.domains/) support  ENS支持
-   Support for Parity's 
    [Personal](https://github.com/paritytech/parity/wiki/JSONRPC-personal-module),
    and Geth's
    [Personal](https://github.com/ethereum/go-ethereum/wiki/Management-APIs#personal)
    client APIs    支持第三方和geth客户端api
-   Support for [Alchemy](https://docs.alchemyapi.io/alchemy/guides/getting-started#web-3-j) and [Infura](https://infura.io/), so you don't have to run
    an Ethereum client yourself  支持 [Alchemy] 和 [Infura]，所以你不需要必须运行自己的以太坊客户端
-   Comprehensive integration tests demonstrating a number of the above
    scenarios   上述一些内容场景都有全面的集成测试
-   Command line tools 命令行工具
-   Android compatible 兼容Android
-   Support for JP Morgan's Quorum via  通过以下方式支持 JP Morgan 的 Quorum
    [web3j-quorum](https://github.com/web3j/quorum)
-   Support for [EEA Privacy features as described in EEA
    documentation](https://entethalliance.org/technical-documents/) and
    implemented in [Hyperledger
    Besu](https://besu.hyperledger.org/en/latest/Reference/API-Methods/#eea-methods).
     支持EEA 和实现 Hyperledger
       
It has five runtime dependencies: 运行时依赖有五个

-   [RxJava](https://github.com/ReactiveX/RxJava) for its
    reactive-functional API  RxJava 反应函数式 API
-   [OKHttp](https://hc.apache.org/httpcomponents-client-ga/index.html)
    for HTTP connections  OKHttp提供HTTP连接
-   [Jackson Core](https://github.com/FasterXML/jackson-core) for fast
    JSON serialisation/deserialization  Jackson Core提供序列化/反序列化
-   [Bouncy Castle](https://www.bouncycastle.org/) ([Spongy
    Castle](https://rtyley.github.io/spongycastle/) on Android) for
    crypto  Bouncy Castle 提供加密
-   [Jnr-unixsocket](https://github.com/jnr/jnr-unixsocket) for \*nix
        IPC (not available on Android) 

It also uses [JavaPoet](https://github.com/square/javapoet) for
generating smart contract wrappers.
它还使用 [JavaPoet]生成智能合约包装器。

QuickStart 快速开始
---------
The simplest way to start your journey with Web3j is to create a project.
We provide this functionality using the [Web3j CLI](http://docs.web3j.io/latest/command_line_tools/). This latter can be installed as follows:

开始您的 Web3j 之旅的最简单方法是创建一个项目。
我们使用 [Web3j CLI] 提供此功能。后者可以按如下方式安装：

For Unix:

```shell script
curl -L get.web3j.io | sh && source ~/.web3j/source.sh
```

For Windows, in Powershell:

```shell script
Set-ExecutionPolicy Bypass -Scope Process -Force; iex ((New-Object System.Net.WebClient).DownloadString('https://raw.githubusercontent.com/web3j/web3j-installer/master/installer.ps1'))
```

Create a new project by running: 通过运行创建一个新项目

```shell script
$ web3j new 
```

Or use our [Maven](https://github.com/web3j/web3j-maven-plugin) or [Gradle](https://github.com/web3j/web3j-gradle-plugin) plugins.
 或使用maven或者gradle插件

#### Please head to the [Web3j Documentation](https://docs.web3j.io) for further instructions on using Web3j.
请前往 [Web3j 文档] 以获取有关使用 Web3j 的更多说明。

Maven
-----

Java:

```
<dependency>
  <groupId>org.web3j</groupId>
  <artifactId>core</artifactId>
  <version>4.8.7</version>
</dependency>
```

Android:

```
<dependency>
  <groupId>org.web3j</groupId>
  <artifactId>core</artifactId>
  <version>4.8.7-android</version>
</dependency>
```

Gradle
------

Java:

implementation ('org.web3j:core:4.8.7')

Android:

implementation ('org.web3j:core:4.8.7-android')

Build instructions 构建说明
------------------

Web3j includes integration tests for running against a live Ethereum
client. If you do not have a client running, you can exclude their
execution as per the below instructions.

Web3j 包括针对实时以太坊运行的集成测试客户。如果您没有运行客户端，则可以排除他们的
按照以下说明执行。

To run a full build (excluding integration tests):
    完整构建运行（不包括集成测试）：
``` {.sourceCode .bash}
$ ./gradlew check
```

To run the integration tests, you will need to set up these variables in order to pull the Docker 
images from the Docker Hub registry:
运行集成测试，您需要设置这些变量以拉取 Docker 从 Docker Hub 注册表的图像：
- `registry.username`
- `registry.password`

Then run the following command: 然后运行以下命令：

``` {.sourceCode .bash}
$ ./gradlew -Pintegration-tests=true :integration-tests:test
```

Check the [Docker client API](https://github.com/docker-java/docker-java/blob/master/docs/getting_started.md#instantiating-a-dockerclientconfig)
for more information on configuration options. 

更多信息参看[Docker client API]

Commercial support and training 商业支持和培训
-------------------------------

Commercial support and training is available from
[web3labs.com](https://www.web3labs.com/web3j-sdk).
 商业支持和培训 [web3labs.com]
 
License 许可
------
Apache 2.0
