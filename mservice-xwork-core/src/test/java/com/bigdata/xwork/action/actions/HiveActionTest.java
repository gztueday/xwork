package com.bigdata.xwork.action.actions;


import com.bigdata.xwork.action.core.WorkActionBase;
import com.bigdata.xwork.action.impl.HiveAction;
import org.junit.Test;

public class HiveActionTest {
    @Test
    public void execute() {
        WorkActionBase ha = new HiveAction();
        try {
            ha.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
