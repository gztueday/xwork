package com.banggood.xwork.action.actions;


import com.banggood.xwork.action.core.WorkActionBase;
import com.banggood.xwork.action.impl.HiveAction;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

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
