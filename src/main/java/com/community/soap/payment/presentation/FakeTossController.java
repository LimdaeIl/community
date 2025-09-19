package com.community.soap.payment.presentation;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/_fake_toss/v1/payments")
public class FakeTossController {

    @PostMapping(value = "/confirm", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> confirm(@RequestBody Map<String, Object> body) {
        // body: { paymentKey, orderId, amount }
        String paymentKey = String.valueOf(body.get("paymentKey"));
        String orderId = String.valueOf(body.get("orderId"));
        Integer amount = (body.get("amount") == null) ? null : (Integer) body.get("amount");

        Map<String, Object> res = new HashMap<>();
        res.put("paymentKey", paymentKey);
        res.put("orderId", orderId);
        res.put("approvedAmount", amount);
        res.put("status", "DONE");
        res.put("method", "CARD");
        res.put("approvedAt", OffsetDateTime.now().toString());
        res.put("receiptUrl", "https://example/receipt/" + paymentKey);
        return res;
    }

    @PostMapping(value = "/{paymentKey}/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> cancel(@PathVariable String paymentKey,
            @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> res = new HashMap<>();
        res.put("paymentKey", paymentKey);
        res.put("status", "CANCELED");
        return res;
    }
}
