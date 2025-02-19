package io.realworld.security.infrastructure

import io.realworld.security.domain.JwtService
import io.realworld.user.domain.UserReadRepository
import io.realworld.user.infrastructure.UserConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@ComponentScan(basePackages = ["io.realworld.security"])
@EnableWebSecurity
@Import(UserConfiguration::class)
class SecurityConfiguration(private val userReadRepository: UserReadRepository,
                            private val jwtService: JwtService) : WebSecurityConfigurerAdapter() {

    @Bean
    fun jwtTokenFilter() = JwtTokenFilter(userReadRepository, jwtService)

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.csrf().disable().cors()
                .and()
                    .exceptionHandling().authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .and()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
                    .antMatchers(HttpMethod.OPTIONS).permitAll()
                    .antMatchers(HttpMethod.GET, "/articles/feed").authenticated()
                    .antMatchers(HttpMethod.POST, "/users", "/users/login").permitAll()
                    .antMatchers(HttpMethod.GET, "/articles/**", "/profiles/**", "/tags").permitAll()
                .anyRequest().authenticated()

        http.addFilterBefore(jwtTokenFilter(), UsernamePasswordAuthenticationFilter::class.java)
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = listOf("*")
            allowedMethods = listOf("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH")
            allowCredentials = true
            allowedHeaders = listOf("Authorization", "Cache-Control", "Content-Type")
        }
        return  UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }
}
