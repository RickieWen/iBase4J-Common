package top.ibase4j.core.filter;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;

import top.ibase4j.core.Constants;
import top.ibase4j.core.util.WebUtil;

/**
 * 国际化信息设置(基于SESSION)
 * 
 * @author ShenHuaJie
 * @version 2016年5月20日 下午3:16:45
 */
public class LocaleFilter implements Filter {
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)request;
        HttpSession session = req.getSession();
        // 设置客户端语言
        Locale locale = (Locale)session.getAttribute("LOCALE");
        if (locale == null) {
            String language = request.getParameter("locale");
            if (StringUtils.isNotBlank(language)) {
                locale = new Locale(language);
                session.setAttribute("LOCALE", locale);
            } else {
                locale = request.getLocale();
            }
        }
        LocaleContextHolder.setLocale(locale);

        session.setAttribute(Constants.USER_IP, WebUtil.getHost(req));
        chain.doFilter(request, response);
    }

    public void destroy() {

    }
}
