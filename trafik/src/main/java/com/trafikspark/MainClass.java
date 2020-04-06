package com.trafikspark;

import com.mysql.jdbc.Driver;
import org.pcap4j.core.*;
import org.pcap4j.packet.Packet;
import org.pcap4j.util.NifSelector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import static org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;

/**
 * Инструкция по использованию проекта в файле readme
 * Позволяет выбрать действующий сетевой интерфейс, с которого будет проводиться захват пакетов, c использованием библиотеки pcap4j.
 * Объем перехваченного трафика будет транслироваться в БД каждые пять минут, и записывать действующее значение в таблицу.
 * Также в таблице будут обновляться значения максимального и минимального трафика каждые 20 минут. При превышении данных лимитов
 * приложение будет отправлять сообщение alert в топик alert.
 * */

public class MainClass{
    public static int allowedTime = 0;
    public static boolean passBy = false;
  public static void main (String[] args)  throws PcapNativeException, NotOpenException, IOException, SQLException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
      // Класс, который будет хранить найденное нами сетевое устройство. Используем его для захвата.
      final PcapNetworkInterface[] device = {getNetworkDevice()};
      System.out.println("You choose" + device[0]);

      if (device[0] == null) {
          System.out.println("No device chosen.");
          System.exit(1);
      }

      int snapshotLength = 65536; // длина захвата ручки
      int readTimeout = 50; //???время задержки буферизации???
      final PcapHandle handle;

      System.out.println("Укажите номер сетевого интерфейса с нужным вам ip адрессом");
      System.out.println(device[0].getAddresses());

      // Создаем обьъект для живой записи пакетов
      handle = device[0].openLive(snapshotLength, PromiscuousMode.PROMISCUOUS, readTimeout);

      BufferedReader parametr = new BufferedReader(new InputStreamReader(System.in));

      System.out.println("Начальные параметры программмы: ");
      System.out.println("url: jdbc:mysql://localhost:3306/traffic_limits и TimeZone ?serverTimezone=Europe/Moscow&useSSL=FALSE");
      System.out.println("useraname: root");
      System.out.println("password: 12345");
      System.out.println("Время действия программы: 30 минут");
      System.out.println("Если хотите изменить параметры программы нажмите 1. Если желаете оставить параметры по умолчанию нажмите 0");

      BD basaData;
      if (parametr.readLine().equals("0")) {
          basaData = new BD("jdbc:mysql://localhost:3306/traffic_limits?serverTimezone=Europe/Moscow&useSSL=FALSE", "root", "Rjkj,jr17");
          allowedTime = 30;
      }
      else {
          Parameters parameters = new Parameters();
          basaData = new BD(parameters.urlTimeZonetoString(), parameters.usernametoString(), parameters.passwordtoString());
          allowedTime = parameters.transferAllowedTime();
      }

      basaData.receiveTable();//Берем дату с которой должен начинаться отсчет, и устанавливаем лимиты(из таблицы)
      /*Создаем объект, который будет работать как таймер. Подробнее в файле класса*/
      TimeManager timeManager = new TimeManager(basaData.receiveData());

      /*Дата которая будет учавствовать в измерении длителльности программы.
      (time.getTime - newTime.getTime) < время выполнения программы*/
      long time = new Date().getTime();

      // PacketListener интерфейс с параметром пакет
        PacketListener listener = new PacketListener() {
            long startTime = new Date().getTime();//получаем текущую дату и время
            int trafik = 0; //создаем локальную переменную трафик которая будет отправляться в бд

            @Override
            public void gotPacket(Packet packet) {
              long newTime = new Date().getTime();//получаем новое текущее время
              long msDelay = newTime - startTime;

              if (1000 * 60 * 5 < msDelay) {
                  Timestamp Dtime = new Timestamp(System.currentTimeMillis());//Объект значение которого будут записываться в таблицу
                  basaData.refreshTable(trafik, Dtime); //Добавляет в таблицу значения трафика каждые пять минут
                  startTime = new Date().getTime();//Переписываем ссылку на новую дату(чтобы занаво вычислить разницу в пять минут)
                  trafik = 0; //Обнуляем трафик, чтобы считать за следующее пять минут
              }
              if (time - newTime  > allowedTime * 60 * 1000) {passBy = true; return;}
              trafik = trafik + packet.length(); //Складываем размеры все перехваченных пакетов
          }
      };

      try {
          /*loop - этот метод создает объект Packet из захваченного пакета, используя фабрику пакетов,
           *и передает его в listener.gotPacket (Packet).
           *Когда пакет захвачен, listener.gotPacket (Packet) вызывается в потоке, который вызвал loop ().
           *И затем этот PcapHandle ожидает, когда поток вернется из gotPacket (),
           *прежде чем он получит следующий пакет из буфера pcap.*/
          if (passBy == false) handle.loop(-1, listener);

        } catch (InterruptedException e) {
          e.printStackTrace();
       }
        handle.close();
        basaData.closeBD();
        parametr.close();
  }

    // Класс, экземпляр которого будет хранить найденное нами сетевое устройство. Используем его для захвата.
    static PcapNetworkInterface getNetworkDevice() {
        PcapNetworkInterface device = null;
        try {
            device = new NifSelector().selectNetworkInterface();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return device;
    }
    }



