/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.table.data;

import org.apache.flink.annotation.PublicEvolving;
import org.apache.flink.table.types.logical.ArrayType;
import org.apache.flink.types.variant.Variant;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 * An internal data structure representing data of {@link ArrayType}.
 *
 * <p>Note: All elements of this data structure must be internal data structures and must be of the
 * same type. See {@link RowData} for more information about internal data structures.
 *
 * <p>{@link GenericArrayData} is a generic implementation of {@link ArrayData} which wraps regular
 * Java arrays.
 *
 * <p>Every instance wraps a one-dimensional Java array. Non-primitive arrays can be used for
 * representing element nullability. The Java array might be a primitive array such as {@code int[]}
 * or an object array (i.e. instance of {@code Object[]}). Object arrays that contain boxed types
 * (e.g. {@link Integer}) MUST be boxed arrays (i.e. {@code new Integer[]{1, 2, 3}}, not {@code new
 * Object[]{1, 2, 3}}). For multidimensional arrays, an array of {@link GenericArrayData} MUST be
 * passed. For example:
 *
 * <pre>{@code
 * // ARRAY < ARRAY < INT NOT NULL > >
 * new GenericArrayData(
 *   new GenericArrayData[]{
 *     new GenericArrayData(new int[3]),
 *     new GenericArrayData(new int[5])
 *   }
 * )
 * }</pre>
 */
@PublicEvolving
public final class GenericArrayData implements ArrayData {

    private final Object array;
    private final int size;
    private final boolean isPrimitiveArray;

    /**
     * Creates an instance of {@link GenericArrayData} using the given Java array.
     *
     * <p>Note: All elements of the array must be internal data structures.
     */
    public GenericArrayData(Object[] array) {
        this(array, array.length, false);
    }

    public GenericArrayData(int[] primitiveArray) {
        this(primitiveArray, primitiveArray.length, true);
    }

    public GenericArrayData(long[] primitiveArray) {
        this(primitiveArray, primitiveArray.length, true);
    }

    public GenericArrayData(float[] primitiveArray) {
        this(primitiveArray, primitiveArray.length, true);
    }

    public GenericArrayData(double[] primitiveArray) {
        this(primitiveArray, primitiveArray.length, true);
    }

    public GenericArrayData(short[] primitiveArray) {
        this(primitiveArray, primitiveArray.length, true);
    }

    public GenericArrayData(byte[] primitiveArray) {
        this(primitiveArray, primitiveArray.length, true);
    }

    public GenericArrayData(boolean[] primitiveArray) {
        this(primitiveArray, primitiveArray.length, true);
    }

    private GenericArrayData(Object array, int size, boolean isPrimitiveArray) {
        this.array = array;
        this.size = size;
        this.isPrimitiveArray = isPrimitiveArray;
    }

    /**
     * Returns true if this is a primitive array.
     *
     * <p>A primitive array is an array whose elements are of primitive type.
     */
    public boolean isPrimitiveArray() {
        return isPrimitiveArray;
    }

