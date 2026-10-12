package com.stockyard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("test")
class SalesFlowTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private JsonNode send(MockHttpServletRequestBuilder req, String token, String body, int expected) throws Exception {
        if (token != null) req.header("Authorization", "Bearer " + token);
        if (body != null) req.contentType(MediaType.APPLICATION_JSON).content(body);
        String res = mvc.perform(req).andExpect(status().is(expected)).andReturn().getResponse().getContentAsString();
        return res.isBlank() ? null : json.readTree(res);
    }

    private String register(String email) throws Exception {
        return send(post("/api/auth/register"), null,
                "{\"email\":\"" + email + "\",\"password\":\"password123\",\"shopName\":\"Test Shop\"}", 201)
                .get("token").asText();
    }

    @Test
    void saleDecrementsStockAtomicallyAndFeedsDashboard() throws Exception {
        String token = register("a@example.com");
        long channelId = send(get("/api/channels"), token, null, 200).get(0).get("id").asLong();

        long productId = send(post("/api/products"), token, """
                {"sku":"MUG-1","title":"Mug","unitCost":4.00,"retailPrice":18.00,"stock":10,"lowStockThreshold":3}
                """, 201).get("id").asLong();

        send(post("/api/sales"), token,
                "{\"productId\":" + productId + ",\"channelId\":" + channelId + ",\"quantity\":8}", 201);

        // Only 2 left: selling 3 must fail and leave stock and ledger untouched.
        send(post("/api/sales"), token,
                "{\"productId\":" + productId + ",\"channelId\":" + channelId + ",\"quantity\":3}", 409);

        JsonNode product = send(get("/api/products"), token, null, 200).get(0);
        assertThat(product.get("stock").asInt()).isEqualTo(2);
        assertThat(product.get("lowStock").asBoolean()).isTrue();
        assertThat(send(get("/api/sales"), token, null, 200)).hasSize(1);

        JsonNode dash = send(get("/api/dashboard"), token, null, 200);
        assertThat(dash.get("revenue").decimalValue()).isEqualByComparingTo("144.00");
        assertThat(dash.get("units").asInt()).isEqualTo(8);
        assertThat(dash.get("profit").decimalValue()).isEqualByComparingTo("112.00");
        assertThat(dash.get("lowStock")).hasSize(1);
    }

    @Test
    void makersCannotSeeEachOthersData() throws Exception {
        String a = register("iso-a@example.com");
        String b = register("iso-b@example.com");
        long productId = send(post("/api/products"), a, """
                {"sku":"X","title":"Secret","unitCost":1,"retailPrice":2,"stock":1}""", 201).get("id").asLong();
        assertThat(send(get("/api/products"), b, null, 200)).isEmpty();
        long bChannel = send(get("/api/channels"), b, null, 200).get(0).get("id").asLong();
        send(post("/api/sales"), b,
                "{\"productId\":" + productId + ",\"channelId\":" + bChannel + ",\"quantity\":1}", 404);
    }

    @Test
    void protectedRoutesRequireToken() throws Exception {
        mvc.perform(get("/api/products")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"nobody@example.com\",\"password\":\"whatever\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.message").exists());
    }
}
