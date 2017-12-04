/**
 * 
 */
package top.ibase4j.core.support.logger;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;

import top.ibase4j.core.util.ExceptionUtil;
import top.ibase4j.core.util.InstanceUtil;

/**
 * 
 * @author ShenHuaJie
 * @version 2017年12月4日 上午10:35:46
 */
public class Config {
    /** 配置文件名 */
    private static final String CONFIG_FILE_NAME = "logger.properties";

    /** 配置map */
    private static Map<String, Object[]> propsMap = InstanceUtil.newHashMap();

    /**
     * 从配置文件中取得 String 值，若无则返回默认值
     * @param keyName 属性名
     * @param defaultValue 默认值
     * @return 属性值
     */
    public static String getString(String keyName, String defaultValue) {
        String value = getString(keyName);
        if (value != null && value.length() > 0) {
            return value.trim();
        } else {
            return defaultValue;
        }
    }

    /**
     * 从配置文件中取得 int 值，若无（或解析异常）则返回默认值
     * @param keyName 属性名
     * @param defaultValue 默认值
     * @return 属性值
     */
    public static int getInt(String keyName, int defaultValue) {
        String value = getString(keyName);
        if (value != null && value.length() > 0) {
            try {
                int parseValue = Integer.parseInt(value.trim());
                return parseValue;
            } catch (Exception e) {
                System.err.println(ExceptionUtil.getStackTraceAsString(e));
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    /**
     * 从配置文件中取得 long 值，若无（或解析异常）则返回默认值
     * @param keyName 属性名
     * @param defaultValue 默认值
     * @return 属性值
     */
    public static long getLong(String keyName, long defaultValue) {
        String value = getString(keyName);
        if (value != null && value.length() > 0) {
            try {
                long parseValue = Long.parseLong(value.trim());
                return parseValue;
            } catch (Exception e) {
                System.err.println(ExceptionUtil.getStackTraceAsString(e));
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    /**
     * 从配置文件中取得 boolean 值，若无则返回默认值
     * @param keyName 属性名
     * @param defaultValue 默认值
     * @return 属性值
     */
    public static boolean getBoolean(String keyName, boolean defaultValue) {
        String value = getString(keyName);
        if (value != null && value.length() > 0) {
            return Boolean.parseBoolean(value.trim());
        } else {
            return defaultValue;
        }
    }

    /**
     * 从配置文件中读取字符串的值
     * 配置文件查找顺序：
     *      1-项目根路径
     *      2-src/main/resources
     * @param keyName 属性名
     * @return 属性值
     */
    private static String getString(String keyName) {
        Properties props = null;
        boolean bIsNeedLoadCfg = false;

        String filePath = CONFIG_FILE_NAME;
        File cfgFile = new File(filePath);
        if (!cfgFile.exists()) {
            try {
                filePath = Config.class.getClassLoader().getResource(CONFIG_FILE_NAME).getPath();
            } catch (Exception e) {
                System.err.println(ExceptionUtil.getStackTraceAsString(e));
                return null;
            }
            cfgFile = new File(filePath);
            if (!cfgFile.exists()) {
                return null;
            }
        }

        Object[] arrs = propsMap.get(filePath);
        if (arrs == null) {
            bIsNeedLoadCfg = true;
        } else {
            Long lastModify = (Long)arrs[0];
            if (lastModify.longValue() != cfgFile.lastModified()) {
                bIsNeedLoadCfg = true;
            } else {
                props = (Properties)arrs[1];
            }
        }

        if (bIsNeedLoadCfg == true) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(cfgFile);
                props = new Properties();
                props.load(fis);
                propsMap.put(filePath, new Object[]{cfgFile.lastModified(), props});
            } catch (Exception e) {
                System.err.println(ExceptionUtil.getStackTraceAsString(e));
                return "";
            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                } catch (Exception e) {
                    System.err.println(ExceptionUtil.getStackTraceAsString(e));
                }
            }
        }
        return props.getProperty(keyName, "");
    }

    /**
     * 将字符串转为字节数组
     * @param str 源字符串
     * @return 字节数组
     */
    public static byte[] StringToBytes(String str) {
        try {
            if (str == null || str.length() <= 0) {
                return new byte[0];
            } else {
                return str.getBytes(Constant.CFG_CHARSET_NAME);
            }
        } catch (Exception e) {
            System.err.println(ExceptionUtil.getStackTraceAsString(e));
        }
        return null;
    }
}
