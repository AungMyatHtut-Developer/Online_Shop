package com.technortal.online_shop.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class LoginControllerTests {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new LoginController(), new ProductController())
                .setViewResolvers(new InternalResourceViewResolver("/templates/", ".html"))
                .build();
    }

    @Test
    void visitorsSeeLoginAtRootAndLoginPath() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("login"));
        mockMvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("login"));
    }

    @Test
    void productsRequireLoginEvenWithAnAnonymousSession() throws Exception {
        mockMvc.perform(get("/products")).andExpect(redirectedUrl("/login"));
        mockMvc.perform(get("/products").session(new MockHttpSession()))
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void validCredentialsAllowProductsAndRotateTheSessionId() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String originalSessionId = session.getId();

        mockMvc.perform(post("/login").session(session)
                        .param("username", "admin").param("password", "1234"))
                .andExpect(redirectedUrl("/products"))
                .andExpect(request().sessionAttribute(LoginController.LOGGED_IN_USER, "admin"));

        assertNotEquals(originalSessionId, session.getId());
        mockMvc.perform(get("/products").session(session)).andExpect(view().name("index"));
        mockMvc.perform(get("/login").session(session)).andExpect(redirectedUrl("/products"));
        mockMvc.perform(get("/").session(session)).andExpect(redirectedUrl("/products"));
    }

    @Test
    void wrongPasswordShowsAnErrorAndDoesNotGrantAccess() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/login").session(session)
                        .param("username", "admin").param("password", "wrong"))
                .andExpect(view().name("login"))
                .andExpect(model().attribute("error", "Invalid username or password."))
                .andExpect(model().attribute("username", "admin"))
                .andExpect(request().sessionAttributeDoesNotExist(LoginController.LOGGED_IN_USER));
        mockMvc.perform(get("/products").session(session)).andExpect(redirectedUrl("/login"));
    }

    @Test
    void wrongUsernameIsRejected() throws Exception {
        mockMvc.perform(post("/login").param("username", "student").param("password", "1234"))
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("error"))
                .andExpect(request().sessionAttributeDoesNotExist(LoginController.LOGGED_IN_USER));
    }

    @Test
    void missingCredentialsAreRejected() throws Exception {
        mockMvc.perform(post("/login"))
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("error"))
                .andExpect(request().sessionAttributeDoesNotExist(LoginController.LOGGED_IN_USER));
    }

    @Test
    void logoutEndsTheAuthenticatedSession() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(LoginController.LOGGED_IN_USER, "admin");
        mockMvc.perform(post("/logout").session(session)).andExpect(redirectedUrl("/login"));
        assertTrue(session.isInvalid());
        mockMvc.perform(get("/products")).andExpect(redirectedUrl("/login"));
    }

    @Test
    void logoutWithoutASessionReturnsToLogin() throws Exception {
        mockMvc.perform(post("/logout")).andExpect(redirectedUrl("/login"));
    }
}
