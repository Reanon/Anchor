package com.reanon.community;

import com.reanon.community.utils.MailClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


/**
 * @author reanon
 * @create 2021-07-02
 */
@SpringBootTest
public class MailTest {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    @DisplayName("测试发送邮件")
    public void testTextMail() {
        mailClient.sendMail("793584285@qq.com", "Test", "Test mail send");
    }

    @Test
    @DisplayName("测试HTML邮件")
    public void testHtmlMail() {
        Context context = new Context();
        context.setVariable("username", "Alice");

        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);

        mailClient.sendMail("793584285@qq.com", "HTMLTest", content);
    }
}
