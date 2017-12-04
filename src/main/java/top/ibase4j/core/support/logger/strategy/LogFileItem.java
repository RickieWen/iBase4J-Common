package top.ibase4j.core.support.logger.strategy;

import java.util.ArrayList;

import top.ibase4j.core.util.InstanceUtil;

/**
 * 日志结构
 * @author ShenHuaJie
 * @version 2017年12月4日 上午11:40:24
 */
public class LogFileItem {

    /** 不包括路径，不带扩展名的日志文件名称 如：MsgInner */
    public String logFileName = "";

    /** 包括路径的完整日志名称 */
    public String fullLogFileName = "";

    /** 当前日志文件大小 */
    public long currLogSize = 0;

    /** 当前正在使用的日志缓存 */
    public char currLogBuff = 'A';

    /** 日志缓存列表A */
    public ArrayList<StringBuffer> alLogBufA = InstanceUtil.newArrayList();

    /** 日志缓存列表B */
    public ArrayList<StringBuffer> alLogBufB = InstanceUtil.newArrayList();

    /** 下次日志输出到文件时间 */
    public long nextWriteTime = 0;

    /** 上次写入时的日期 */
    public String lastPCDate = "";

    /** 当前已缓存大小 */
    public long currCacheSize = 0;
}
