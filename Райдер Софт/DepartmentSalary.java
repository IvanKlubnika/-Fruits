import java.sql.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static java.util.Map.*;

/**
* Первый способ решения!!!!!!
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
    static final Service service = new Service();

    public static void main(String[] args) {
        //Передаем параметры, нужные для подключения к БД.
        Repository repository = new Repository(DB_URL, DB_DRIVER, USER, PASSWORD);
        //Создаем запрос в БД.
        ResultSet resultSet = repository.createQuery();
        //Создаем объект, который будет построчно обрабатывать информацию.
        QueryProcessing queryProcessing = new QueryProcessing(service);

        //Обрабатываем полученные из БД данные.
        try {
            queryProcessing.dataAnalysis(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Выводим результат на экран.
        service.printDepartmentSalary();

        //Закрываем соединение.
        repository.close();
    }
}


class QueryProcessing {

    Service service;

    QueryProcessing(Service service) {
        this.service = service;
    }

    /**
     * В данном методе мы проходимся по всем данным, полученным из БД, и обрабатываем их.
     *
     * @param resultSet
     * @throws SQLException
     */
    void dataAnalysis(ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            String department = resultSet.getString("department");
            int salary = resultSet.getInt("salary");

            /*
              Проверяем, ведутся ли уже подсчеты зарплаты для данного отдела.
              Если да, то тогда вызываем метод amountOfMoney, который добавит зарплату к общей сумме.
              Если нет, то тогда добавляем данный отдел в список и начинаем считать общую зарплату.
             */
            if (service.checkDepartment(department)) {
                service.amountOfMoney(department, salary);
            } else {
                service.addDepartment(department, salary);
            }
        }
    }
}


class Service {
    /**
     * Тут хранятся пары ключ-значение, в которых ключом является соответствующий отдел,
     * а значением - общая зарплата в этом отделе.
     */
    HashMap<String, Integer> departmentSalary = new HashMap();

    /**
     * Данный метод проходится по всем отделам в хэш-таблице и ищет совпадения.
     * Если совпадения найдены, то значит этот отдел не нужно повторно добавлять в хэш-таблицу.
     * Если данного отдела не найдено, то он заносится в таблицу.
     *
     * @param department
     * @return
     */
    boolean checkDepartment(String department) {
        boolean check = false;

        for (Entry entry : departmentSalary.entrySet()) {
            if (department.equals(entry.getKey())) {
                check = true;
                break;
            }
        }
        return check;
    }


    /**
     * Данный метод суммирует все зарплаты сотрудников отдела
     * и заносит их в хэш-таблицу после пересчета.
     *
     * @param department
     * @param salary
     */
    void amountOfMoney(String department, Integer salary) {
        Integer sum = departmentSalary.get(department);
        sum = sum + salary;
        departmentSalary.remove(department);
        departmentSalary.put(department, sum);
    }


    /**
     * Данный метод добавляет отдел, который встретился первый раз, в хэш-таблицу
     * и добавляет зарплату первого сотрудника этого отдела.
     *
     * @param department
     * @param salary
     */
    void addDepartment(String department, Integer salary) {
        departmentSalary.put(department, salary);
    }


    /**
     * Данный метод выводит результат (общую зарплату отдела).
     */
    void printDepartmentSalary() {
        for (Entry entry : departmentSalary.entrySet()) {
            System.out.println("Department: " + entry.getKey());
            System.out.println("Salary: $" + entry.getValue());
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
        sql = "SELECT department, salary FROM employees";


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
