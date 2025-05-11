package db;

import java.util.*;

public class DBUtils {
    public static final Map<String, List<String>> TABLE_COLUMNS; // 각 테이블의 속성을 저장한 map
    public static final Map<String, String> PRIMARY_KEYS; // 각 테이블의 PK 속성 이름 저장한 map

    public static final Set<String> INT_COLUMNS; // 도메인이 INT 인 애들을 모아놓은 set
    public static final Set<String> DATE_COLUMNS; // 도메인이 DATE 인 애들을 모아놓은 set
    public static final Set<String> BLOB_COLUMNS; // 도메인이 BLOB 인 애들을 모아놓은 set

    public static final Map<String, Integer> MAINTENANCE_FEES; // 외부 정비소에 맡길 수 있는 정비 목록과 요금

    static {
        Map<String, List<String>> fieldMap = new HashMap<>();
        Map<String, String> pkMap = new HashMap<>();
        Map<String, Integer> maintenanceMap = new HashMap<>();

        fieldMap.put("RENT_COMPANY", Arrays.asList("company_name", "address", "phone", "manager_name", "manager_email"));
        fieldMap.put("CAMPING_CAR", Arrays.asList("car_name", "car_number", "capacity", "image", "car_detail", "rental_fee", "registration_date", "company_id"));
        fieldMap.put("PARTS", Arrays.asList("part_name", "price", "quantity", "arrival_date", "supplier"));
        fieldMap.put("EMPLOYEE", Arrays.asList("employee_name", "phone", "address", "salary", "num_dependents", "department", "employee_role"));
        fieldMap.put("CUSTOMER", Arrays.asList("login_id", "password", "driver_license", "customer_name", "address", "phone", "email", "previous_date", "previous_car"));
        fieldMap.put("RENTAL", Arrays.asList("rental_start", "rental_period", "fee", "deadline", "additional_detail", "additional_fee", "campingcar_id", "company_id", "driver_license"));
        fieldMap.put("EXTERNAL_MAINTENANCE_SHOP", Arrays.asList("shop_name", "address", "phone", "manager_name", "manager_email"));
        fieldMap.put("EXTERNAL_MAINTENANCE_REQUEST", Arrays.asList("maintenance_detail", "maintenance_date", "fee", "deadline", "additional_detail", "campingcar_id", "shop_id", "company_id", "driver_license"));
        fieldMap.put("SELF_MAINTENANCE", Arrays.asList("date", "duration", "campingcar_id", "part_id", "employee_id"));
        TABLE_COLUMNS = Collections.unmodifiableMap(fieldMap);

        pkMap.put("RENT_COMPANY", "company_id");
        pkMap.put("CAMPING_CAR", "campingcar_id");
        pkMap.put("PARTS", "part_id");
        pkMap.put("EMPLOYEE", "employee_id");
        pkMap.put("CUSTOMER", "customer_id");
        pkMap.put("RENTAL", "rental_id");
        pkMap.put("EXTERNAL_MAINTENANCE_SHOP", "shop_id");
        pkMap.put("EXTERNAL_MAINTENANCE_REQUEST", "maintenance_id");
        pkMap.put("SELF_MAINTENANCE", "maintenance_id");
        PRIMARY_KEYS = Collections.unmodifiableMap(pkMap);

        INT_COLUMNS = Set.of(
                "company_id", "capacity", "rental_fee", "price", "quantity", "salary", "num_dependents",
                "rental_period", "fee", "additional_fee", "campingcar_id", "employee_id", "shop_id", "part_id"
        );

        DATE_COLUMNS = Set.of(
                "registration_date", "previous_date", "rental_start", "arrival_date", "maintenance_date", "deadline", "date"
        );

        BLOB_COLUMNS = Set.of("image");

        maintenanceMap.put("엔진오일 교체", 85000);
        maintenanceMap.put("타이어 교체", 90000);
        maintenanceMap.put("브레이크 패드 점검", 70000);
        maintenanceMap.put("실내 세차", 30000);
        maintenanceMap.put("에어컨 필터 교체", 60000);
        maintenanceMap.put("타이밍벨트 교체", 120000);
        maintenanceMap.put("외부 흠집 복원", 95000);
        maintenanceMap.put("냉각수 보충", 40000);
        maintenanceMap.put("전조등 교체", 55000);
        maintenanceMap.put("배터리 점검", 60000);
        maintenanceMap.put("오일 누유 점검", 75000);
        maintenanceMap.put("샤워실 수압 문제", 65000);
        MAINTENANCE_FEES = Collections.unmodifiableMap(maintenanceMap);
    }
}

