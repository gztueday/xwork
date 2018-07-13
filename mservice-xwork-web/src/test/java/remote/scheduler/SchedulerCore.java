package remote.scheduler;

import com.banggood.mservice.xwork.web.WebAPP;
import com.banggood.xwork.scheduler.core.DispatcherScheduler;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * scheduler核心功能测试
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = WebAPP.class)
@WebAppConfiguration
public class SchedulerCore {

    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private DispatcherScheduler dispatcherScheduler;


}
