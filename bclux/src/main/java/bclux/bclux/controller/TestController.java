package bclux.bclux.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TestController {

    @RequestMapping("/testfail")
    public String testFail() {
        return "fail";
    }

    @GetMapping("/testresult")
    public String testResult() {
        return "result";
    }

}
