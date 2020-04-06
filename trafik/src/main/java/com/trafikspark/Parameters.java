package com.trafikspark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
/**Класс позволяет ввести параметры для выполнения программы
 * Примечание: Игнорирование параметра Timezone может привести к проблеме с SSL и часовым поясом*/
public class Parameters {
   private String url;
   private String Timezone;
   private String username;
   private String password;
   private int allowedtime;

    Parameters() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Введите адрес источника данных");
        url = reader.readLine();

        System.out.println("Введите Timezone (Игнорирование данного параметра может привести к проблеме с SSL и часовым поясом)");
        Timezone = reader.readLine();

        System.out.println("Введите логин");
        username = reader.readLine();

        System.out.println("Введите пароль");
        password = reader.readLine();

        System.out.println("Введите время выполнения программы в минутах");
        allowedtime = Integer.parseInt(reader.readLine());

        reader.close();
    }

    public String urlTimeZonetoString() {
        return url + "?" + Timezone;
    }

    public String usernametoString() {
        return username;
    }

    public String passwordtoString() {
        return password;
    }

    public int transferAllowedTime(){
    return allowedtime;
    }
}
