package com.example.access_guard.configuration;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KaptchaConfig {

    @Bean
    public DefaultKaptcha kaptchaProducer() {
        Properties props = new Properties();
        props.setProperty("kaptcha.border", "no");
        props.setProperty("kaptcha.textproducer.font.color", "black");
        props.setProperty("kaptcha.textproducer.char.length", "5");
        props.setProperty("kaptcha.textproducer.char.string", "ABCDEFGHJKLMNPQRSTUVWXYZ23456789");
        props.setProperty("kaptcha.image.width", "200");
        props.setProperty("kaptcha.image.height", "70");
        props.setProperty("kaptcha.textproducer.font.size", "40");
        props.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.DefaultNoise");
        props.setProperty("kaptcha.obscurificator.impl", "com.google.code.kaptcha.impl.WaterRipple");


        Config config = new Config(props);
        DefaultKaptcha kaptcha = new DefaultKaptcha();
        kaptcha.setConfig(config);
        return kaptcha;
    }
}
