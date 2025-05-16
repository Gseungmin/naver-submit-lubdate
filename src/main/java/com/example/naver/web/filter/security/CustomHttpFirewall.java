package com.example.naver.web.filter.security;

import com.example.naver.domain.service.BlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.firewall.FirewalledRequest;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.security.web.firewall.StrictHttpFirewall;

import static com.example.naver.web.util.FunctionUtil.getClientIp;

@RequiredArgsConstructor
public class CustomHttpFirewall extends StrictHttpFirewall {

    private final BlacklistService blacklistService;

    @Override
    public FirewalledRequest getFirewalledRequest(HttpServletRequest request) throws RequestRejectedException {
        try {
            return super.getFirewalledRequest(request);
        } catch (RequestRejectedException ex) {
            String clientIp = getClientIp(request);
            blacklistService.addToBlacklist(clientIp);
            throw ex;
        }
    }
}