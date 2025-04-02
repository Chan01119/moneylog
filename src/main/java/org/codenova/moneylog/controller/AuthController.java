package org.codenova.moneylog.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codenova.moneylog.entity.User;
import org.codenova.moneylog.repository.UserRepository;
import org.codenova.moneylog.request.LoginRequest;
import org.codenova.moneylog.service.KakaoApiService;
import org.codenova.moneylog.service.NaverApiService;
import org.codenova.moneylog.vo.KakaoTokenResponse;
import org.codenova.moneylog.vo.NaverProfileResponse;
import org.codenova.moneylog.vo.NaverTokenResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@Controller
@AllArgsConstructor
@RequestMapping("/auth")
@Slf4j
public class AuthController {
    private final NaverApiService naverApiService;
    private KakaoApiService kakaoApiService;
    private UserRepository userRepository;

    @GetMapping("/login")
    public String loginHandle(Model model) {
        //log.info("loginHandle...executed");

        model.addAttribute("kakaoClientId", "9663da44d048e888ca60dc1fc7f814c8");
        model.addAttribute("kakaoRedirectUri", "http://192.168.10.30:8080/auth/kakao/callback");

        model.addAttribute("naverClientId", "oGXNFfVotH6T8VMYbaVa");
        model.addAttribute("naverRedirectUri", "http://192.168.10.30:8080/auth/naver/callback");

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

        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signupPostHandle(@ModelAttribute User user) {
        User found = userRepository.findByEmail(user.getEmail());
        if (found == null) {
            user.setProvider("LOCAL");
            user.setVerified("F");
            userRepository.save(user);
        }
        return "redirect:/index";
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
        return "redirect:/";
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
        }else {
            session.setAttribute("user", found);
        }

        return "redirect:/";

    }
}
