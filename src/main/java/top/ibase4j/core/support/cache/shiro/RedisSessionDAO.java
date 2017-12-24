package top.ibase4j.core.support.cache.shiro;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;

import com.alibaba.fastjson.JSON;

import top.ibase4j.core.util.CacheUtil;
import top.ibase4j.core.util.InstanceUtil;

/**
 * 
 * @author ShenHuaJie
 * @version 2017年12月24日 下午8:53:39
 */
public class RedisSessionDAO extends AbstractSessionDAO {
    private static final String REDIS_SHIRO_SESSION = "IBASE4J-SHIRO-SESSION:";
    private static final int EXPIRE_TIME = 600;

    public void update(Session session) throws UnknownSessionException {
        saveSession(session);
    }

    public void delete(Session session) {
        if (session == null) {
            return;
        }
        Serializable id = session.getId();
        if (id != null) {
            CacheUtil.getCache().del(buildRedisSessionKey(id));
        }
    }

    public Collection<Session> getActiveSessions() {
        List<Session> list = InstanceUtil.newArrayList();
        Set<Object> set = CacheUtil.getCache().getAll(REDIS_SHIRO_SESSION + "*");
        for (Object object : set) {
            list.add(JSON.parseObject((String)object, Session.class));
        }
        return list;
    }

    protected Serializable doCreate(Session session) {
        Serializable sessionId = this.generateSessionId(session);
        this.assignSessionId(session, sessionId);
        saveSession(session);
        return sessionId;
    }

    protected Session doReadSession(Serializable sessionId) {
        String sessionKey = buildRedisSessionKey(sessionId);
        String value = (String)CacheUtil.getCache().get(sessionKey);
        return JSON.parseObject(value, Session.class);
    }

    private void saveSession(Session session) {
        if (session == null || session.getId() == null) throw new NullPointerException("session is empty");
        String sessionKey = buildRedisSessionKey(session.getId());
        String value = JSON.toJSONString(session);
        Long sessionTimeOut = session.getTimeout() / 1000 + EXPIRE_TIME;
        CacheUtil.getCache().set(sessionKey, value, sessionTimeOut.intValue());
    }

    private String buildRedisSessionKey(Serializable sessionId) {
        return REDIS_SHIRO_SESSION + sessionId;
    }
}
