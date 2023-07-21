package com.ai.demo.controller;

import com.ai.demo.service.HolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// add annotation and mapping
@RestController
@RequestMapping("/holiday")
public class HolidayController {

    // autowire holiday service
    @Autowired
    private HolidayService holidayService;

    // add method
    @GetMapping("/next")
    public String getNextHoliday(@RequestParam String country) {
        return holidayService.getNextHoliday(country);
    }

    // next year
    @GetMapping("/next-year")
    public List<String> postNextYearHoliday(@RequestParam String country) {
        return holidayService.getNextYearHoliday(country);
    }

    // is it a holiday? extract query variable
    @GetMapping("/is-holiday")
    public boolean isHoliday(@RequestParam String country, @RequestParam String date) {
        return holidayService.isHoliday(country, date);
    }


    // add holiday
    @PostMapping("/addOrUpdate")
    public boolean addHoliday(@RequestBody String holiday){
        holidayService.addOrUpdateHoliday(holiday);
        return true;
    }


    // delete holiday
    @PostMapping("/delete")
    public boolean deleteHoliday(@RequestBody String holiday){
        holidayService.deleteHoliday(holiday);
        return true;
    }

}
