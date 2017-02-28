package com.zang.api;

import com.zang.api.connectors.SmsConnector;
import com.zang.api.domain.SmsMessage;
import com.zang.api.domain.enums.HttpMethod;
import com.zang.api.domain.enums.SmsDirection;
import com.zang.api.domain.list.SmsMessageList;
import com.zang.api.exceptions.ZangException;
import junit.framework.Assert;
import org.junit.Test;
import org.mockserver.model.Parameter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Iterator;


public class SmsTest extends BaseZangTest {

    @Test
    public void viewSms() throws ZangException, IOException {
        createExpectation("GET", "SMS/Messages/TestSmsSid.json", null, null,
                "sms\\sms.json");
        SmsConnector connector = connectorFactory.getSmsConnector();
        SmsMessage smsMessage = connector.viewSmsMessage("TestSmsSid");
        checkMessage(smsMessage);
    }

    @Test
    public void listSms() throws ZangException, IOException {
        createExpectation("GET", "SMS/Messages.json", null,
                new Parameter[]{
                    new Parameter("To", "+123456"),
                        new Parameter("Page", "0"),
                        new Parameter("PageSize", "10")
                },
                "sms\\smslist.json");
        SmsConnector connector = connectorFactory.getSmsConnector();
        SmsMessageList smsMessages = connector.listSmsMessages("+123456", null, null, null, 0L, 10L);
        int cnt = 0;
        Iterator<SmsMessage> it = smsMessages.iterator();
        SmsMessage fromList = null;
        while(it.hasNext()) {
            cnt++;
            fromList = it.next();
        }
        Assert.assertEquals(2, cnt);
        Assert.assertEquals(new Long(1), smsMessages.getNumpages());
        checkMessage(fromList);

    }

    @Test
    public void sendSms() throws ZangException, IOException {

        createExpectation("POST", "SMS/Messages.json",
                new Parameter[]{
                        new Parameter("To", testParameters.getPhone1()),
                        new Parameter("From", testParameters.getPhone2()),
                        new Parameter("Body", "test from java"),
                        new Parameter("StatusCallbackMethod", "GET"),
                        new Parameter("AllowMultiple", "False")
                }, null,
                "sms\\sms.json");

        SmsConnector connector = connectorFactory.getSmsConnector();
        SmsMessage smsMessage = connector.sendSmsMessage(testParameters.getPhone1(), testParameters.getPhone2(),
                "test from java", "callback.url", HttpMethod.GET, false);
        checkMessage(smsMessage);

    }

    private void checkMessage(SmsMessage msg) {
        Assert.assertEquals("test from java", msg.getBody());
        Assert.assertEquals("sent", msg.getStatus());
        Assert.assertEquals(SmsDirection.OUTBOUND_API, msg.getDirection());
        Assert.assertEquals(new BigDecimal("0.0616"), msg.getPrice());
    }

}
