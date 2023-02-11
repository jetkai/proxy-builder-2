package pe.proxy.proxybuilder2.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

/**
 * SecurityConfig
 *
 * Allows access to mapped directories, specified in the below functions
 * @see filterChain, configure
 *
 * @author Kai
 * @version 1.0, 15/05/2022
 */
@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun filterChain(http : HttpSecurity) : SecurityFilterChain {
        http.authorizeHttpRequests {
                auth -> auth.requestMatchers("*")
            .permitAll().anyRequest().authenticated()
        }.httpBasic(withDefaults()).csrf().disable()
        return http.build()
    }

    @Bean
    fun configure(http : HttpSecurity): SecurityFilterChain {
        http.authorizeHttpRequests {
                auth -> auth.requestMatchers("*")
            .permitAll().anyRequest().authenticated()
        }.httpBasic(withDefaults())
            .csrf().disable()
        return http.build()
    }

}
/* Deprecated in 3.0.0
class SecurityConfig : WebSecurityConfigurerAdapter() {

    override fun configure(http : HttpSecurity) {
        http.authorizeRequests()
            .antMatchers("/*").permitAll()
            .antMatchers("/api/v1/*").permitAll()
            .anyRequest().authenticated()
            //Disable CSRF (TEMP)
            //.and().csrf().disable()
            .and().csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
    }

}*/