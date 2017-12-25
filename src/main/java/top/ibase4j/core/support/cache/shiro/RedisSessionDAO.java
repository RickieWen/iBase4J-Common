package top.ibase4j.core.support.cache.shiro;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.SimpleSession;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands.SetOption;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;

import top.ibase4j.core.util.InstanceUtil;
import top.ibase4j.core.util.SerializeUtil;

/**
 * 
 * @author ShenHuaJie
 * @version 2017年12月24日 下午8:53:39
 */
public class RedisSessionDAO extends AbstractSessionDAO {
    private static final String REDIS_SHIRO_SESSION = "IBASE4J-SHIRO-SESSION:";
    private static final int EXPIRE_TIME = 600;
    @Autowired
    private RedisTemplate<Serializable, Serializable> redisTemplate;
    private RedisConnection redisConnection;

    private RedisConnection getRedisConnection() {
        if (redisConnection == null) {
            redisConnection = redisTemplate.getConnectionFactory().getConnection();
        }
        return redisConnection;
    }

    public void update(Session session) throws UnknownSessionException {
        saveSession(session);
    }

    public void delete(Session session) {
        if (session != null) {
            Serializable id = session.getId();
            if (id != null) {
                getRedisConnection().del(buildRedisSessionKey(id));
            }
        }
    }

    public Collection<Session> getActiveSessions() {
        List<Session> list = InstanceUtil.newArrayList();
        Set<byte[]> set = getRedisConnection().keys((REDIS_SHIRO_SESSION + "*").getBytes());
        for (byte[] key : set) {
            list.add(SerializeUtil.deserialize(getRedisConnection().get(key), SimpleSession.class));
        }
        return list;
    }

    public void delete(Serializable sessionId) {
        if (sessionId != null) {
            byte[] sessionKey = buildRedisSessionKey(sessionId);
            getRedisConnection().del(sessionKey);
        }
    }

    protected Serializable doCreate(Session session) {
        Serializable sessionId = generateSessionId(session);
        assignSessionId(session, sessionId);
        saveSession(session);
        return sessionId;
    }

    protected Session doReadSession(Serializable sessionId) {
        byte[] sessionKey = buildRedisSessionKey(sessionId);
        byte[] value = getRedisConnection().get(sessionKey);
        if (value == null) {
            return null;
        }
        Session session = SerializeUtil.deserialize(value, SimpleSession.class);
        return session;
    }

    private void saveSession(Session session) {
        if (session == null || session.getId() == null) throw new UnknownSessionException("session is empty");
        byte[] sessionKey = buildRedisSessionKey(session.getId());
        Long sessionTimeOut = session.getTimeout() / 1000 + EXPIRE_TIME;
        byte[] value = SerializeUtil.serialize(session);
        getRedisConnection().set(sessionKey, value, Expiration.seconds(sessionTimeOut.intValue()), SetOption.UPSERT);
    }

    private byte[] buildRedisSessionKey(Serializable sessionId) {
        return (REDIS_SHIRO_SESSION + sessionId).getBytes();
    }
}
