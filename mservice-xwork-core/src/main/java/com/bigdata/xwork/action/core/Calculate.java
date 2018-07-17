package com.bigdata.xwork.action.core;

import org.springframework.stereotype.Component;

/**
 * Created by zouyi on 2018/3/9.
 */
@Component
public class Calculate implements SeekOut {

    @Override
    public long calculatorDependVersion(long remoteVersion, long calculate) {
        return remoteVersion - calculate;
    }

    @Override
    public long calculatoRemoteVersion(long dependVersion, long calculate) {
        return dependVersion + calculate;
    }

}
