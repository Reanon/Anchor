package com.reanon.community.controller;

import com.reanon.community.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

/**
 * 网站数据统计: 独立访客、日活跃用户等
 *
 * @author reanon
 * @create 2021-07-22
 */
@Controller
public class DataController {
    @Autowired
    private DataService dataService;

    /**
     * 进入统计界面
     */
    @RequestMapping(value = "/data", method = {RequestMethod.GET, RequestMethod.POST})
    public String getDataPage() {
        // 进入模板
        return "/site/admin/data";
    }

    /**
     * 统计网站 uv（独立访客）
     *    - @DateTimeFormat 设置传入字符串的格式为日期
     *
     * @param start 开始日期
     * @param end 结束日期
     */
    @PostMapping("/data/uv")
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date end,
                        Model model) {
        long uv = dataService.calculateUV(start, end);
        model.addAttribute("uvResult", uv);
        model.addAttribute("uvStartDate", start);
        model.addAttribute("uvEndDate", end);
        // 请求转发到统计页面, 是同一个请求
        return "forward:/data";
    }

    /**
     * 统计网站 DAU
     *
     * @param start 开始日期
     * @param end 结束日期
     */
    @PostMapping("/data/dau")
    public String getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                         @DateTimeFormat(pattern = "yyyy-MM-dd") Date end,
                         Model model) {
        // 计算 start ~ end 时间内的 DAU
        long dau = dataService.calculateDAU(start, end);
        model.addAttribute("dauResult", dau);
        model.addAttribute("dauStartDate", start);
        model.addAttribute("dauEndDate", end);
        // 请求转发到统计页面
        return "forward:/data";
    }

}
