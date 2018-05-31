package com.troy.empireserialization.serializers;

import com.troy.empireserialization.ObjectIn;
import com.troy.empireserialization.ObjectOut;
import com.troy.empireserialization.io.in.Input;
import com.troy.empireserialization.io.out.Output;

public interface Serializer<T> {

	public Class<T> getType();

	/**
	 * Writes an object's fields to the output stream so that It can be de-serialized later using
	 * {@link #readFields(Object, Input)}
	 * 
	 * @param obj
	 *            The object to write
	 * @param out
	 *            The output to write to
	 */
	public void writeFields(ObjectOut objectOut, T obj, Output out);

	/**
	 * 
	 * Reads from the input provided to assign the fields of the object passed in.
	 * 
	 * @param objIn
	 *            The object input stream to read from in case this object has other objects as instance variables
	 * @param obj
	 *            The object whose fields should be assigned
	 * @param in
	 *            The input to read from
	 * @param type
	 *            The type of the object to read
	 */
	public void readFields(ObjectIn objIn, T obj, Input in, Class<T> type);

	/**
	 * Returns a new instance of the type represented by this serializer. If an instance cannot be created by this
	 * serializer for any reason, it should return null to delegate instance creation to the current instantiation
	 * strategy.
	 * 
	 * @return A new instance of the type represented by this serializer
	 */
	public T newInstance();

	/**
	 * Writes a TS Standard compliant type definition for the type this serializer represents
	 * 
	 * @param out
	 *            The output to write the definition to
	 */
	public void writeTypeDefinition(Output out);
}