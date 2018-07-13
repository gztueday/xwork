package com.banggood.xwork.action.function;

import org.apache.tools.ant.util.DateUtils;

public class TimeFunctionImpl implements ITimeFunction {

	private long runTime;

	public TimeFunctionImpl(long runTime)
	{
		this.runTime=runTime;
	}

	@Override
	public String getMinute(long add, String format) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHour(long add, String format) {
	    if(add<0){
	        return DateUtils.format(runTime, format);
        }else{
	        return DateUtils.format(add,format);
        }
	}

	@Override
	public String getDate(long add, String format) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMoth(long add, String format) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getYear(long add, String format) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getCurrentTime(long add) {
		// TODO Auto-generated method stub
		return 0;
	}

}
