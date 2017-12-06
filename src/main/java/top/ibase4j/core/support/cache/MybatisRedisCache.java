/**
 * 
 */
package top.ibase4j.core.support.cache;

import java.io.Serializable;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.ibatis.cache.Cache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import top.ibase4j.core.Constants;
import top.ibase4j.core.support.context.ApplicationContextHolder;
import top.ibase4j.core.util.PropertiesUtil;

/**
 * 
 * @author ShenHuaJie
 * @version 2017年12月6日 上午10:29:20
 */
public class MybatisRedisCache implements Cache {
    private final Logger logger = LogManager.getLogger();
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    @SuppressWarnings("unchecked")
    private RedisTemplate<Serializable, Serializable> redisTemplate = (RedisTemplate<Serializable, Serializable>)ApplicationContextHolder
        .getBean("redisTemplate");

    private String id;

    public MybatisRedisCache(final String id) {
        if (id == null) {
            throw new IllegalArgumentException("Cache instances require an ID");
        }
        logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>MybatisRedisCache:id=" + id);
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    public void putObject(Object key, Object value) {
        logger.debug(">>>>>>>>>>>>>>>>>>>>>>>>putObject:" + key + "=" + value);
        final String keyf = Constants.CACHE_NAMESPACE + key.toString();
        final Object valuef = value;
        try {
            redisTemplate.execute(new RedisCallback<Long>() {
                public Long doInRedis(RedisConnection connection) throws DataAccessException {
                    RedisSerializer<Object> serializer = new GenericJackson2JsonRedisSerializer();
                    byte[] keyb = serializer.serialize(keyf);
                    connection.set(keyb, serializer.serialize(valuef));
                    connection.expire(keyb, PropertiesUtil.getInt("redis.expiration"));
                    return 1L;
                }
            });
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    public Object getObject(Object key) {
        Object result = null;
        try {
            final String keyf = Constants.CACHE_NAMESPACE + key.toString();
            result = redisTemplate.execute(new RedisCallback<Object>() {
                public Object doInRedis(RedisConnection connection) throws DataAccessException {
                    RedisSerializer<Object> serializer = new GenericJackson2JsonRedisSerializer();
                    Object value = serializer.deserialize(connection.get(serializer.serialize(keyf)));
                    return value;
                }
            });
        } catch (Exception e) {
            logger.error("", e);
        }
        return result;
    }

    public Object removeObject(Object key) {
        Object result = null;
        try {
            final Object keyf = Constants.CACHE_NAMESPACE + key;
            result = redisTemplate.execute(new RedisCallback<Object>() {
                public Object doInRedis(RedisConnection connection) throws DataAccessException {
                    RedisSerializer<Object> serializer = new GenericJackson2JsonRedisSerializer();
                    byte[] keyb = serializer.serialize(keyf);
                    return connection.del(keyb.toString().getBytes());
                }
            });
        } catch (Exception e) {
            logger.error("", e);
        }
        return result;
    }

    public int getSize() {
        return redisTemplate.execute(new RedisCallback<Integer>() {
            public Integer doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.dbSize().intValue();
            }
        });
    }

    public void clear() {
        redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                connection.flushDb();
                connection.flushAll();
                return Boolean.TRUE;
            }
        });
    }
}
