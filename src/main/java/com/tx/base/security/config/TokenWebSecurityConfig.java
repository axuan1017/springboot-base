package com.tx.base.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Security核心配置类
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class TokenWebSecurityConfig extends WebSecurityConfigurerAdapter {

    private com.tx.base.security.security.TokenManager tokenManager;
    private RedisTemplate redisTemplate;
    private com.tx.base.security.security.DefaultPasswordEncoder defaultPasswordEncoder;
    private UserDetailsService userDetailsService;
    //使用BC加密
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * 使用 自定义密码加密类
     * @param userDetailsService
     * @param defaultPasswordEncoder
     * @param tokenManager
     * @param redisTemplate
     */
    //@Autowired
    //public TokenWebSecurityConfig(UserDetailsService userDetailsService, com.tx.base.security.security.DefaultPasswordEncoder defaultPasswordEncoder,
    //                              com.tx.base.security.security.TokenManager tokenManager, RedisTemplate redisTemplate) {
    //    this.userDetailsService = userDetailsService;
    //    this.defaultPasswordEncoder = defaultPasswordEncoder;
    //    this.tokenManager = tokenManager;
    //    this.redisTemplate = redisTemplate;
    //}

    /**
     * 配置使用认证的类
     * @param userDetailsService    自定义UserService
     * @param bCryptPasswordEncoder 自定义密码加密器
     * @param tokenManager          自定义token管理器
     * @param redisTemplate         自定义redis管理器
     */
    @Autowired
    public TokenWebSecurityConfig(UserDetailsService userDetailsService, BCryptPasswordEncoder bCryptPasswordEncoder,
                                  com.tx.base.security.security.TokenManager tokenManager, RedisTemplate redisTemplate) {
        this.userDetailsService = userDetailsService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.tokenManager = tokenManager;
        this.redisTemplate = redisTemplate;
    }


    /**
     * 配置设置
     * @param http
     * @throws Exception
     */
    //设置退出的地址和token，redis操作地址
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.exceptionHandling()
                .authenticationEntryPoint(new com.tx.base.security.security.UnauthEntryPoint())//没有权限访问
                .and().csrf().disable()
                .authorizeRequests()
                //.antMatchers("/swagger-resources","/v2/api-docs").permitAll()  //放行swagger路径
                .anyRequest().authenticated()
                //.and().formLogin().loginProcessingUrl("/index/login")
                .and().logout().logoutUrl("/index/logout")//退出路径
                .addLogoutHandler(new com.tx.base.security.security.TokenLogoutHandler(tokenManager, redisTemplate))
                .and()
                .addFilter(new com.tx.base.security.filter.TokenLoginFilter(authenticationManager(), tokenManager, redisTemplate))//添加自定义过滤器
                .addFilter(new com.tx.base.security.filter.TokenAuthFilter(authenticationManager(), tokenManager, redisTemplate))
                .httpBasic();
    }

    /**
     * 调用userDetailsService和密码处理
     * @param auth
     * @throws Exception
     */
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
    }

    /**
     * 不进行认证的路径，可以直接访问
     * @param web
     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/swagger-resources","/v2/api-docs", "/webjars/**", "/index/unAuth", "/doc.html","/admin/user/save");
    }
}
