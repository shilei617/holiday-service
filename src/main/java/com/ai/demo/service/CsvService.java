package com.ai.demo.service;

import com.ai.demo.entity.Holiday;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CsvService{



    private String csvFilePath = "/Users/leilei/share/github/holiday-service/src/main/resources";

    // write csv
    public void writeCsv(ConcurrentHashMap<String, List<Holiday>> countryHolidayMap) {
        // convert to list
        List<Holiday> holidays = new ArrayList<>();
        countryHolidayMap.forEach((country, holidayList) -> {
            holidayList.forEach(holiday -> {
                holidays.add(holiday);
            });
        });

        // convert to string
        String content = convertToString(holidays);
        // write to csv
        writeToCsv(content);

    }


    // convert holiday list to string, every string use , to split
    public String convertToString(List<Holiday> holidayList) {
        StringBuilder sb = new StringBuilder();
        holidayList.forEach(holiday -> {
            sb.append(holiday.getCountry()).append(",").append(holiday.getDate()).append(",").append(holiday.getName()).append("\n");
        });
        return sb.toString();
    }


    // write string to csv file using buffer writer
    public void writeToCsv(String content){
        String temp = csvFilePath + "/holiday_temp.csv";
        // write to src/main/resources/holiday.csv
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(temp))) {
            writer.write(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //delete old file and rename temp file
        File file = new File(csvFilePath + "/holiday.csv");
        file.delete();
        File tempFile = new File(temp);
        tempFile.renameTo(file);
    }


    // please write a method to read data from csv file
    public List<Holiday> readCsv() throws IOException {
        String csvFile = csvFilePath + "/holiday.csv";
        // read data from csv file using common csv
        List<Holiday> holidays = new ArrayList<>();
        InputStream inputStream = new FileInputStream(csvFile);
        CSVParser parser = CSVFormat.DEFAULT.parse(new InputStreamReader(inputStream));
        parser.forEach(csvRecord -> {
            // convert to holiday
            Holiday holiday = new Holiday();
            holiday.setCountry(csvRecord.get(0));
            holiday.setDate(csvRecord.get(1));
            holiday.setName(csvRecord.get(2));
            holidays.add(holiday);
        });

        //return
        return holidays;
    }


}
