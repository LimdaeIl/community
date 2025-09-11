package com.community.soap.user.persistence.external.email;

import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Component
@RequiredArgsConstructor
public class MailTemplateRenderer {
    private final SpringTemplateEngine engine;

    public String render(String template, Map<String, Object> model) {
        Context ctx = new Context(Locale.KOREA);
        ctx.setVariables(model);
        return engine.process(template, ctx);
    }
}
