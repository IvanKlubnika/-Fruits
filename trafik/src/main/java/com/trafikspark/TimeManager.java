package com.trafikspark;

import org.apache.commons.net.ntp.TimeStamp;

import java.sql.Timestamp;
/**Создает объект,который будет работать как таймер.В конструкторе есть цикл который закончится,
  *когда время станет больше установленной в таблице даты.
  *Если время изначально больше,выведет сообщение об ошибке*/

public class TimeManager {

 TimeManager(Timestamp dataNext){
     Timestamp timestamp = new Timestamp(System.currentTimeMillis());
     if (dataNext.after(timestamp)){
     while(dataNext.compareTo(timestamp) > 0) {
        timestamp = new Timestamp(System.currentTimeMillis());
    }}
    else System.out.println("Значение даты с которой должен начаться отсчет уже прошло. Обновите таблицу и перезапустите программу ");
    }
}