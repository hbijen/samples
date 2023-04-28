package com.example.authserver.web;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.authserver.entity.AppUser;
import com.example.authserver.enums.RegisterWith;
import com.example.authserver.model.SignupModel;
import com.example.authserver.repository.AppUserRepository;
import com.example.authserver.service.EmailService;
import com.example.authserver.service.SMSService;

import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequestMapping("/auth")
public class LoginController {

	@Autowired
	@Qualifier("signupValidator")
    private Validator signupValidator;

	@Autowired
    private EmailService emailService;

	@Autowired
    private SMSService smsService;	
	
	@Autowired
    private AppUserRepository appUserRepo;

	@Autowired
	private CacheManager cacheManager;

	@InitBinder
	private void initBinder(WebDataBinder binder) {
		binder.setValidator(signupValidator);
	}	

    @GetMapping("/login")
	public String loginpage() {
		return "login";
	}

	@GetMapping("/register-with-email")
	public String signupWithEmail(Model model) {
		final SignupModel signupModel = new SignupModel();
		signupModel.setRegisterWith(RegisterWith.email);
		model.addAttribute("signupModel", signupModel);
		return "register";
	}
	@GetMapping("/register-with-phoneno")
	public String signupWithPhone(Model model) {
		final SignupModel signupModel = new SignupModel();
		signupModel.setRegisterWith(RegisterWith.sms);
		model.addAttribute("signupModel", signupModel);

		return "register";
	}
	
	@PostMapping("/register")
	public String register(
		@ModelAttribute("signupModel") SignupModel signupModel,
		BindingResult result,
		Model model) {

		try {
			//add the model so that the input values are retain when the error message is displayed in the same page
			model.addAttribute("signupModel", signupModel);

			// input validation of email or phone
			signupValidator.validate(signupModel, result);
			if (result.hasErrors()) {
				return "register";
			}
			AppUser appUser = null;
			if (signupModel.getRegisterWith() == RegisterWith.email) {
				appUser = appUserRepo.findByEmail(signupModel.getEmail());
			} else if (signupModel.getRegisterWith() == RegisterWith.sms) {
				appUser = appUserRepo.findByEmail(signupModel.getPhone());
			}			

			//a new registration is saved as an app user with pending verification status
			if (appUser == null) {
				appUser = AppUser.from(signupModel);
				appUserRepo.save(appUser);
			}

			sendVerifyCode(signupModel);
			model.addAttribute("code_sent", true);

		} catch (Exception ex) {
			log.error("", ex);
			model.addAttribute("error", ex.getMessage());
			return "register";
		}

		return "registration-verify";
	}

	private String generateOTP() {
		Random rand = new Random();
		return String.valueOf(rand.nextInt(900000) + 100000); 
	}

	private void sendVerifyCode(SignupModel signupModel) throws MessagingException {
		//send verify code in sms or email
		String otp = generateOTP();

		if (signupModel.getRegisterWith() == RegisterWith.email) {
			cacheManager.getCache("otp").put(signupModel.getEmail(), otp);
			emailService.sendEmailVerifyCode(signupModel.getEmail(), "", otp);
		} else if (signupModel.getRegisterWith() == RegisterWith.sms) {
			cacheManager.getCache("otp").put(signupModel.getPhone(), otp);
			String otpMessage = String.format("Your verification code is %s.", otp);
			smsService.sendOtp(signupModel.getPhone(), otpMessage);
		}
	}

	@PostMapping("/resend-code")
	public String resendCode(
		@ModelAttribute("signupModel") SignupModel signupModel,
		Model model) {
		model.addAttribute("signupModel", signupModel);
		try {
			sendVerifyCode(signupModel);
			model.addAttribute("code_sent", true);
		} catch (Exception ex) {
			log.error("", ex);
			model.addAttribute("error", ex.getMessage());
		}
		return "registration-verify";
	}

	@PostMapping("/register-confirm")
	public String confirm(@ModelAttribute("signupModel") SignupModel signupModel, Model model) {
		
		model.addAttribute("signupModel", signupModel);
		String otp = "";
		if (signupModel.getRegisterWith() == RegisterWith.email) {
			otp = cacheManager.getCache("otp").get(signupModel.getEmail(), String.class);
		} else if (signupModel.getRegisterWith() == RegisterWith.sms) {
			otp = cacheManager.getCache("otp").get(signupModel.getPhone(), String.class);
		}
		
		
		log.debug("otp " + otp + ", entered code " + signupModel.getVerifyCode());

		if (otp == null) {
			model.addAttribute("error", "Code expired");
			return "registration-verify";
		}
		if (!signupModel.getVerifyCode().equals(otp)) {
			model.addAttribute("error", "Code invalid");
			return "registration-verify";
		}

		if (signupModel.getRegisterWith() == RegisterWith.sms) {
			//TODO: upon otp verification redirect to homepage
			return "registration-confirm";
		}

		//for email registration flow
		return "registration-confirm";
	}

}
