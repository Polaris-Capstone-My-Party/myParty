package com.myParty;


import com.myParty.services.UserDetailsLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private UserDetailsLoader membersLoader;

    public SecurityConfiguration(UserDetailsLoader membersLoader) {
        this.membersLoader = membersLoader;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(membersLoader) // How to find users by their username
                .passwordEncoder(passwordEncoder()) // How to encode and verify passwords
        ;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                /* Login configuration */
                .formLogin()
                    .loginPage("/login")
                    .defaultSuccessUrl("/profile") // user's home page, it can be any URL USER PROFILE PAGES
                    .permitAll() // Anyone can go to the login page
                /* Logout configuration */
                .and()
                    .logout()
                    .logoutSuccessUrl("/login?logout") // append a query string value
                /* Pages that can be viewed without having to log in */
                .and()
                    .authorizeRequests()
                    .antMatchers("/", "/{party_id}/party", "/rsvp/{urlKey}", "/parties/notFound") // anyone can see the homepage and the party page
                    .permitAll()
                /* Pages that require authentication */
                .and()
                    .authorizeRequests()
                    .antMatchers(
                        "/parties/create", "/parties/success/{urlKey}", "/parties/{urlKey}", // only authenticated users can create parties
                        "/parties/edit/{id}", "/parties/delete/{id}", // only authenticated users can edit parties
                            "/profile", "/rsvp/{urlKey}/login", "/member/{urlKey}/view", "/members/editProfile/{id}", "/member/delete/{id}", "/rsvp/{urlKey}/{memberId}",
                            "/member/successRsvp/{urlKey}/{partyMemberKey}", "/rsvp/{urlKey}/member/{partyMemberKey}/view", "/rsvp/{urlKey}/member/{partyMemberKey}/edit"
                )
                    .authenticated()
        ;
    }
}