package jpabook.jpashop.controller;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j // Logger log = LoggerFactory.getLogger(getClass());
@Controller
public class HomeController {

    @RequestMapping("/")
    public String home() {
        log.info("hole controller");
        return "home";
    }

}
