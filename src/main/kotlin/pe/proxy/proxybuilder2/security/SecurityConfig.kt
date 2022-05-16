package pe.proxy.proxybuilder2.security

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

/**
 * SecurityConfig
 *
 * Allows access to mapped directories, specified in the below functions
 * @see configure
 *
 * @author Kai
 * @version 1.0, 15/05/2022
 */
@Configuration
@EnableWebSecurity
class SecurityConfig : WebSecurityConfigurerAdapter() {

    @Throws(Exception::class)
    override fun configure(authentication : AuthenticationManagerBuilder) {
        //authentication.authenticationProvider(authenticationProvider)
    }

    @Throws(Exception::class)
    override fun configure(http : HttpSecurity) {
        http.authorizeRequests()
            .antMatchers("/api/proxype/running").permitAll()
            .anyRequest().authenticated()
            //Disable CSRF (TEMP)
            .and().csrf().disable()
    }
}