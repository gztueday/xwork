package com.banggood.xwork.action.function;

/**
 * 服务启动时初始化函数信息
 */
public interface ITimeFunction {

    @FDescAnnotation(usage = "#{getMinute(-1,\"yyyy-MM-dd hh:mm\")}", desc = "获取当前运行实例的时间，精确到分钟")
    String getMinute(long add, String format);

    @FDescAnnotation(usage = "#{getHour(-1,\"yyyy-MM-dd hh\")}", desc = "获取当前运行实例的时间，精确到小时")
    String getHour(long add, String format);

    @FDescAnnotation(usage = "#{getDate(-1,\"yyyy-MM-dd\")}", desc = "获取当前运行实例的时间，精确到天")
    String getDate(long add, String format);

    @FDescAnnotation(usage = "#{getMoth(-1,\"yyyy-MM\")}", desc = "获取当前运行实例的时间，精确到月份")
    String getMoth(long add, String format);

    @FDescAnnotation(usage = "#{getYear(-1,\"yyyy\")}", desc = "获取当前运行实例的时间，精确到年")
    String getYear(long add, String format);

    @FDescAnnotation(usage = "#{getCurrentTime(-1000)}", desc = "在当前运行实例的毫秒")
    long getCurrentTime(long add);

}
