package com.bigdata.xwork.action.core;

/**
 * 计算dependVersion接口
 */
public interface SeekOut {

    long calculatorDependVersion(long remoteVersion, long calculate);

    long calculatoRemoteVersion(long dependVersion, long calculate);

}
