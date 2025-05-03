package db;

import java.util.*;

public class DBUtils {
    public static final Map<String, List<String>> TABLE_COLUMNS; // 각 테이블의 속성을 저장한 map
    public static final Map<String, String> PRIMARY_KEYS; // 각 테이블의 PK 속성 이름 저장한 map
    public static final Set<String> NUMERIC_COLUMNS; // 도메인이 int형인 애들을 모아놓은 set

    static {
        Map<String, List<String>> fieldMap = new HashMap<>();
        Map<String, String> pkMap = new HashMap<>();

        fieldMap.put("RENT_COMPANY", Arrays.asList("company_name", "address", "phone", "manager_name", "manager_email"));
        pkMap.put("RENT_COMPANY", "company_id");

        fieldMap.put("CAMPING_CAR", Arrays.asList("car_name", "car_number", "capacity", "image", "car_detail", "rental_fee", "registration_date", "company_id"));
        pkMap.put("CAMPING_CAR", "campingcar_id");

        fieldMap.put("PARTS", Arrays.asList("part_name", "price", "quantity", "arrival_date", "supplier"));
        pkMap.put("PARTS", "part_id");

        fieldMap.put("EMPLOYEE", Arrays.asList("employee_name", "phone", "address", "salary", "num_dependents", "department", "employee_role"));
        pkMap.put("EMPLOYEE", "employee_id");

        fieldMap.put("CUSTOMER", Arrays.asList("login_id", "password", "driver_license", "customer_name", "address", "phone", "email", "previous_date", "previous_car"));
        pkMap.put("CUSTOMER", "customer_id");

        fieldMap.put("RENTAL", Arrays.asList("rental_start", "rental_period", "fee", "deadline", "additional_detail", "additional_fee", "campingcar_id", "company_id", "driver_license"));
        pkMap.put("RENTAL", "rental_id");

        fieldMap.put("EXTERNAL_MAINTENANCE_SHOP", Arrays.asList("shop_name", "address", "phone", "manager_name", "manager_email"));
        pkMap.put("EXTERNAL_MAINTENANCE_SHOP", "shop_id");

        fieldMap.put("EXTERNAL_MAINTENANCE_REQUEST", Arrays.asList("maintenance_detail", "maintenance_date", "fee", "deadline", "additional_detail", "campingcar_id", "shop_id", "company_id", "driver_license"));
        pkMap.put("EXTERNAL_MAINTENANCE_REQUEST", "maintenance_id");

        fieldMap.put("SELF_MAINTENANCE", Arrays.asList("date", "duration", "campingcar_id", "part_id", "employee_id"));
        pkMap.put("SELF_MAINTENANCE", "maintenance_id");

        TABLE_COLUMNS = Collections.unmodifiableMap(fieldMap);
        PRIMARY_KEYS = Collections.unmodifiableMap(pkMap);
        NUMERIC_COLUMNS = Set.of(
                "company_id",
                "capacity",
                "rental_fee",
                "price",
                "quantity",
                "salary",
                "num_dependents",
                "rental_period",
                "fee",
                "additional_fee",
                "campingcar_id",
                "employee_id",
                "shop_id",
                "part_id"
        );
    }
}

