package org.mars_sim.msp.restws;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

/**
 * This filter removes any problem with CORS from the web clients.
 * It changes the response header in the chain so it is not blocked later.
 * @author barryeva
 *
 */
@Component
public class CORSResponseFilter implements Filter {

	private Log log = LogFactory.getLog(CORSResponseFilter.class);
	
    @Override
    public void init(FilterConfig arg0) throws ServletException {
        log.info("Filter init " + arg0);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp,
            FilterChain chain) throws IOException, ServletException {

        HttpServletResponse response=(HttpServletResponse) resp;
        
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");

        chain.doFilter(req, resp);
    }

    @Override
    public void destroy() {}

}