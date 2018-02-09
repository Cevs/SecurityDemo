package com.example.SecurityDemo.controllers;

import com.example.SecurityDemo.ActiveUserStore;
import com.example.SecurityDemo.LoggedUser;
import com.example.SecurityDemo.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import sun.rmi.runtime.Log;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class IndexController {

    @RequestMapping(value ={"/", "/index"})
    public String index(HttpServletRequest request){
        return "index";
    }
}
