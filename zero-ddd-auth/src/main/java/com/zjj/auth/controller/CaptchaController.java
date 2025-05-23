package com.zjj.auth.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ShearCaptcha;
import com.zjj.autoconfigure.component.core.ResponseEntity;
import com.zjj.security.captcha.component.spi.CaptchaService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.UUID;

/**
 * @author zengJiaJun
 * @version 1.0
 * @crateTime 2024年12月30日 21:41
 */
@RestController
@RequestMapping("/auth/v1/captcha")
public class CaptchaController {

    @Autowired
    private CaptchaService captchaService;


//    @GetMapping
//    public Response<CaptchaVO> captcha(HttpServletResponse response) throws IOException {
//        SpecCaptcha gifCaptcha = new SpecCaptcha(90, 30, 2);
//
//        // 设置字体
//        //gifCaptcha.setFont();  // 有默认字体，可以不用设置
//
//        // 设置类型，纯数字、纯字母、字母数字混合
//        gifCaptcha.setCharType(Captcha.TYPE_ONLY_NUMBER);
//
//        // 生成的验证码
////        String img = gifCaptcha.text();
//        String img = gifCaptcha.base64(new ByteArrayOutputStream());
//        String captchaKey = UUID.randomUUID().toString();
//        captchaService.putCaptcha(captchaKey, gifCaptcha.text());
//        return Response.success(new CaptchaVO(captchaKey, img));
//
//    }

    @GetMapping
    public ResponseEntity<CaptchaVO> captcha(HttpServletResponse response) throws IOException {
        ShearCaptcha captcha = CaptchaUtil.createShearCaptcha(80, 40, 4, 4);
        String captchaKey = UUID.randomUUID().toString();
        captchaService.putCaptcha(captchaKey, captcha.getCode());
        return ResponseEntity.ok(new CaptchaVO(captchaKey, captcha.getImageBase64()));
    }
}
