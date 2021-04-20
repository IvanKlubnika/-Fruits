import java.sql.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static java.util.Map.*;


/**
 * Второй способ решения!!!!!!
 */

/**
 * Все классы поместил в один документ,
 * так как в требованиях по оформлению указано, что ответ должен быть в одном java файле.
 */
public class DepartmentSalary {

    static final String DB_DRIVER = "org.postgresql.Driver";
    static final String DB_URL = "jdbc:postgresql://localhost:5432/homework";

    static final String USER = "ivan";
    static final String PASSWORD = "enigma";

    public static void main(String[] args) {
        //Передаем параметры, нужные для подключения к БД.
        Repository repository = new Repository(DB_URL, DB_DRIVER, USER, PASSWORD);
        //Создаем запрос в БД.
        ResultSet resultSet = repository.createQuery();
        //Создаем объект, который будет построчно обрабатывать информацию.
        QueryProcessing queryProcessing = new QueryProcessing();

        //Обрабатываем полученные из БД данные.
        try {
            queryProcessing.dataAnalysis(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Закрываем соединение.
        repository.close();
    }
}



class QueryProcessing {

    /**
     * В данном методе мы проходимся по всем данным, полученным из БД, и выводим их.
     *
     * @param resultSet
     * @throws SQLException
     */
    void dataAnalysis(ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            String department = resultSet.getString("department");
            int salary = resultSet.getInt("sum");
            System.out.println("Department: " + department);
            System.out.println("Salary: $" + salary);
        }
    }
}



class Repository {

    Connection dbConnection = null;
    Statement statement = null;
    ResultSet resultSet = null;

    Repository(String DB_URL, String DB_DRIVER, String USER, String PASSWORD) {

        //Подключаемся к базе данных
        System.out.println("Подключаем JDBC драйвер");

        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println("Драйвер PostgreSQL JDBC не найден. Включите его в путь к вашей библиотеке.");
            e.printStackTrace();
        }

        System.out.println("Идет подключение к базе данных");
        try {
            dbConnection = DriverManager.getConnection(DB_URL, USER, PASSWORD);

            if (dbConnection != null) {
                System.out.println("Вы успешно подключились к базе данных.");
            } else System.out.println("Не удалось подключиться к базе данных.");

        } catch (SQLException e) {
            System.out.println("Ошибка подключения:");
            e.printStackTrace();
        }
    }


    /**
     * Данный метод используется для создания SQL запроса.
     *
     * @return
     */
    ResultSet createQuery() {

        try {
            statement = dbConnection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String sql;
        sql = "SELECT department, SUM(salary) FROM employees\n" +
                "GROUP BY department";


        try {
            resultSet = statement.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultSet;
    }


    /**
     * Данный метод закрывает все ресурсы.
     */
    void close() {
        try {
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            dbConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
