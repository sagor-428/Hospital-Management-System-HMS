package com.hospital;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register(Model model) {

        model.addAttribute(
                "registrationRequest",
                new RegistrationRequest()
        );

        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid
            @ModelAttribute("registrationRequest")
            RegistrationRequest request,
            BindingResult result) {

        if (result.hasErrors()) {
            return "auth/register";
        }

        try {

            userService.registerPatient(request);

        } catch (RuntimeException e) {

            result.rejectValue(
                    "email",
                    "error.email",
                    e.getMessage()
            );

            return "auth/register";
        }

        return "redirect:/login?registered=true";
    }
}