package com.storage.drive.storagedrive.controller;

import com.storage.drive.storagedrive.model.Users;
import com.storage.drive.storagedrive.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
public class UserController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private AuthorizationService authorizationService;
    private UserService userService;
    private FileService fileService;

    public UserController(AuthorizationService authorizationService, UserService userService, FileService fileService) {
        this.authorizationService = authorizationService;
        this.userService = userService;
        this.fileService = fileService;
    }

    @GetMapping(path = "/")
    public String getIndexPage() {
        return "redirect:/home";
    }

    @GetMapping(path = "/home")
    public String getHomePage(Authentication authentication, Model model) {
        String username = (String) authentication.getPrincipal();

        Long currentUserId = userService.getCurrentUserId();


        if (currentUserId != null) {
            Map<String, Object> userData = new HashMap<>();

            userData.put("fileList", this.fileService.getFilesOfUser(currentUserId));
            userData.put("username", this.userService.getCurrentUsername());

            model.addAllAttributes(userData);

            return "home";
        } else {
            return "redirect:/login";
        }
    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "login";
    }


    @GetMapping("/signup")
    public String getSignupPage() {
        return "signup";
    }

    @PostMapping("/signup")
    public String signUpUser(@ModelAttribute("users") Users users, Model model) {
        String signUpError = null;

        if (!userService.isUsernameAvailable(users.getUsername())) {
            signUpError = "Sorry, Username is already taken!";
            logger.warn(String.format("Username %s, is already taken", users.getUsername()));
        }

        if (signUpError == null) {
            int userAdded = userService.addUser(users);
            if (userAdded != 1) {
                signUpError = "Error in signing up. Please try again";
                logger.warn("Couldn't sign up for user: " + users.getUsername());
            }
        }

        if (signUpError == null) {
            model.addAttribute("signUpSuccess", true);
            return "login";
        } else {
            model.addAttribute("signUpError", true);
        }

        return "signup";
    }
}
