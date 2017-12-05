package top.ibase4j.core.support.logger;

import top.ibase4j.core.support.logger.strategy.LogManager;
import top.ibase4j.core.util.DateUtil;
import top.ibase4j.core.util.DateUtil.DATE_PATTERN;
import top.ibase4j.core.util.ExceptionUtil;

/**
 * 日志工具类
 * @author ShenHuaJie
 * @version 2017年12月4日 上午11:39:50
 */
public class Logger {

    private static Logger instance;
    private static LogManager logManager;
    private static String FQCN = Logger.class.getName();
    static {
        logManager = LogManager.getInstance();
    }

    public Logger() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                close();
            }
        }));
    }

    public static synchronized Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    /**
     * 写调试日志
     * @param logMsg 日志内容
     */
    public void debug(String logMsg, String... param) {
        logMsg = format(logMsg, param);
        writeLog(Constant.CFG_LOG_NAME, Constant.DEBUG, logMsg, null);
    }

    /**
     * 写调试日志
     * @param logMsg 日志内容
     */
    public void debug(String logMsg, Throwable e) {
        writeLog(Constant.CFG_LOG_NAME, Constant.DEBUG, logMsg, e);
    }

    /**
     * 写普通日志
     * @param logMsg 日志内容
     */
    public void info(String logMsg, String... param) {
        logMsg = format(logMsg, param);
        writeLog(Constant.CFG_LOG_NAME, Constant.INFO, logMsg, null);
    }

    /**
     * 写普通日志
     * @param logMsg 日志内容
     */
    public void info(String logMsg, Throwable e) {
        writeLog(Constant.CFG_LOG_NAME, Constant.INFO, logMsg, e);
    }

    /**
     * 写警告日志
     * @param logMsg 日志内容
     */
    public void warn(String logMsg, String... param) {
        logMsg = format(logMsg, param);
        writeLog(Constant.CFG_LOG_NAME, Constant.WARN, logMsg, null);
    }

    /**
     * 写警告日志
     * @param logMsg 日志内容
     */
    public void warn(String logMsg, Throwable e) {
        writeLog(Constant.CFG_LOG_NAME, Constant.WARN, logMsg, e);
    }

    /**
     * 写错误日志
     * @param logMsg 日志内容
     */
    public void error(String logMsg, String... param) {
        logMsg = format(logMsg, param);
        writeLog(Constant.CFG_LOG_NAME, Constant.ERROR, logMsg, null);
    }

    /**
     * 写错误日志
     * @param logMsg 日志内容
     */
    public void error(String logMsg, Throwable e) {
        writeLog(Constant.CFG_LOG_NAME, Constant.ERROR, logMsg, e);
    }

    /**
     * 写严重错误日志
     * @param logMsg 日志内容
     */
    public void fatal(String logMsg, String... param) {
        logMsg = format(logMsg, param);
        writeLog(Constant.CFG_LOG_NAME + "-fatal", Constant.FATAL, logMsg, null);
    }

    /**
     * 写严重错误日志
     * @param logMsg 日志内容
     */
    public void fatal(String logMsg, Throwable e) {
        writeLog(Constant.CFG_LOG_NAME + "-fatal", Constant.FATAL, logMsg, e);
    }

    /**
     * 写系统日志
     * @param level 日志级别
     * @param logMsg 日志内容
     */
    public void writeLog(int level, String logMsg) {
        writeLog(Constant.CFG_LOG_NAME, level, logMsg, null);
    }

    /**
     * 写日志
     * @param logFileName 日志文件名
     * @param level 日志级别
     * @param logMsg 日志内容
     */
    public void writeLog(String logFileName, int level, String logMsg, Throwable throwable) {
        if (logMsg != null && Constant.CFG_LOG_LEVEL.indexOf("" + level) >= 0) {
            StringBuffer sb = new StringBuffer(logMsg.length() + 100);
            sb.append(DateUtil.getDateTime(DATE_PATTERN.YYYY_MM_DD_HH_MM_SS_SSS));
            sb.append(" [");
            sb.append(Constant.LOG_DESC_MAP.get(String.valueOf(level)));
            sb.append("] [");
            sb.append(Thread.currentThread().getName());
            sb.append("] [");
            sb.append(calcLocation(FQCN));
            sb.append("] - ");
            sb.append(logMsg);
            sb.append("\n");
            if (throwable != null) {
                sb.append(ExceptionUtil.getStackTraceAsString(throwable));
                sb.append("\n");
            }
            logManager.addLog(logFileName, sb);
            // 错误信息强制打印到控制台；若 CONSOLE_PRINT 配置为 true，也将日志打印到控制台
            if (Constant.ERROR == level || Constant.FATAL == level || Constant.CONSOLE_PRINT) {
                try {
                    System.out.print(
                        new String(sb.toString().getBytes(Constant.CFG_CHARSET_NAME), Constant.CFG_CHARSET_NAME));
                } catch (Exception e) {
                    System.out.print(ExceptionUtil.getStackTraceAsString(e));
                }
            }
        }
    }

    /**
     * 优雅关闭
     */
    private void close() {
        logManager.close();
    }

    /**
     * @param logMsg
     * @param param
     * @return
     */
    private String format(String logMsg, String... param) {
        if (param != null) {
            for (String p : param) {
                logMsg = logMsg.replace("{}", p);
            }
        }
        return logMsg;
    }

    private static StackTraceElement calcLocation(final String fqcn) {
        if (fqcn == null) {
            return null;
        }
        // LOG4J2-1029 new Throwable().getStackTrace is faster than Thread.currentThread().getStackTrace().
        final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        boolean next = false;
        for (final StackTraceElement element : stackTrace) {
            final String className = element.getClassName();
            if (next && !fqcn.equals(className)) {
                return element;
            }
            if (fqcn.equals(className)) {
                next = true;
            } else if ("?".equals(className)) {
                break;
            }
        }
        return null;
    }
}
