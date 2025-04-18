package org.codenova.moneylog.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codenova.moneylog.entity.User;
import org.codenova.moneylog.entity.Verification;
import org.codenova.moneylog.repository.UserRepository;
import org.codenova.moneylog.repository.VerificationRepository;
import org.codenova.moneylog.request.FindPasswordRequest;
import org.codenova.moneylog.request.LoginRequest;
import org.codenova.moneylog.service.KakaoApiService;
import org.codenova.moneylog.service.MailSendService;
import org.codenova.moneylog.service.NaverApiService;
import org.codenova.moneylog.vo.KakaoTokenResponse;
import org.codenova.moneylog.vo.NaverProfileResponse;
import org.codenova.moneylog.vo.NaverTokenResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@AllArgsConstructor
@RequestMapping("/auth")
@Slf4j
public class AuthController {
    private final NaverApiService naverApiService;
    private final VerificationRepository verificationRepository;
    private KakaoApiService kakaoApiService;
    private UserRepository userRepository;
    private MailSendService mailSendService;



    @GetMapping("/login")
    public String loginHandle(Model model) {
        //log.info("loginHandle...executed");

        model.addAttribute("kakaoClientId", "9663da44d048e888ca60dc1fc7f814c8");
        model.addAttribute("kakaoRedirectUri", "http://192.168.10.158:8080/auth/kakao/callback");

        model.addAttribute("naverClientId", "oGXNFfVotH6T8VMYbaVa");
        model.addAttribute("naverRedirectUri", "http://192.168.10.158:8080/auth/naver/callback");

        return "auth/login";
    }

    @PostMapping("/login")
    public String loginPostHandle(@ModelAttribute LoginRequest loginRequest,
                                  HttpSession session,
                                  Model model) {
        User user = userRepository.findByEmail(loginRequest.getEmail());
        if (user != null && user.getPassword().equals(loginRequest.getPassword())) {
            session.setAttribute("user", user);
            return "redirect:/index";
        } else {
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/signup")
    public String signupGetHandle(Model model) {
        LocalDateTime.now().plusMinutes(30);

        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signupPostHandle(@ModelAttribute User user) {
        User found = userRepository.findByEmail(user.getEmail());
        if (found == null) {
            user.setProvider("LOCAL");
            user.setVerified("F");
            userRepository.save(user);
            mailSendService.sendWelcomeMessage(user);
        }
        return "redirect:/index";
    }


    @GetMapping("/find-password")
    public String findPasswordHandle(Model model) {
        return "auth/find-password";
    }


    @PostMapping("/find-password")
    public String findPasswordPostHandle(@ModelAttribute @Valid FindPasswordRequest req,
                                         BindingResult result,
                                         Model model) {

        if (result.hasErrors()) {
            model.addAttribute("error", "이메일 형식이 아닙니다.");
            return "auth/find-password-error";
        }

        User found = userRepository.findByEmail(req.getEmail());
        if (found == null) {
            model.addAttribute("error", "해당 이메일로 임시번호를 전송할 수 없습니다.");
            return "auth/find-password-error";
        }

        String temporalPassword = UUID.randomUUID().toString().substring(0, 8);
        userRepository.updatePasswordByEmail(req.getEmail(), temporalPassword);
        mailSendService.sendTemporalPasswordMessage(req.getEmail(), temporalPassword);

        return "";
    }


    @GetMapping("/email-check")
    public String emailVerify() {
        return "auth/email-check";
    }

    @PostMapping("/email-create-verification")
    public String emailVerifyPost(@RequestParam("email") String email, Model model){

        User user = userRepository.findByEmail(email);
        if (user == null) {
            model.addAttribute("error" + "이메일이 틀립니다.");
            return "auth/email-check";
        }

        String result = UUID.randomUUID().toString();

        Verification verification = new Verification();
        verification.setToken(result);
        verification.setUserEmail(user.getEmail());

        verificationRepository.create(verification);

        mailSendService.sendEmailVerify(verification.getUserEmail(), verification.getToken());

        return "redirect:/auth/login";
    }

    @GetMapping("/email-verify/{email}/{token}")
    public String emailVerifyCheck(@PathVariable("email") String email, @PathVariable("token") String token) {

        User user = userRepository.findByEmail(email);
        Verification verification = verificationRepository.selectByToken(token);
        if (user.getEmail().equals(verification.getUserEmail())) {
            userRepository.updateUserVerified(email);
        }

        return "auth/email-verify-check";
    }






    @GetMapping("/kakao/callback")
    public String kakaoCallbackHandle(@RequestParam("code") String code,
                                      HttpSession session) throws JsonProcessingException {
        //log.info("code = {}", code);
        KakaoTokenResponse response = kakaoApiService.exchangeToken(code);
        //log.info("response.idToken = {}", response.getIdToken());

        DecodedJWT decodedJWT = JWT.decode(response.getIdToken());

        String sub = decodedJWT.getClaim("sub").toString();
        String nickname = decodedJWT.getClaim("nickname").toString();
        String picture = decodedJWT.getClaim("picture").toString();

        User found = userRepository.findByProviderAndProviderId("KAKAO", sub);
        if (found != null) {
            session.setAttribute("user", found);
        } else {
            User user = User.builder().provider("KAKAO").
                    providerId(sub).nickname(nickname).picture(picture).verified("T").build();
            userRepository.save(user);
            session.setAttribute("user", user);
        }
        log.info("decodedJWT: sub={}, nickname={}, picture={}", sub, nickname, picture);
        return "redirect:/index";
    }


    // ==============================================naver=====================================================

    @GetMapping("/naver/callback")
    public String naverCallbackHandle(@RequestParam("code") String code,
                                      @RequestParam("state") String state,
                                      HttpSession session) throws JsonProcessingException {

        log.info("code={}, state={}", code, state);

        NaverTokenResponse tokenResponse =
                naverApiService.exchangeToken(code, state);

        log.info("accessToken = {}", tokenResponse.getAccessToken());

        NaverProfileResponse profileResponse
                = naverApiService.exchangeProfile(tokenResponse.getAccessToken());

        log.info("profileResponse id = {}", profileResponse.getId());
        log.info("profileResponse nickname = {}", profileResponse.getNickname());
        log.info("profileResponse profileImage = {}", profileResponse.getProfileImage());


        User found = userRepository.findByProviderAndProviderId("NAVER", profileResponse.getId());
        if (found == null) {
            User user = User.builder()
                    .nickname(profileResponse.getNickname())
                    .provider("NAVER")
                    .providerId(profileResponse.getId())
                    .verified("T")
                    .picture(profileResponse.getProfileImage()).build();

            userRepository.save(user);
            session.setAttribute("user", user);
        } else {
            session.setAttribute("user", found);
        }

        return "redirect:/index";

    }
}
