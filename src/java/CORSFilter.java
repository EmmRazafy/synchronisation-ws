
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author P12A-92-Emmanuel
 */
public class CORSFilter implements Filter{
    /**
     * Default constructor.
     */
    public CORSFilter() {
      // TODO Auto-generated constructor stub
    }
  
  /**
     * @param fConfig
   * @see Filter#init(FilterConfig)
   */
    @Override
    public void init(FilterConfig fConfig) throws ServletException {
      // TODO Auto-generated method stub
    }

    /**
     * Authorize (allow) all domains to consume the content
     * @param response 
     */
    private static void addCorsHeader(HttpServletResponse response){
        //TODO: externalize the Allow-Origin
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD");
        response.addHeader("Access-Control-Allow-Headers", "X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept");
        response.addHeader("Access-Control-Max-Age", "1728000");
    }

  /**
   * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
   */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
        throws IOException, ServletException {

      HttpServletRequest request = (HttpServletRequest) servletRequest;
      //System.out.println("CORSFilter HTTP Request: " + request.getMethod());

      
      addCorsHeader((HttpServletResponse) servletResponse);
      HttpServletResponse resp = (HttpServletResponse) servletResponse;

      // For HTTP OPTIONS verb/method reply with ACCEPTED status code -- per CORS handshake
      if (request.getMethod().equals("OPTIONS")) {
        resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        return;
      }

      // pass the request along the filter chain
      chain.doFilter(request, servletResponse);
    }

    /**
     * @see Filter#destroy()
     */
    @Override
    public void destroy() {
      // TODO Auto-generated method stub
    }
    
}
