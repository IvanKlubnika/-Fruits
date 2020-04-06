package com.trafikspark;

import java.sql.*;
import java.util.GregorianCalendar;
import java.lang.Object;
// import java.sql.DataSource ;
/**В данном классе описывается взаимодействия с базой данных.
 * Он позволяет:
 * записывать новые значения в таблицу,
 * перезаписывать значения
 * обновлять лимиты таблицы каждые 20 минут
 * */
public class BD {
    private Connection connection;
    private Statement statement;
    private int number = 0;
    private int limitUp;
    private int limitDown;
    private Timestamp nextDate;
    private GregorianCalendar calendar = new GregorianCalendar();
    private KafkaManager kafkaManager;

    BD(String url, String username, String password){
        try {
            kafkaManager= new KafkaManager();
            connection = DriverManager.getConnection( url, username, password);
            System.out.println("Подключение к БД прошло успешно");
            statement = connection.createStatement();
        }
    catch (SQLException e){
        System.out.println("Не подключились к БД, ошибка:");
        System.out.println(e);
    }}


/*     */
   void refreshTable(int trafik, Timestamp Dtime){
       String stringDtime = Dtime.toString();
       String sqlCommand  = "INSERT limits_per_hour(limit_name, limit_value, effective_date) VALUES('" + number
              + "', " + trafik + ", " + "('"  + stringDtime + "'));";
      number++;
/*Если прошло 20 минут то обновляем лимиты, в БД и сразу же в самой программе, чтобы снова не получать их из БД.
   смотрим остаток от деления на 4, тк метод вызывается каждые пять минут, а обновлять лимиты нужно каждые 20 */
       if (number % 4 == 0) refreshLimit(trafik, Dtime);
       try{
         statement.executeUpdate(sqlCommand);
     }
     catch (SQLException e){
         System.out.println(e);
       }}


       void printLimit(){
        System.out.println(limitDown);
        System.out.println(limitUp);
        System.out.println(calendar.getTime());
       }


/*Берем дату с которой должен начинаться отсчет, и устанавливаем лимиты*/
    void receiveTable(){
        /*Получаем значение даты максимального лимита из БД, далее к дате обращаться не будем,
        будем только отсылать ее в таблицу каждые пять минут*/
       try {
           ResultSet resultSet = statement.executeQuery("SELECT * FROM limits_per_hour");
           while (resultSet.next()) {
               /*Узнаем название лимита, чтобы воспользоваться строчкой*/
               String limit_name = resultSet.getString(1);
        /*Получаем значение верхнего лимита один раз, далее будем только перезаписывать его в БД и в самой программе
        Получаем значение даты максимального лимита, далее к дате обращаться не будем,
        будем только отсылать ее в таблицу каждые пять минут, вместе с новыми данными*/
               if(limit_name.equals("max")) {
                   limitUp = resultSet.getInt(2); //Устанавливаем верхний предел
                   nextDate = resultSet.getTimestamp(3);//Устанавливает дату начала отсчета
                   calendar.setTime(nextDate);
               }
               else limitDown = resultSet.getInt(2);// Тк первоначально в таблице только два значения
           }} catch (SQLException e){
           System.out.println(e);}
    }


    /*Устанавливает новые значения min и max в программе*/
    void refreshLimit(int trafik, Timestamp Dtime){

        if(trafik > limitUp){
            limitUp = trafik;
            kafkaManager.messageKafkaTopic();//Отправляем сообщение в топик alert
            tableMax(trafik, Dtime);//Устанавливает новые значения max в таблице
        }

        if(trafik < limitDown){
            limitDown = trafik;
            kafkaManager.messageKafkaTopic();//Отправляем сообщение в топик alert
            tableMin(trafik, Dtime);//Устанавливает новые значения min в таблице
        }
    }

    /*Метод устанавливает новое значения максимального лимита в таблице*/
    void tableMax(int trafik, Timestamp Dtime){
        try {
            String stringDtime = Dtime.toString();
            statement.executeUpdate("UPDATE limits_per_hour SET limit_value = " +  trafik + " WHERE limit_name = 'max' ");
            statement.executeUpdate("UPDATE limits_per_hour SET effective_date = " + "('"  + stringDtime + "')" + " WHERE limit_name = 'max' ");
        }catch (SQLException e){
            System.out.println(e);
        }
        }

    /*Метод устанавливает новое значения минимального лимита в таблице*/
    void tableMin(int trafik, Timestamp Dtime){
        try {
            String stringDtime = Dtime.toString();
            statement.executeUpdate("UPDATE limits_per_hour SET limit_value = " +  trafik + " WHERE limit_name = 'min'");
            statement.executeUpdate("UPDATE limits_per_hour SET effective_date = "  + "('"  + stringDtime + "')" +  " WHERE limit_name = 'min'");
        }catch (SQLException e){
            System.out.println(e);
        }
    }

     Timestamp receiveData() {
      if (nextDate != null) return nextDate;
     else return null;
    }

    /*Метод для закрытия потока*/
    void closeBD() throws SQLException{
        connection.close();
    }

}
