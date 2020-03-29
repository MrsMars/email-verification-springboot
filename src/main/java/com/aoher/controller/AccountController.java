package com.aoher.controller;

import com.aoher.model.VerificationForm;
import com.aoher.service.VerificationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;

@Controller
public class AccountController {

    private static final String ATTRIBUTE_VERIFICATION_FORM = "verificationForm";
    private static final String ATTRIBUTE_NO_ERRORS = "noErrors";

    @Autowired
    private VerificationTokenService verificationTokenService;

    @GetMapping("/")
    public String index() {
        return "redirect:/email-verification";
    }

    @GetMapping("/email-verification")
    public String fromGet(Model model) {
        model.addAttribute(ATTRIBUTE_VERIFICATION_FORM, new VerificationForm());
        return "verification-form";
    }

    @PostMapping("/email-verification")
    public String formPost(@Valid VerificationForm verificationForm, BindingResult bindingResult, Model model) {
        if (!bindingResult.hasErrors()) {
            model.addAttribute(ATTRIBUTE_NO_ERRORS, true);
        }
        model.addAttribute(ATTRIBUTE_VERIFICATION_FORM, verificationForm);

        verificationTokenService.createVerification(verificationForm.getEmail());
        return "verification-form";
    }

    @GetMapping("/verify-email")
    @ResponseBody
    public String verifyEmail(String code) {
        return verificationTokenService.verifyEmail(code).getBody();
    }
}
