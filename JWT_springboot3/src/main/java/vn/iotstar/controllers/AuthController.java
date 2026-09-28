package vn.iotstar.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class AuthController {

    @GetMapping({"login", "login.html"})
    public String index() {
        return "login";
    }

    @GetMapping({"user/profile", "profile"})
    public String profile() {
        return "profile";
    }
}
