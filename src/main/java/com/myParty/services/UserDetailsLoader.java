package com.myParty.services;

import com.myParty.models.Member;
import com.myParty.repositories.MemberRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsLoader implements UserDetailsService {
    private final MemberRepository memberDao;

    public UserDetailsLoader(MemberRepository memberDao) {
        this.memberDao = memberDao;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberDao.findByUsername(username);
        if (member == null) {
            throw new UsernameNotFoundException("No user found for " + username);
        }

        return new Member(member);
    }
}