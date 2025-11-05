package com.example.demo.log;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestIdFilter extends OncePerRequestFilter {
    public static final String REQ_ID = "requestId";
    public static final String HDR = "X-Request-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String rid = req.getHeader(HDR);
        if (rid == null || rid.isBlank()) rid = UUID.randomUUID().toString();
        MDC.put(REQ_ID, rid);
        res.setHeader(HDR, rid);
        try {
            chain.doFilter(req, res);
        } finally {
            MDC.remove(REQ_ID);
        }
    }
}
