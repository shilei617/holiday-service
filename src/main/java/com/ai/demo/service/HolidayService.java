package com.ai.demo.service;

import com.ai.demo.entity.Holiday;
import com.google.gson.Gson;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class HolidayService implements InitializingBean {

    private final Gson gson = new Gson();

    public ConcurrentHashMap<String, List<Holiday>> countryHolidayMap = null;  // countryHolidayMap


    //implement afterPropertiesSet
    @Override
    public void afterPropertiesSet() throws Exception {
        // read csv file
        List<Holiday> holidays = csvService.readCsv();
        // convert to countryHolidayMap
        ConcurrentHashMap<String, List<Holiday>> map = convertToMap(holidays);
        // sort holiday list by date
        map.forEach((country, holidayList) -> {
            holidayList = sortHoliday(holidayList);
            map.put(country, holidayList);
        });

        // set countryHolidayMap
        this.countryHolidayMap = map;

        // create a new thread to write map to csv
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000 * 60);
                    writeCsv();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }


    // write csv
    public void writeCsv() {
        csvService.writeCsv(this.countryHolidayMap);
    }


    // csv service
    @Autowired
    private CsvService csvService;

    // get next holiday
    public String getNextHoliday(String country) {
        Date date = new Date();
        // format date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(date);
        // get holiday list
        List<Holiday> holidayList = countryHolidayMap.get(country);
        Holiday nextHoliday = getNextHoliday(holidayList, currentDate);
        //return date
        return nextHoliday.getDate();
    }

    // get next year
    public String nextYear(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        String currentYear = sdf.format(date);
        int nextYear = Integer.parseInt(currentYear) + 1;
        return String.valueOf(nextYear);
    }


    // get next year holiday date list by country
    public List<String> getNextYearHoliday(String country) {
        // get next year
        String nextYear = nextYear();
        // get holiday list
        List<Holiday> holidayList = countryHolidayMap.get(country);
        // iterate list where date start with current year
        List<Holiday> nextYearHolidayList = new ArrayList<>();
        holidayList.forEach(holiday -> {
            String dateStr = holiday.getDate();
            if (dateStr.startsWith(nextYear)) {
                nextYearHolidayList.add(holiday);
            }
        });

        // convert to date list
        List<String> dateList = new ArrayList<>();
        nextYearHolidayList.forEach(holiday -> dateList.add(holiday.getDate()));
        // return
        return dateList;
    }




    // get item where data > current date
    public Holiday getNextHoliday(List<Holiday> holidays, String currentDate) {
        // get next holiday
        Holiday nextHoliday = null;
        for (Holiday holiday : holidays) {
            // get date
            String date = holiday.getDate();
            // compare date
            if (date.compareTo(currentDate) > 0) {
                nextHoliday = holiday;
                break;
            }
        }
        // return
        return nextHoliday;
    }

    // is holiday or not
    public boolean isHoliday(String country, String date) {
        checkDateFormat(date);
        // get holiday list
        List<Holiday> holidayList = countryHolidayMap.get(country);
        // iterate list
        for (Holiday holiday : holidayList) {
            // get holiday date
            String holidayDate = holiday.getDate();
            // compare date
            if (holidayDate.equals(date)) {
                return true;
            }
        }
        // return
        return false;
    }

    // convert string to date using format yyyy-MM-dd
    public void checkDateFormat(String dateStr) {
        // convert to date
        Date date = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            date = sdf.parse(dateStr);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            throw new RuntimeException("Invalid input");
        }
    }



    // convert holiday list to ConcurrentHashMap
    public ConcurrentHashMap<String, List<Holiday>> convertToMap(List<Holiday> holidays) {
        // create map
        ConcurrentHashMap<String, List<Holiday>> map = new ConcurrentHashMap<>();
        // sort holiday list by date
        holidays = sortHoliday(holidays);
        // convert to map
        holidays.forEach(holiday -> {
            // get country
            String country = holiday.getCountry();
            // get holiday list
            List<Holiday> holidayList = map.get(country);
            // if holiday list is null, create new list
            if (holidayList == null) {
                holidayList = new ArrayList<>();
            }
            // add holiday to list
            holidayList.add(holiday);
            // put holiday list to map
            map.put(country, holidayList);
        });
        // return
        return map;
    }


    // sort holiday list by date
    public List<Holiday> sortHoliday(List<Holiday> holidays) {
        // sort by date
        holidays.sort(Comparator.comparing(Holiday::getDate));
        return holidays;
    }

    // convert body to holiday using gson
    public Holiday convertToHoliday(String body) {
        // convert to holiday
        Holiday holiday = null;
        try {
            holiday = gson.fromJson(body, Holiday.class);
        }catch (Exception ex){
            System.out.println(ex.getMessage());
            throw new RuntimeException("Invalid input");
        }
        // return
        return holiday;
    }


    // add holiday
    public void addOrUpdateHoliday(String body) {
        // convert to holiday
        Holiday holiday = convertToHoliday(body);

        // getHolidayFromMap
        Holiday holidayFromMap = getHolidayFromMap(holiday);

        // if null, add
        if (holidayFromMap == null) {
            addHoliday(holiday);
            return;
        }

        // if not null, then compare by name, if not equal, delete old one and add new one
        if(!compareByName(holiday, holidayFromMap)){
            deleteHoliday(holidayFromMap);
            addHoliday(holiday);
        }

    }


    // add holiday
    public void addHoliday(Holiday holiday) {
        // get country
        String country = holiday.getCountry();
        // get holiday list
        List<Holiday> holidayList = countryHolidayMap.get(country);
        // add holiday
        holidayList.add(holiday);
        // sort holiday list
        holidayList = sortHoliday(holidayList);
        // put holiday list to map
        countryHolidayMap.put(country, holidayList);
    }


    // judge holiday is exist or not
    public boolean isExist(Holiday holiday) {
        // getHolidayFromMap
        Holiday holidayFromMap = getHolidayFromMap(holiday);
        // judge holiday is exist or not
        return holidayFromMap != null;
        // return
    }


    public void deleteHoliday(String body) {
        // convert to holiday
        Holiday holiday = convertToHoliday(body);
        // judge holiday is exist or not
        if (!isExist(holiday)) {
            return;
        }
        // delete holiday
        deleteHoliday(holiday);
    }


    // delete holiday
    public void deleteHoliday(Holiday holiday) {
        // get country
        String country = holiday.getCountry();
        // get holiday list
        List<Holiday> holidayList = countryHolidayMap.get(country);
        // iterate list
        for (Holiday h : holidayList) {
            // get date
            String date = h.getDate();
            // compare date
            if (date.equals(holiday.getDate())) {
                // remove holiday
                holidayList.remove(h);
                // put holiday list to map
                countryHolidayMap.put(country, holidayList);
                return;
            }
        }
    }


    // compare 2 holiday by name return boolean
    public boolean compareByName(Holiday holiday1, Holiday holiday2) {
        // get name
        String name1 = holiday1.getName();
        String name2 = holiday2.getName();
        // compare name
        return name1.equals(name2);
    }

    // get holiday from map by holiday
    public Holiday getHolidayFromMap(Holiday holiday) {
        // get country
        String country = holiday.getCountry();
        // get holiday list
        List<Holiday> holidayList = countryHolidayMap.get(country);
        // iterate list
        for (Holiday h : holidayList) {
            // get date
            String date = h.getDate();
            // compare date
            if (date.equals(holiday.getDate())) {
                return h;
            }
        }
        // return
        return null;
    }



}
