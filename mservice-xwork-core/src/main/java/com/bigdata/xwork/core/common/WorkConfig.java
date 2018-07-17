package com.bigdata.xwork.core.common;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

import com.bigdata.xwork.action.core.WorkActionParam;
import com.bigdata.xwork.core.exception.NoParamException;
import org.apache.tools.ant.util.DateUtils;


/**
 *
 * */
public class WorkConfig  implements Serializable {

	private Map<String, WorkActionParam> parameters;

	public WorkConfig() {
	    //parameters = Collections.synchronizedMap(new HashMap<String, WorkActionParam>());
        parameters = new HashMap<>();
	}


	public Map<String, WorkActionParam> getParameters() {
		return this.parameters;
	}

	/**
	 * Removes all of the mappings from this map.
	 */
	public void clear() {
		parameters.clear();
	}


	public void registerParam(String key,WorkActionParam param)
	{
		this.parameters.put(key, param);
	}



	/**
	 * Associates all of the given map's keys and values in the Context.
	 */
	public void putAll(Map<String, WorkActionParam> map) {
		parameters.putAll(map);
	}

	public void putAllMap(Map<String, String> map) {
		Iterator<Entry<String, String>> iter=map.entrySet().iterator();
		while(iter.hasNext())
		{
			Entry<String, String> entry=iter.next();
			this.put(entry.getKey(), entry.getValue());
		}
	}


	public void putMap(String key,Map<String,String> value)
	{
		put(key,value);
	}

	public void putMap(String key,Map<String,String> map,String updater,String comment) throws NoParamException
	{
		WorkActionParam param=new WorkActionParam();
		param.updateContent(map, updater, comment);
		this.parameters.put(key,param);
	}

	/**
	 *
	 * @param key
	 *            key with which the specified value is to be associated
	 * @param value
	 *            to be associated with the specified key
	 */
	public void putInteger(String key, int value) {
		this.put(key, value);
	}


	public void putFloat(String key, float value) {
		this.put(key, value);
	}

	public void putLong(String key,Long value)
	{
		put(key,value);
	}

	protected void put(String key,Object value)
	{

		WorkActionParam param=new WorkActionParam();
		param.setContent(value);
		this.parameters.put(key, param);
	}

	public void putWorkActionParam(String key,WorkActionParam param)
	{
		this.parameters.put(key, param);
	}

	public void putString(String key,String value)
	{
		put(key,value);
	}

	public void putObject(String key,Object value)
	{
		this.put(key, value);
	}

	public void putBoolean(String key,boolean value)
	{
		this.put(key, value);
	}

	public void put(String key, String value,String updater,String comment) throws NoParamException {
		WorkActionParam param=new WorkActionParam();
		param.updateContent(value, updater, comment);
		parameters.put(key, param);
	}

	/**
	 * Gets value mapped to key, returning defaultValue if unmapped.
	 *
	 */
	public Boolean getBoolean(String key, Boolean defaultValue) {
		String value = get(key);
		if (value != null) {
			return Boolean.parseBoolean(value.trim());
		}
		return defaultValue;
	}

	/**
	 *
	 * @param key
	 *            to be found
	 * @return value associated with key or null if unmapped
	 */
	public Boolean getBoolean(String key) {
		return getBoolean(key, null);
	}



	/**
	 * Gets value mapped to key, returning defaultValue if unmapped.
	 *
	 * @param key
	 *            to be found
	 * @param defaultValue
	 *            returned if key is unmapped
	 * @return value associated with key
	 */
	public Integer getInteger(String key, Integer defaultValue) {
		String value = get(key);
		if (value != null) {
			return Integer.parseInt(value.trim());
		}
		return defaultValue;
	}



	/**
	 * Gets value mapped to key, returning null if unmapped.
	 *
	 * @param key
	 *            to be found
	 * @return value associated with key or null if unmapped
	 */
	public Integer getInteger(String key) {
		return getInteger(key, null);
	}

	/**
	 * Gets value mapped to key, returning defaultValue if unmapped.
	 *
	 * @param key
	 *            to be found
	 * @param defaultValue
	 *            returned if key is unmapped
	 * @return value associated with key
	 */
	public Long getLong(String key, Long defaultValue) {
		String value = get(key);
		if (value != null) {
			return System.currentTimeMillis();
		}
		return defaultValue;
	}

	/**
	 *
	 * @param key
	 *            to be found
	 * @return value associated with key or null if unmapped
	 */
	public Long getLong(String key) {
		return getLong(key, null);
	}

	/**
	 * Gets value mapped to key, returning defaultValue if unmapped.
	 *
	 * @param key
	 *            to be found
	 * @param defaultValue
	 *            returned if key is unmapped
	 * @return value associated with key
	 */
	public String getString(String key, String defaultValue) {
		return get(key, defaultValue);
	}

	/**
	 * Gets value mapped to key, returning null if unmapped.
	 *
	 * @param key
	 *            to be found
	 * @return value associated with key or null if unmapped
	 */
	public String getString(String key) {
		return get(key);
	}

	private String get(String key, String defaultValue) {
		WorkActionParam result = parameters.get(key);
		if (result != null) {
			return result.getContent().toString();
		}
		return defaultValue;
	}

	@SuppressWarnings("unchecked")
	public Map<String,Object> getMap(String key)
	{
		if(this.parameters.containsKey(key))
		{
			return (Map<String,Object>)this.parameters.get(key).getContent();
		}else
		{
			throw new RuntimeException("no such parameter for key:"+key);
		}

	}

	@SuppressWarnings("unchecked")
	public List<String> getList(String key)
	{
		if(this.parameters.containsKey(key))
		{
			return (List<String>)this.parameters.get(key).getContent();
		}else
		{
			throw new RuntimeException("no such parameter for key:"+key);
		}
	}

	private String get(String key) {
		return get(key, null);
	}

	@Override
	public String toString() {
		return "{ parameters:" + parameters + " }";
	}


	public void setParameters(Map<String, WorkActionParam> parameters) {
		this.parameters = Collections.synchronizedMap(parameters);
	}

}
