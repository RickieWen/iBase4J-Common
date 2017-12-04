package top.ibase4j.core.listener;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import top.ibase4j.core.support.logger.Logger;

public class ServerListener implements ApplicationListener<ApplicationReadyEvent> {
    protected final Logger logger = Logger.getInstance();

    public void onApplicationEvent(ApplicationReadyEvent event) {
        logger.info("=================================");
        String server = event.getSpringApplication().getSources().iterator().next().toString();
        logger.info("系统[{}]启动完成!!!", server.substring(server.lastIndexOf(".") + 1));
        logger.info("=================================");
    }
}
