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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Fixed;
import org.web3j.abi.datatypes.Int;
import org.web3j.abi.datatypes.StaticArray;
import org.web3j.abi.datatypes.StaticStruct;
import org.web3j.abi.datatypes.StructType;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Ufixed;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.Utf8String;

/**
 *
 * 工具包
 *
 * */
public class Utils {
    //空构造
    private Utils() {}


    /**
     * 获取类型名称
     * @param typeReference 类型参考
     * @param <T>
     * @return
     */
    static <T extends Type> String getTypeName(TypeReference<T> typeReference) {
        try {
            //获取类型
            java.lang.reflect.Type reflectedType = typeReference.getType();

            Class<?> type;
            //是否为参数化类型
            if (reflectedType instanceof ParameterizedType) {
                //获取变量类型
                type = (Class<?>) ((ParameterizedType) reflectedType).getRawType();
                return getParameterizedTypeName(typeReference, type);
            } else {
                type = Class.forName(reflectedType.getTypeName());
                return getSimpleTypeName(type);
            }
        } catch (ClassNotFoundException e) {
            throw new UnsupportedOperationException("Invalid class reference provided", e);
        }
    }


    /**
     * 获取数据类型简写名称
     * @param type
     * @return
     */
    static String getSimpleTypeName(Class<?> type) {
        //类型简写，小写
        String simpleName = type.getSimpleName().toLowerCase();

        //uint int  Ufixed Fixed( 定长/不定长浮点类型)
        if (type.equals(Uint.class)
                || type.equals(Int.class)
                || type.equals(Ufixed.class)
                || type.equals(Fixed.class)) {
            return simpleName + "256";
        } else if (type.equals(Utf8String.class)) {
            return "string";
        } else if (type.equals(DynamicBytes.class)) {
            return "bytes";
        } else if (StructType.class.isAssignableFrom(type)) {
            return type.getName();
        } else {
            return simpleName;
        }
    }

    //获取参数化类型名称
    static <T extends Type, U extends Type> String getParameterizedTypeName(
            TypeReference<T> typeReference, Class<?> type) {

        try {
            //是否为动态数组类型
            if (type.equals(DynamicArray.class)) {
                //从数组中获取参数化类型
                Class<U> parameterizedType = getParameterizedTypeFromArray(typeReference);
                //获取简化类型名
                String parameterizedTypeName = getSimpleTypeName(parameterizedType);
                //返回参数类型，然后再补上[]格式
                return parameterizedTypeName + "[]";
            } else if (type.equals(StaticArray.class)) { //是否为静态数组类型
                //从数组中获取参数化类型
                Class<U> parameterizedType = getParameterizedTypeFromArray(typeReference);
                //从数组中获取参数化类型
                String parameterizedTypeName = getSimpleTypeName(parameterizedType);

                //括号内补上大小
                return parameterizedTypeName
                        + "["
                        + ((TypeReference.StaticArrayTypeReference) typeReference).getSize()
                        + "]";
            } else {
                //抛出非法类型
                throw new UnsupportedOperationException("Invalid type provided " + type.getName());
            }
        } catch (ClassNotFoundException e) {
            //抛出非法类型参考
            throw new UnsupportedOperationException("Invalid class reference provided", e);
        }
    }

    //从数组类型获取参数化类型
    @SuppressWarnings("unchecked")
    static <T extends Type> Class<T> getParameterizedTypeFromArray(TypeReference typeReference)
            throws ClassNotFoundException {

        java.lang.reflect.Type type = typeReference.getType();
        //返回类型数组
        java.lang.reflect.Type[] typeArguments =
                ((ParameterizedType) type).getActualTypeArguments();
        //获取第一个值的类型名
        String parameterizedTypeName = typeArguments[0].getTypeName();
        return (Class<T>) Class.forName(parameterizedTypeName);
    }

    //转换 列表类型
    @SuppressWarnings("unchecked")
    public static List<TypeReference<Type>> convert(List<TypeReference<?>> input) {
        List<TypeReference<Type>> result = new ArrayList<>(input.size());
        result.addAll(
                input.stream()
                        .map(typeReference -> (TypeReference<Type>) typeReference)
                        .collect(Collectors.toList()));
        return result;
    }


    /**
     *
     * 类型映射
     * @param input  输入列表数据
     * @param outerDestType 外部定义类型
     * @param innerType 内部类型
     */
    public static <T, R extends Type<T>, E extends Type<T>> List<E> typeMap(
            List<List<T>> input, Class<E> outerDestType, Class<R> innerType) {
        List<E> result = new ArrayList<>();
        try {
            //返回构造器（包括public 、非public 、 private
            Constructor<E> constructor =
                    outerDestType.getDeclaredConstructor(Class.class, List.class);
            for (List<T> ts : input) {
                //创建实例
                E e = constructor.newInstance(innerType, typeMap(ts, innerType));
                result.add(e);
            }
        } catch (NoSuchMethodException
                | IllegalAccessException
                | InstantiationException
                | InvocationTargetException e) {
            throw new TypeMappingException(e);
        }
        return result;
    }

    public static <T, R extends Type<T>> List<R> typeMap(List<T> input, Class<R> destType)
            throws TypeMappingException {

        List<R> result = new ArrayList<>(input.size());

        if (!input.isEmpty()) {
            try {
                //获取构造函数
                Constructor<R> constructor =
                        destType.getDeclaredConstructor(input.get(0).getClass());
                for (T value : input) {
                    //创建实例加入list
                    result.add(constructor.newInstance(value));
                }
            } catch (NoSuchMethodException
                    | IllegalAccessException
                    | InvocationTargetException
                    | InstantiationException e) {
                throw new TypeMappingException(e);
            }
        }
        return result;
    }

    /**
     *
     * 返回静态结构中规范字段的列表。
     * 示例：
     * struct Baz {
     *  Struct Bar { int a, int b },
     *  int c
     *
     * } 将返回 {a, b, c}。
     *
     * Returns flat list of canonical fields in a static struct. Example: struct Baz { Struct Bar {
     * int a, int b }, int c } will return {a, b, c}.
     *
     * @param classType Static struct type
     * @return Flat list of canonical fields in a nested struct
     */
    public static List<Field> staticStructNestedPublicFieldsFlatList(Class<Type> classType) {
        return staticStructsNestedFieldsFlatList(classType).stream()
                .filter(field -> Modifier.isPublic(field.getModifiers()))
                .collect(Collectors.toList());
    }

    /**
     *
     * 遍历静态结构并递归枚举其所有字段和嵌套结构字段。
     *
     * Goes over a static structs and enumerates all of its fields and nested structs fields
     * recursively.
     *
     * @param classType Static struct type  静态结构
     * @return Flat list of all the fields nested in the struct
     */
    @SuppressWarnings("unchecked")
    public static List<Field> staticStructsNestedFieldsFlatList(Class<Type> classType) {
        //classType.getDeclaredFields()获得所有声明的字段，即包括public、private和proteced，但是不包括父类的申明字段
        List<Field> canonicalFields =
                Arrays.stream(classType.getDeclaredFields())
                        .filter(field -> !StaticStruct.class.isAssignableFrom(field.getType()))
                        .collect(Collectors.toList());
        List<Field> nestedFields =
                Arrays.stream(classType.getDeclaredFields())
                        .filter(field -> StaticStruct.class.isAssignableFrom(field.getType()))
                        .map(
                                field ->
                                        staticStructsNestedFieldsFlatList(
                                                (Class<Type>) field.getType()))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
        return Stream.concat(canonicalFields.stream(), nestedFields.stream())
                .collect(Collectors.toList());
    }
}
