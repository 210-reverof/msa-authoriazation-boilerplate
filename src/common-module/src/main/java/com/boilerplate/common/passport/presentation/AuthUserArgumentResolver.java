package com.boilerplate.common.passport.presentation;

import com.boilerplate.common.passport.application.PassportProvider;
import com.boilerplate.common.passport.exception.InvalidPassportException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class AuthUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final PassportProvider passportProvider;

    public AuthUserArgumentResolver(PassportProvider passportProvider) {
        this.passportProvider = passportProvider;
    }

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthUser.class);
    }

    @Override
    public Object resolveArgument(final MethodParameter parameter, final ModelAndViewContainer mavContainer,
                                  final NativeWebRequest webRequest, final WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        String passport = extractPassport(request);
        return passportProvider.getMemberInfo(passport);
    }

    private String extractPassport(HttpServletRequest request) {
        String passportHeader = request.getHeader("Passport");
        if (passportHeader == null || passportHeader.isEmpty()) {
            throw new InvalidPassportException("잘못된 패스포트");
        }
        return passportHeader;
    }
}
