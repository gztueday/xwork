package com.banggood.xwork.core.common;

import com.banggood.xwork.action.function.FunctionDescriptor;
import com.banggood.xwork.action.function.ParamDescriptor;
import com.banggood.xwork.core.exception.MatchException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FunctionUtils {
	
	private static String patternStr=".*\\$\\{(?<func>[a-zA-Z].*)\\}";

	/**
	 * 匹配函数
	 */
	public static FunctionDescriptor matchFunction(String command) {

		FunctionDescriptor descriptor = new FunctionDescriptor();

		Pattern pattern = Pattern.compile(patternStr);

		Matcher m = pattern.matcher(command);

		String[] groups = { "func" };
		
		m.find();

		String matchStr = m.group(groups[0]);

		if (matchStr.contains("(") && matchStr.contains(")")) {
			String function = matchStr.substring(0, matchStr.indexOf("("));
			String[] params = matchStr.substring(matchStr.indexOf("(")+1, matchStr.indexOf(")")).split(",");

			descriptor.setName(function);
			ParamDescriptor[] paramDescriptor = new ParamDescriptor[params.length];

			for (int i = 0; i < params.length; i++) {
				String tmp=null;
				if(params[i].startsWith("'"))
				{
					tmp=params[i].substring(1, params[i].length());
				}else{
					tmp=params[i];
				}
				
				if(tmp.contains("'"))
				{
					tmp=tmp.substring(0, tmp.length()-1);
				}
				paramDescriptor[i] = new ParamDescriptor(tmp, i);
			}

			descriptor.setParamDescriptor(paramDescriptor);

		}

		return descriptor;

	}

	/**
	 * 匹配变量
	 */
	public static String matchVariable(String command) {



		Pattern pattern = Pattern.compile(patternStr);

		Matcher m = pattern.matcher(command);

		String[] groups = { "func" };
		
		m.find();

		String matchStr = m.group(groups[0]);

		if (!matchStr.contains("(") && !matchStr.contains(")")) {
			return matchStr;
		}

		return null;
	}

	/**
	 * 检查字符串是否合法,不合法则抛错，如
	 * @throws MatchException 
	 * */
	public static MatchType checkParamConfig(String command) throws MatchException {
		
		Pattern pattern = Pattern.compile(patternStr);

		Matcher m = pattern.matcher(command);
		
		if(!m.find())
		{
			throw new MatchException("function name or variable name is Illegal,it only can contain number,lower-case letters,capitals");
		}
		
		String[] groups = { "func" };

		String matchStr = m.group(groups[0]);

		if (matchStr.contains("(") && matchStr.contains(")")) {
			return MatchType.MATCHFUNCTION;
		}
		
		return MatchType.MATCHVARIABLE;
	}
	
	
	public static String invokeTimeFunction(FunctionDescriptor fd)
	{
		StringBuffer sb=new StringBuffer();
		
		com.banggood.xwork.action.function.TimeFunctionImpl tf=new com.banggood.xwork.action.function.TimeFunctionImpl(System.currentTimeMillis());
		
		if(fd.getName().equals("getHour"))
		{
			sb.append(tf.getHour(Long.parseLong(fd.getParamDescriptor()[0].getName()), fd.getParamDescriptor()[1].getName()));
		}

		
		return sb.toString();
	}
	
	
	
	public enum MatchType {
		
		MATCHFUNCTION,MATCHVARIABLE

	}

	
}