    /**
     * Converts this {@link GenericArrayData} into an array of Java {@link Object}.
     *
     * <p>The method will convert a primitive array into an object array. But it will not convert
     * internal data structures into external data structures (e.g. {@link StringData} to {@link
     * String}).
     */
    public Object[] toObjectArray() {
        if (isPrimitiveArray) {
            Class<?> arrayClass = array.getClass();
            if (int[].class.equals(arrayClass)) {
                return ArrayUtils.toObject((int[]) array);
            } else if (long[].class.equals(arrayClass)) {
                return ArrayUtils.toObject((long[]) array);
            } else if (float[].class.equals(arrayClass)) {
                return ArrayUtils.toObject((float[]) array);
            } else if (double[].class.equals(arrayClass)) {
                return ArrayUtils.toObject((double[]) array);
            } else if (short[].class.equals(arrayClass)) {
                return ArrayUtils.toObject((short[]) array);
            } else if (byte[].class.equals(arrayClass)) {
                return ArrayUtils.toObject((byte[]) array);
            } else if (boolean[].class.equals(arrayClass)) {
                return ArrayUtils.toObject((boolean[]) array);
            }
            throw new RuntimeException("Unsupported primitive array: " + arrayClass);
        } else {
            return (Object[]) array;
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isNullAt(int pos) {
        return !isPrimitiveArray && ((Object[]) array)[pos] == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GenericArrayData that = (GenericArrayData) o;
        return size == that.size
                && isPrimitiveArray == that.isPrimitiveArray
                && Objects.deepEquals(array, that.array);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(size, isPrimitiveArray);
        result = 31 * result + Arrays.deepHashCode(new Object[] {array});
        return result;
    }

    // ------------------------------------------------------------------------------------------
    // Read-only accessor methods
    // ------------------------------------------------------------------------------------------

    @Override
    public boolean getBoolean(int pos) {
        return isPrimitiveArray ? ((boolean[]) array)[pos] : (boolean) getObject(pos);
    }

    @Override
    public byte getByte(int pos) {
        return isPrimitiveArray ? ((byte[]) array)[pos] : (byte) getObject(pos);
    }

    @Override
    public short getShort(int pos) {
        return isPrimitiveArray ? ((short[]) array)[pos] : (short) getObject(pos);
    }

    @Override
    public int getInt(int pos) {
        return isPrimitiveArray ? ((int[]) array)[pos] : (int) getObject(pos);
    }

    @Override
    public long getLong(int pos) {
        return isPrimitiveArray ? ((long[]) array)[pos] : (long) getObject(pos);
    }

    @Override
    public float getFloat(int pos) {
        return isPrimitiveArray ? ((float[]) array)[pos] : (float) getObject(pos);
    }

    @Override
    public double getDouble(int pos) {
        return isPrimitiveArray ? ((double[]) array)[pos] : (double) getObject(pos);
    }

    @Override
    public byte[] getBinary(int pos) {
        return (byte[]) getObject(pos);
    }

    @Override
    public StringData getString(int pos) {
        return (StringData) getObject(pos);
    }

    @Override
    public DecimalData getDecimal(int pos, int precision, int scale) {
        return (DecimalData) getObject(pos);
    }

    @Override
    public TimestampData getTimestamp(int pos, int precision) {
        return (TimestampData) getObject(pos);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> RawValueData<T> getRawValue(int pos) {
        return (RawValueData<T>) getObject(pos);
    }

    @Override
    public Variant getVariant(int pos) {
        return (Variant) getObject(pos);
    }

    @Override
    public RowData getRow(int pos, int numFields) {
        return (RowData) getObject(pos);
    }

    @Override
    public ArrayData getArray(int pos) {
        return (ArrayData) getObject(pos);
    }

    @Override
    public MapData getMap(int pos) {
        return (MapData) getObject(pos);
    }

    private Object getObject(int pos) {
        return ((Object[]) array)[pos];
    }

    // ------------------------------------------------------------------------------------------
    // Conversion Utilities
    // ------------------------------------------------------------------------------------------

    private boolean anyNull() {
        for (Object element : (Object[]) array) {
            if (element == null) {
                return true;
            }
        }
        return false;
    }

    private void checkNoNull() {
        if (anyNull()) {
            throw new RuntimeException("Primitive array must not contain a null value.");
        }
    }

    @Override
    public boolean[] toBooleanArray() {
        if (isPrimitiveArray) {
            return (boolean[]) array;
        }
        checkNoNull();
        return ArrayUtils.toPrimitive((Boolean[]) array);
    }

    @Override
    public byte[] toByteArray() {
        if (isPrimitiveArray) {
            return (byte[]) array;
        }
        checkNoNull();
        return ArrayUtils.toPrimitive((Byte[]) array);
    }

    @Override
    public short[] toShortArray() {
        if (isPrimitiveArray) {
            return (short[]) array;
        }
        checkNoNull();
        return ArrayUtils.toPrimitive((Short[]) array);
    }

    @Override
    public int[] toIntArray() {
        if (isPrimitiveArray) {
            return (int[]) array;
        }
        checkNoNull();
        return ArrayUtils.toPrimitive((Integer[]) array);
    }

    @Override
    public long[] toLongArray() {
        if (isPrimitiveArray) {
            return (long[]) array;
        }
        checkNoNull();
        return ArrayUtils.toPrimitive((Long[]) array);
    }

    @Override
    public float[] toFloatArray() {
        if (isPrimitiveArray) {
            return (float[]) array;
        }
        checkNoNull();
        return ArrayUtils.toPrimitive((Float[]) array);
    }

    @Override
    public double[] toDoubleArray() {
        if (isPrimitiveArray) {
            return (double[]) array;
        }
        checkNoNull();
        return ArrayUtils.toPrimitive((Double[]) array);
    }
}
