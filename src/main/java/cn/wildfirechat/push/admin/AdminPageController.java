package cn.wildfirechat.push.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AdminPageController {

    @RequestMapping({"/admin", "/admin/", "/admin/login.html"})
    public String adminIndex() {
        return "forward:/admin/index.html";
    }
}
