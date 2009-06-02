/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.broadleafcommerce.email.test;

import javax.annotation.Resource;

import org.broadleafcommerce.email.service.EmailService;
import org.broadleafcommerce.email.service.info.EmailInfo;
import org.broadleafcommerce.test.integration.BaseTest;
import org.testng.annotations.Test;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

public class EmailTest extends BaseTest {

    @Resource
    EmailService emailService;

    @Test
    public void testSynchronousEmail() throws Exception {
        GreenMail greenMail = new GreenMail(
                new ServerSetup[] {
                        new ServerSetup(30000, "127.0.0.1", ServerSetup.PROTOCOL_SMTP)
                }
        );
        greenMail.start();

        EmailInfo info = new EmailInfo();
        info.setFromAddress("me@test.com");
        info.setSubject("test");
        info.setEmailTemplate("org/broadleafcommerce/email/service/template/default.vm");
        info.setSendEmailReliableAsync("false");

        emailService.sendTemplateEmail("to@localhost", info, null);

        assert(greenMail.waitForIncomingEmail(10000, 1));

        greenMail.stop();
    }

}
