package com.example.naver.web.config;

import com.example.naver.domain.service.BlacklistService;
import com.example.naver.web.filter.security.CustomHttpFirewall;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.firewall.HttpFirewall;

@Configuration
public class FirewallConfig {

    @Bean
    public HttpFirewall httpFirewall(BlacklistService blacklistService) {
        CustomHttpFirewall firewall = new CustomHttpFirewall(blacklistService);
        firewall.setAllowSemicolon(false);
        firewall.setAllowUrlEncodedSlash(false);
        firewall.setAllowUrlEncodedDoubleSlash(false);
        firewall.setAllowBackSlash(false);
        firewall.setAllowUrlEncodedPeriod(false);
        firewall.setAllowUrlEncodedPercent(false);
        return firewall;
    }
}