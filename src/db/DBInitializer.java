package db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;

import static java.sql.Types.BLOB;

public class DBInitializer {
    public static void initialize(Connection conn) {
        String[] ddl = {
                "CREATE TABLE IF NOT EXISTS rent_company (\n" +
                        "  company_id INT NOT NULL AUTO_INCREMENT,\n" +
                        "  company_name VARCHAR(45) NOT NULL,\n" +
                        "  address VARCHAR(100) NOT NULL,\n" +
                        "  phone VARCHAR(20) NOT NULL,\n" +
                        "  manager_name VARCHAR(45) NOT NULL,\n" +
                        "  manager_email VARCHAR(100) NOT NULL,\n" +
                        "  PRIMARY KEY (company_id)\n" +
                        ")",

                "CREATE TABLE IF NOT EXISTS camping_car (\n" +
                        "  campingcar_id INT NOT NULL AUTO_INCREMENT,\n" +
                        "  car_name VARCHAR(45) NOT NULL,\n" +
                        "  car_number VARCHAR(20) NOT NULL UNIQUE,\n" +
                        "  capacity INT NOT NULL,\n" +
                        "  image LONGBLOB,\n" +
                        "  car_detail TEXT NOT NULL,\n" +
                        "  rental_fee INT NOT NULL,\n" +
                        "  registration_date DATE NOT NULL,\n" +
                        "  company_id INT NOT NULL,\n" +
                        "  PRIMARY KEY (campingcar_id),\n" +
                        "  FOREIGN KEY (company_id) REFERENCES rent_company(company_id) ON DELETE CASCADE\n" +
                        ")",

                "CREATE TABLE IF NOT EXISTS parts (\n" +
                        "  part_id INT NOT NULL AUTO_INCREMENT,\n" +
                        "  part_name VARCHAR(45) NOT NULL,\n" +
                        "  price INT NOT NULL,\n" +
                        "  quantity INT NOT NULL,\n" +
                        "  arrival_date DATE NOT NULL,\n" +
                        "  supplier VARCHAR(45) NOT NULL,\n" +
                        "  PRIMARY KEY (part_id)\n" +
                        ")",

                "CREATE TABLE IF NOT EXISTS employee (\n" +
                        "  employee_id INT NOT NULL AUTO_INCREMENT,\n" +
                        "  employee_name VARCHAR(45) NOT NULL,\n" +
                        "  phone VARCHAR(20) NOT NULL,\n" +
                        "  address VARCHAR(100) NOT NULL,\n" +
                        "  salary INT NOT NULL,\n" +
                        "  num_dependents INT NOT NULL,\n" +
                        "  department VARCHAR(45) NOT NULL,\n" +
                        "  employee_role VARCHAR(45) NOT NULL,\n" +
                        "  PRIMARY KEY (employee_id)\n" +
                        ")",

                "CREATE TABLE IF NOT EXISTS self_maintenance (\n" +
                        "  maintenance_id INT NOT NULL AUTO_INCREMENT,\n" +
                        "  date DATE NOT NULL,\n" +
                        "  duration INT NOT NULL,\n" +
                        "  campingcar_id INT NOT NULL,\n" +
                        "  part_id INT NOT NULL,\n" +
                        "  employee_id INT NOT NULL,\n" +
                        "  PRIMARY KEY (maintenance_id),\n" +
                        "  FOREIGN KEY (campingcar_id) REFERENCES camping_car(campingcar_id) ON DELETE CASCADE,\n" +
                        "  FOREIGN KEY (part_id) REFERENCES parts(part_id) ON DELETE CASCADE,\n" +
                        "  FOREIGN KEY (employee_id) REFERENCES employee(employee_id) ON DELETE CASCADE\n" +
                        ")",

                "CREATE TABLE IF NOT EXISTS customer (\n" +
                        "  customer_id INT NOT NULL AUTO_INCREMENT,\n" +
                        "  login_id VARCHAR(45) NOT NULL,\n" +
                        "  password VARCHAR(45) NOT NULL,\n" +
                        "  driver_license VARCHAR(45) NOT NULL UNIQUE,\n" +
                        "  customer_name VARCHAR(45) NOT NULL,\n" +
                        "  address VARCHAR(100) NOT NULL,\n" +
                        "  phone VARCHAR(20) NOT NULL,\n" +
                        "  email VARCHAR(100) NOT NULL,\n" +
                        "  previous_date DATE,\n" +
                        "  previous_car VARCHAR(45),\n" +
                        "  PRIMARY KEY (customer_id)\n" +
                        ")",

                "CREATE TABLE IF NOT EXISTS rental (\n" +
                        "  rental_id INT NOT NULL AUTO_INCREMENT,\n" +
                        "  rental_start DATE NOT NULL,\n" +
                        "  rental_period INT NOT NULL,\n" +
                        "  fee INT NOT NULL,\n" +
                        "  deadline DATE NOT NULL,\n" +
                        "  additional_detail VARCHAR(100),\n" +
                        "  additional_fee INT,\n" +
                        "  campingcar_id INT NOT NULL,\n" +
                        "  company_id INT NOT NULL,\n" +
                        "  driver_license VARCHAR(45) NOT NULL,\n" +
                        "  PRIMARY KEY (rental_id),\n" +
                        "  FOREIGN KEY (campingcar_id) REFERENCES camping_car(campingcar_id) ON DELETE CASCADE,\n" +
                        "  FOREIGN KEY (company_id) REFERENCES rent_company(company_id) ON DELETE CASCADE,\n" +
                        "  FOREIGN KEY (driver_license) REFERENCES customer(driver_license) ON DELETE CASCADE\n" +
                        ")",

                "CREATE TABLE IF NOT EXISTS external_maintenance_shop (\n" +
                        "  shop_id INT NOT NULL AUTO_INCREMENT,\n" +
                        "  shop_name VARCHAR(45) NOT NULL,\n" +
                        "  address VARCHAR(100) NOT NULL,\n" +
                        "  phone VARCHAR(20) NOT NULL,\n" +
                        "  manager_name VARCHAR(45) NOT NULL,\n" +
                        "  manager_email VARCHAR(100) NOT NULL,\n" +
                        "  PRIMARY KEY (shop_id)\n" +
                        ")",

                "CREATE TABLE IF NOT EXISTS external_maintenance_request (\n" +
                        "  maintenance_id INT NOT NULL AUTO_INCREMENT,\n" +
                        "  maintenance_detail TEXT NOT NULL,\n" +
                        "  maintenance_date DATE NOT NULL,\n" +
                        "  fee INT NOT NULL,\n" +
                        "  deadline DATE NOT NULL,\n" +
                        "  additional_detail TEXT,\n" +
                        "  campingcar_id INT NOT NULL,\n" +
                        "  shop_id INT NOT NULL,\n" +
                        "  company_id INT NOT NULL,\n" +
                        "  driver_license VARCHAR(45) NOT NULL,\n" +
                        "  PRIMARY KEY (maintenance_id),\n" +
                        "  FOREIGN KEY (campingcar_id) REFERENCES camping_car(campingcar_id) ON DELETE CASCADE,\n" +
                        "  FOREIGN KEY (shop_id) REFERENCES external_maintenance_shop(shop_id) ON DELETE CASCADE,\n" +
                        "  FOREIGN KEY (company_id) REFERENCES rent_company(company_id) ON DELETE CASCADE,\n" +
                        "  FOREIGN KEY (driver_license) REFERENCES customer(driver_license) ON DELETE CASCADE\n" +
                        ")"
        };  // 렌트카 사진 NOT NULL 결정해야됨, 테스트할 땐 일단 null 허용해놓고

        try {
            Statement stmt = conn.createStatement();

            stmt.execute("CREATE SCHEMA IF NOT EXISTS camping DEFAULT CHARACTER SET utf8;");
            stmt.execute("USE camping"); // 데이터베이스에 camping이 없으면 여기서 use camping 선언 후 같은 conn 사용

            stmt.execute("DROP TABLE IF EXISTS external_maintenance_request");
            stmt.execute("DROP TABLE IF EXISTS external_maintenance_shop");
            stmt.execute("DROP TABLE IF EXISTS rental");
            stmt.execute("DROP TABLE IF EXISTS customer");
            stmt.execute("DROP TABLE IF EXISTS self_maintenance");
            stmt.execute("DROP TABLE IF EXISTS employee");
            stmt.execute("DROP TABLE IF EXISTS parts");
            stmt.execute("DROP TABLE IF EXISTS camping_car");
            stmt.execute("DROP TABLE IF EXISTS rent_company");

            for (String query : ddl) {
                stmt.execute(query);
            }

            System.out.println("DB 초기화 완료");

            stmt.execute("SET GLOBAL validate_password.policy = LOW");
            stmt.execute("SET GLOBAL validate_password.length = 4");
            stmt.execute("SET GLOBAL validate_password.mixed_case_count = 0");
            stmt.execute("SET GLOBAL validate_password.number_count = 0");
            stmt.execute("SET GLOBAL validate_password.special_char_count = 0");

            stmt.execute("DROP USER IF EXISTS 'user1'@'localhost'");

            stmt.execute("CREATE USER IF NOT EXISTS 'user1'@'localhost' IDENTIFIED BY 'user1'");
            stmt.execute("GRANT SELECT, INSERT, UPDATE ON camping.camping_car TO 'user1'@'localhost'");
            stmt.execute("GRANT SELECT, INSERT, UPDATE, DELETE ON camping.rental TO 'user1'@'localhost'");
            stmt.execute("GRANT SELECT, INSERT, UPDATE ON camping.external_maintenance_request TO 'user1'@'localhost'");
            stmt.execute("GRANT SELECT ON camping.external_maintenance_shop TO 'user1'@'localhost'");

            System.out.println("MYSQL 계정 생성: ID: user1, PASSWORD: user1");

        } catch (SQLException e) {
            System.out.println("SQL 실행오류");
            e.printStackTrace();
        }
    }

    public static void dataInsert(Connection conn) {
        String[] rentCompany = {
                "INSERT INTO rent_company (company_name, address, phone, manager_name, manager_email) VALUES ('롯데렌터카', '서울특별시 강남구 테헤란로 123번길 45', '010-1111-1111', '김민수', 'minsu.kim@lottemobility.co.kr')",
                "INSERT INTO rent_company (company_name, address, phone, manager_name, manager_email) VALUES ('SK렌터카', '부산광역시 해운대구 해운대해변로 197번길 22', '010-1111-1112', '이지은', 'jieun.lee@skrentcar.com')",
                "INSERT INTO rent_company (company_name, address, phone, manager_name, manager_email) VALUES ('현대캐피탈 장기렌트', '대구광역시 중구 동성로 56번길 78', '010-1111-1113', '박준영', 'junyoung.park@hyundaicapital.com')",
                "INSERT INTO rent_company (company_name, address, phone, manager_name, manager_email) VALUES ('하나캐피탈 장기렌트', '인천광역시 남동구 인하로 321번길 12', '010-1111-1114', '최서연', 'seoyeon.choi@hanacapital.co.kr')",
                "INSERT INTO rent_company (company_name, address, phone, manager_name, manager_email) VALUES ('AJ렌터카', '광주광역시 서구 상무대로 890번길 34', '010-1111-1115', '정우진', 'woojin.jeong@ajrentcar.com')",
                "INSERT INTO rent_company (company_name, address, phone, manager_name, manager_email) VALUES ('제주렌트카', '대전광역시 유성구 대학로 99번길 67', '010-1111-1116', '한소영', 'soyoung.han@jejurentcar.co.kr')",
                "INSERT INTO rent_company (company_name, address, phone, manager_name, manager_email) VALUES ('스타모빌리티', '울산광역시 남구 삼산로 456번길 89', '010-1111-1117', '오태현', 'taehyun.oh@starmobility.co.kr')",
                "INSERT INTO rent_company (company_name, address, phone, manager_name, manager_email) VALUES ('블루드라이브', '경기도 수원시 팔달구 정조로 234번길 15', '010-1111-1118', '강다연', 'dayeon.kang@bluedrive.co.kr')",
                "INSERT INTO rent_company (company_name, address, phone, manager_name, manager_email) VALUES ('이지렌탈', '강원도 춘천시 소양로 678번길 41', '010-1111-1119', '윤재훈', 'jaehoon.yoon@easyrental.co.kr')",
                "INSERT INTO rent_company (company_name, address, phone, manager_name, manager_email) VALUES ('모션카', '전라북도 전주시 덕진구 백제대로 567번길 23', '010-1111-1120', '조하린', 'harin.cho@motioncar.co.kr')",
                "INSERT INTO rent_company (company_name, address, phone, manager_name, manager_email) VALUES ('트립메이트', '경상남도 창원시 의창구 원이대로 345번길 56', '010-1111-1121', '신동혁', 'donghyuk.shin@tripmate.co.kr')",
                "INSERT INTO rent_company (company_name, address, phone, manager_name, manager_email) VALUES ('넥스트라이드', '제주특별자치도 제주시 연동로 789번길 78', '010-1111-1122', '류지민', 'jimin.ryu@nextride.co.kr')"
        };

        Object[][] campingcar = {
                {"프리로드", "서울32가1234", 4, "images/campingcar1.jpg", "4인 취침, 주방(싱크대, 냉장고), 샤워실, 2.2L 디젤", 120000, "2024-03-15", 1},
                {"윈드밴", "부산17나5678", 3, "images/campingcar2.jpg", "3인 취침, 소형 주방, 루프탑 텐트, 1.0L 가솔린", 95000, "2024-02-20", 2},
                {"스타캠프", "대구88다9012", 5, "images/campingcar3.jpg", "5인 취침, 야외 샤워, 태양광 패널, 3.6L 가솔린", 140000, "2024-04-10", 3},
                {"트레일러너", "인천22라3456", 4, "images/campingcar4.jpg", "4인 취침, 주방(가스레인지), 화장실, 2.3L 디젤", 130000, "2024-01-25", 4},
                {"로밍홈", "광주44마7890", 3, "images/campingcar1.jpg", "3인 취침, 접이식 테이블, 외부 오닝, 2.2L 디젤", 110000, "2024-05-05", 5},
                {"아웃도어", "대전66바1234", 4, "images/campingcar2.jpg", "4인 취침, 소형 주방, 루프탑 캐리어, 2.0L 디젤", 100000, "2024-03-30", 6},
                {"글램퍼", "울산77사5678", 5, "images/campingcar3.jpg", "5인 취침, 대형 냉장고, 샤워실, 3.0L 디젤", 150000, "2024-06-12", 7},
                {"블루트립", "경기99아9012", 4, "images/campingcar4.jpg", "4인 취침, 전기차 기반, 태양광 충전, 100kW 전기모터", 160000, "2024-07-01", 8},
                {"이지캠퍼", "강원33자3456", 3, "images/campingcar1.jpg", "3인 취침, 컴팩트 주방, 접이식 침대, 1.5L 가솔린", 90000, "2024-02-15", 9},
                {"모션밴", "전북55차7890", 4, "images/campingcar2.jpg", "4인 취침, 화장실, 외부 BBQ 공간, 2.5L 디젤", 125000, "2024-04-20", 10},
                {"트립스타", "경남88카1234", 5, "images/campingcar3.jpg", "5인 취침, 주방(전자레인지), TV, 3.2L 디젤", 145000, "2024-05-25", 11},
                {"넥스트밴", "제주99타5678", 4, "images/campingcar4.jpg", "4인 취침, 루프탑 텐트, 샤워실, 2.4L 디젤", 135000, "2024-06-30", 12},
                {"어반트레일", "서울78라9012", 4, "images/campingcar1.jpg", "4인 취침, 주방(싱크대, 전자레인지), 샤워실, 2.4L 디젤", 125000, "2024-09-12", 1},
                {"코스트밴", "부산56마3456", 3, "images/campingcar2.jpg", "3인 취침, 소형 주방, 루프탑 텐트, 1.6L 가솔린", 92000, "2024-08-18", 2},
                {"리버캠퍼", "대구23바7890", 5, "images/campingcar3.jpg", "5인 취침, 대형 오닝, 화장실, 3.2L 디젤", 145000, "2024-10-05", 3},
                {"브리즈로버", "인천45사1234", 4, "images/campingcar4.jpg", "4인 취침, 주방(가스레인지), 루프탑 캐리어, 2.3L 디젤", 130000, "2024-07-22", 4},
                {"메도우밴", "광주67아5678", 3, "images/campingcar1.jpg", "3인 취침, 접이식 테이블, 외부 샤워, 1.5L 가솔린", 90000, "2024-11-08", 5},
                {"오름캠퍼", "제주89타9012", 4, "images/campingcar2.jpg", "4인 취침, 소형 주방, 샤워실, 2.0L 디젤", 115000, "2024-09-25", 6},
                {"나이트스타", "울산12자3456", 5, "images/campingcar3.jpg", "5인 취침, 대형 냉장고, 화장실, 3.0L 디젤", 150000, "2024-06-28", 7},
                {"솔라트립", "경기34카7890", 4, "images/campingcar4.jpg", "4인 취침, 전기차 기반, 태양광 충전, 100kW 전기모터", 160000, "2024-12-03", 8},
                {"스트림캠프", "강원56하1234", 3, "images/campingcar1.jpg", "3인 취침, 컴팩트 주방, 접이식 침대, 1.4L 가솔린", 91000, "2024-08-05", 9},
                {"모빌로밍", "전북78파5678", 4, "images/campingcar2.jpg", "4인 취침, 외부 BBQ 공간, 샤워실, 2.5L 디젤", 128000, "2024-10-15", 10},
                {"패스파인더", "경남90타9012", 5, "images/campingcar3.jpg", "5인 취침, 주방(전자레인지), TV, 3.2L 디젤", 148000, "2024-07-10", 11},
                {"코럴로버", "제주12파3456", 4, "images/campingcar5.jpg", "4인 취침, 루프탑 텐트, 샤워실, 2.4L 디젤", 132000, "2024-11-20", 12}
        };

        String[] customer = {
                "INSERT INTO customer (login_id, password, driver_license, customer_name, address, phone, email, previous_date, previous_car) VALUES ('user1', 'user1', '11-24-123456-78', '김서준', '서울특별시 강남구 논현로 20길 12', '010-2345-6789', 'seojun.kim@gmail.com', '2024-01-03', '서울32가1234')",
                "INSERT INTO customer (login_id, password, driver_license, customer_name, address, phone, email, previous_date, previous_car) VALUES ('user2', 'user2', '26-23-234567-89', '박소연', '부산광역시 해운대구 해운대해변로 30길 8', '010-3456-7890', 'soyeon.park@naver.com', '2024-01-04', '대구88다9012')",
                "INSERT INTO customer (login_id, password, driver_license, customer_name, address, phone, email, previous_date, previous_car) VALUES ('user3', 'user3', '27-22-345678-90', '이준호', '대구광역시 중구 국채보상로 15길 25', '010-4567-8901', 'junho.lee@daum.net', '2024-01-08', '광주44마7890')",
                "INSERT INTO customer (login_id, password, driver_license, customer_name, address, phone, email, previous_date, previous_car) VALUES ('user4', 'user4', '28-21-456789-01', '최지민', '인천광역시 남동구 논현로 10길 18', '010-5678-9012', 'jimin.choi@gmail.com', '2024-01-07', '경기99아9012')",
                "INSERT INTO customer (login_id, password, driver_license, customer_name, address, phone, email, previous_date, previous_car) VALUES ('user5', 'user5', '29-20-567890-12', '정하윤', '광주광역시 서구 상무중앙로 25길 7', '010-6789-0123', 'hayoon.jeong@naver.com', '2024-01-10', '전북55차7890')",
                "INSERT INTO customer (login_id, password, driver_license, customer_name, address, phone, email, previous_date, previous_car) VALUES ('user6', 'user6', '30-19-678901-23', '한예린', '대전광역시 유성구 과학로 12길 30', '010-7890-1234', 'yerin.han@daum.net', '2024-01-13', '제주99타5678')",
                "INSERT INTO customer (login_id, password, driver_license, customer_name, address, phone, email, previous_date, previous_car) VALUES ('user7', 'user7', '31-18-789012-34', '오시우', '울산광역시 남구 중앙로 20길 15', '010-8901-2345', 'siwoo.oh@gmail.com', '2024-02-16', '서울32가1234')",
                "INSERT INTO customer (login_id, password, driver_license, customer_name, address, phone, email, previous_date, previous_car) VALUES ('user8', 'user8', '13-17-890123-45', '강지안', '경기도 수원시 영통구 매봉로 15길 10', '010-9012-3456', 'jian.kang@naver.com', '2024-02-13', '인천45사1234')",
                "INSERT INTO customer (login_id, password, driver_license, customer_name, address, phone, email, previous_date, previous_car) VALUES ('user9', 'user9', '32-16-901234-56', '윤도현', '강원도 춘천시 중앙로 10길 22', '010-0123-4567', 'dohyun.yoon@daum.net', '2024-02-15', '울산12자3456')",
                "INSERT INTO customer (login_id, password, driver_license, customer_name, address, phone, email, previous_date, previous_car) VALUES ('user10', 'user10', '33-15-012345-67', '조유진', '전라북도 전주시 완산구 효자로 8길 17', '010-1234-5678', 'yujin.cho@gmail.com', '2024-02-11', '대구88다9012')",
                "INSERT INTO customer (login_id, password, driver_license, customer_name, address, phone, email, previous_date, previous_car) VALUES ('user11', 'user11', '34-14-123456-78', '신하람', '경상남도 창원시 성산구 상남로 12길 20', '010-2345-6781', 'haram.shin@naver.com', '2024-02-06', '제주12파3456')",
                "INSERT INTO customer (login_id, password, driver_license, customer_name, address, phone, email, previous_date, previous_car) VALUES ('user12', 'user12', '14-13-234567-89', '류서아', '제주특별자치도 제주시 애월로 15길 9', '010-3456-7891', 'seoa.ryu@daum.net', '2024-02-22', '광주44마7890')"
        };

        String[] employee = {
                "INSERT INTO employee (employee_name, phone, address, salary, num_dependents, department, employee_role) VALUES ('김태현', '010-2000-0001', '서울특별시 성동구 왕십리로 10길 5', 3000000, 2, '정비부', '정비')",
                "INSERT INTO employee (employee_name, phone, address, salary, num_dependents, department, employee_role) VALUES ('이수민', '010-2000-0002', '부산광역시 수영구 광안로 30', 3100000, 1, '정비부', '정비')",
                "INSERT INTO employee (employee_name, phone, address, salary, num_dependents, department, employee_role) VALUES ('박준형', '010-2000-0003', '대구광역시 북구 침산로 22길 12', 3200000, 0, '정비부', '정비')",
                "INSERT INTO employee (employee_name, phone, address, salary, num_dependents, department, employee_role) VALUES ('최지우', '010-2000-0004', '인천광역시 연수구 송도과학로 70', 3300000, 1, '정비부', '정비')",
                "INSERT INTO employee (employee_name, phone, address, salary, num_dependents, department, employee_role) VALUES ('정우석', '010-2000-0005', '광주광역시 남구 봉선중앙로 48', 2900000, 3, '정비부', '정비')",
                "INSERT INTO employee (employee_name, phone, address, salary, num_dependents, department, employee_role) VALUES ('한서영', '010-2000-0006', '대전광역시 서구 둔산대로 118', 3000000, 0, '관리부', '관리')",
                "INSERT INTO employee (employee_name, phone, address, salary, num_dependents, department, employee_role) VALUES ('오지민', '010-2000-0007', '울산광역시 중구 종가로 9', 2800000, 2, '관리부', '사무')",
                "INSERT INTO employee (employee_name, phone, address, salary, num_dependents, department, employee_role) VALUES ('강하늘', '010-2000-0008', '경기도 성남시 분당구 정자일로 54', 3100000, 1, '관리부', '관리')",
                "INSERT INTO employee (employee_name, phone, address, salary, num_dependents, department, employee_role) VALUES ('윤가람', '010-2000-0009', '강원도 원주시 단구로 87', 3050000, 0, '정비부', '정비')",
                "INSERT INTO employee (employee_name, phone, address, salary, num_dependents, department, employee_role) VALUES ('조윤호', '010-2000-0010', '전라북도 전주시 덕진구 백제대로 111', 3150000, 2, '정비부', '정비')",
                "INSERT INTO employee (employee_name, phone, address, salary, num_dependents, department, employee_role) VALUES ('신유진', '010-2000-0011', '경상남도 진주시 진주대로 144', 3250000, 1, '정비부', '정비')",
                "INSERT INTO employee (employee_name, phone, address, salary, num_dependents, department, employee_role) VALUES ('류성훈', '010-2000-0012', '제주특별자치도 제주시 중앙로 12길 15', 3350000, 0, '정비부', '정비')"
        };

        String[] parts = {
                "INSERT INTO parts (part_name, price, quantity, arrival_date, supplier) VALUES ('타이어', 100000, 20, '2025-01-05', '타이어랜드')",
                "INSERT INTO parts (part_name, price, quantity, arrival_date, supplier) VALUES ('배터리', 80000, 15, '2025-01-08', '배터리마트')",
                "INSERT INTO parts (part_name, price, quantity, arrival_date, supplier) VALUES ('오일필터', 15000, 50, '2025-01-12', '오일존')",
                "INSERT INTO parts (part_name, price, quantity, arrival_date, supplier) VALUES ('에어컨필터', 20000, 40, '2025-01-15', '필터하우스')",
                "INSERT INTO parts (part_name, price, quantity, arrival_date, supplier) VALUES ('브레이크패드', 60000, 25, '2025-01-20', '브레이크샵')",
                "INSERT INTO parts (part_name, price, quantity, arrival_date, supplier) VALUES ('냉각수', 30000, 30, '2025-01-25', '냉각마트')",
                "INSERT INTO parts (part_name, price, quantity, arrival_date, supplier) VALUES ('라이트', 50000, 12, '2025-02-01', '빛전자')",
                "INSERT INTO parts (part_name, price, quantity, arrival_date, supplier) VALUES ('타이밍벨트', 120000, 10, '2025-02-07', '정비왕')",
                "INSERT INTO parts (part_name, price, quantity, arrival_date, supplier) VALUES ('엔진오일', 25000, 45, '2025-02-15', '오일마트')",
                "INSERT INTO parts (part_name, price, quantity, arrival_date, supplier) VALUES ('연료필터', 17000, 35, '2025-02-20', '필터코리아')",
                "INSERT INTO parts (part_name, price, quantity, arrival_date, supplier) VALUES ('점화플러그', 22000, 28, '2025-02-25', '전기정비')",
                "INSERT INTO parts (part_name, price, quantity, arrival_date, supplier) VALUES ('와이퍼', 12000, 60, '2025-03-01', '와이퍼샵')"
        };

        String[] selfMaintenance = {
                "INSERT INTO self_maintenance (date, duration, campingcar_id, part_id, employee_id) VALUES ('2024-03-01', 2, 1, 1, 1)",
                "INSERT INTO self_maintenance (date, duration, campingcar_id, part_id, employee_id) VALUES ('2024-03-03', 1, 1, 2, 2)",
                "INSERT INTO self_maintenance (date, duration, campingcar_id, part_id, employee_id) VALUES ('2024-03-05', 2, 3, 3, 3)",
                "INSERT INTO self_maintenance (date, duration, campingcar_id, part_id, employee_id) VALUES ('2024-03-07', 1, 5, 4, 4)",
                "INSERT INTO self_maintenance (date, duration, campingcar_id, part_id, employee_id) VALUES ('2024-03-09', 2, 5, 5, 5)",
                "INSERT INTO self_maintenance (date, duration, campingcar_id, part_id, employee_id) VALUES ('2024-03-11', 1, 8, 6, 6)",
                "INSERT INTO self_maintenance (date, duration, campingcar_id, part_id, employee_id) VALUES ('2024-04-13', 2, 12, 7, 7)",
                "INSERT INTO self_maintenance (date, duration, campingcar_id, part_id, employee_id) VALUES ('2024-04-15', 1, 16, 8, 8)",
                "INSERT INTO self_maintenance (date, duration, campingcar_id, part_id, employee_id) VALUES ('2024-04-17', 2, 20, 9, 9)",
                "INSERT INTO self_maintenance (date, duration, campingcar_id, part_id, employee_id) VALUES ('2024-04-19', 3, 24, 10, 10)",
                "INSERT INTO self_maintenance (date, duration, campingcar_id, part_id, employee_id) VALUES ('2024-04-21', 1, 24, 11, 11)",
                "INSERT INTO self_maintenance (date, duration, campingcar_id, part_id, employee_id) VALUES ('2024-04-23', 1, 1, 12, 12)"
        };

        String[] rental = {
                "INSERT INTO rental (rental_start, rental_period, fee, deadline, additional_detail, additional_fee, campingcar_id, company_id, driver_license) VALUES ('2024-01-01', 3, 360000, '2024-02-01', '없음', 0, 1, 1, '11-24-123456-78')",
                "INSERT INTO rental (rental_start, rental_period, fee, deadline, additional_detail, additional_fee, campingcar_id, company_id, driver_license) VALUES ('2024-01-03', 2, 280000, '2024-02-03', '와이파이 포함', 10000, 3, 3, '26-23-234567-89')",
                "INSERT INTO rental (rental_start, rental_period, fee, deadline, additional_detail, additional_fee, campingcar_id, company_id, driver_license) VALUES ('2024-01-05', 4, 440000, '2024-02-05', NULL, NULL, 5, 5, '27-22-345678-90')",
                "INSERT INTO rental (rental_start, rental_period, fee, deadline, additional_detail, additional_fee, campingcar_id, company_id, driver_license) VALUES ('2024-01-07', 1, 160000, '2024-02-07', '단기렌트', 0, 8, 8, '28-21-456789-01')",
                "INSERT INTO rental (rental_start, rental_period, fee, deadline, additional_detail, additional_fee, campingcar_id, company_id, driver_license) VALUES ('2024-01-09', 2, 260000, '2024-02-09', NULL, NULL, 10, 10, '29-20-567890-12')",
                "INSERT INTO rental (rental_start, rental_period, fee, deadline, additional_detail, additional_fee, campingcar_id, company_id, driver_license) VALUES ('2024-01-11', 3, 390000, '2024-02-11', '캠핑 장비 포함', 30000, 12, 12, '30-19-678901-23')",
                "INSERT INTO rental (rental_start, rental_period, fee, deadline, additional_detail, additional_fee, campingcar_id, company_id, driver_license) VALUES ('2024-02-15', 2, 250000, '2024-03-15', '보험 포함', 15000, 1, 1, '31-18-789012-34')",
                "INSERT INTO rental (rental_start, rental_period, fee, deadline, additional_detail, additional_fee, campingcar_id, company_id, driver_license) VALUES ('2024-02-10', 4, 520000, '2024-03-10', NULL, NULL, 16, 4, '13-17-890123-45')",
                "INSERT INTO rental (rental_start, rental_period, fee, deadline, additional_detail, additional_fee, campingcar_id, company_id, driver_license) VALUES ('2024-02-15', 1, 160000, '2024-03-15', NULL, NULL, 20, 8, '32-16-901234-56')",
                "INSERT INTO rental (rental_start, rental_period, fee, deadline, additional_detail, additional_fee, campingcar_id, company_id, driver_license) VALUES ('2024-02-10', 2, 280000, '2024-03-10', NULL, NULL, 3, 3, '33-15-012345-67')",
                "INSERT INTO rental (rental_start, rental_period, fee, deadline, additional_detail, additional_fee, campingcar_id, company_id, driver_license) VALUES ('2024-02-05', 2, 270000, '2024-03-05', NULL, NULL, 24, 11, '34-14-123456-78')",
                "INSERT INTO rental (rental_start, rental_period, fee, deadline, additional_detail, additional_fee, campingcar_id, company_id, driver_license) VALUES ('2024-02-20', 3, 400000, '2024-03-20', '와이파이 포함', 8000, 5, 5, '14-13-234567-89')",
                "INSERT INTO rental (rental_start, rental_period, fee, deadline, additional_detail, additional_fee, campingcar_id, company_id, driver_license) VALUES ('2024-03-01', 3, 450000, '2024-04-01', '프리미엄 보험 포함', 20000, 2, 2, '11-24-123456-78')",
                "INSERT INTO rental (rental_start, rental_period, fee, deadline, additional_detail, additional_fee, campingcar_id, company_id, driver_license) VALUES ('2024-03-05', 2, 380000, '2024-04-05', NULL, NULL, 4, 4, '26-23-234567-89')",
                "INSERT INTO rental (rental_start, rental_period, fee, deadline, additional_detail, additional_fee, campingcar_id, company_id, driver_license) VALUES ('2024-03-10', 4, 600000, '2024-04-10', '캠핑 장비 세트 포함', 35000, 6, 6, '27-22-345678-90')",
                "INSERT INTO rental (rental_start, rental_period, fee, deadline, additional_detail, additional_fee, campingcar_id, company_id, driver_license) VALUES ('2024-03-15', 2, 400000, '2024-04-15', '와이파이 포함', 10000, 7, 7, '28-21-456789-01')",
                "INSERT INTO rental (rental_start, rental_period, fee, deadline, additional_detail, additional_fee, campingcar_id, company_id, driver_license) VALUES ('2024-04-01', 3, 480000, '2024-05-01', '프리미엄 보험 포함', 25000, 9, 9, '29-20-567890-12')",
                "INSERT INTO rental (rental_start, rental_period, fee, deadline, additional_detail, additional_fee, campingcar_id, company_id, driver_license) VALUES ('2024-04-05', 2, 360000, '2024-05-05', '내비게이션 포함', 15000, 11, 11, '30-19-678901-23')",
                "INSERT INTO rental (rental_start, rental_period, fee, deadline, additional_detail, additional_fee, campingcar_id, company_id, driver_license) VALUES ('2024-04-10', 4, 550000, '2024-05-10', NULL, NULL, 13, 1, '31-18-789012-34')",
                "INSERT INTO rental (rental_start, rental_period, fee, deadline, additional_detail, additional_fee, campingcar_id, company_id, driver_license) VALUES ('2024-04-15', 2, 390000, '2024-05-15', '와이파이 포함', 10000, 15, 3, '13-17-890123-45')",
                "INSERT INTO rental (rental_start, rental_period, fee, deadline, additional_detail, additional_fee, campingcar_id, company_id, driver_license) VALUES ('2024-04-20', 3, 460000, '2024-05-20', NULL, NULL, 17, 5, '32-16-901234-56')",
                "INSERT INTO rental (rental_start, rental_period, fee, deadline, additional_detail, additional_fee, campingcar_id, company_id, driver_license) VALUES ('2025-05-31', 60, 420000, '2025-06-30', '캠핑 장비 포함', 20000, 19, 7, '11-24-123456-78')"
        };

        String[] externalShop = {
                "INSERT INTO external_maintenance_shop (shop_name, address, phone, manager_name, manager_email) VALUES ('강남오토정비', '서울특별시 강남구 역삼로 101', '010-3000-0001', '김상호', 'sangho.kim@autosvc.com')",
                "INSERT INTO external_maintenance_shop (shop_name, address, phone, manager_name, manager_email) VALUES ('해운대모터스', '부산광역시 해운대구 해운대로 202', '010-3000-0002', '박소영', 'soyoung.park@hdmotors.co.kr')",
                "INSERT INTO external_maintenance_shop (shop_name, address, phone, manager_name, manager_email) VALUES ('대구카센타', '대구광역시 수성구 달구벌대로 155', '010-3000-0003', '이정훈', 'junghoon.lee@dgcar.com')",
                "INSERT INTO external_maintenance_shop (shop_name, address, phone, manager_name, manager_email) VALUES ('인천정비월드', '인천광역시 남동구 인하로 77', '010-3000-0004', '최지우', 'jiwoo.choi@icrepair.net')",
                "INSERT INTO external_maintenance_shop (shop_name, address, phone, manager_name, manager_email) VALUES ('광주오토케어', '광주광역시 북구 무등로 21', '010-3000-0005', '정민재', 'minjae.jeong@gjautocare.com')",
                "INSERT INTO external_maintenance_shop (shop_name, address, phone, manager_name, manager_email) VALUES ('대전카정비센터', '대전광역시 유성구 궁동로 45', '010-3000-0006', '한예진', 'yejin.han@djautosvc.kr')",
                "INSERT INTO external_maintenance_shop (shop_name, address, phone, manager_name, manager_email) VALUES ('울산정비센터', '울산광역시 중구 태화로 68', '010-3000-0007', '오승현', 'seunghyun.oh@usrepair.com')",
                "INSERT INTO external_maintenance_shop (shop_name, address, phone, manager_name, manager_email) VALUES ('수원모터정비', '경기도 수원시 장안구 정자동 12-3', '010-3000-0008', '강지윤', 'jiyoon.kang@swmotor.net')",
                "INSERT INTO external_maintenance_shop (shop_name, address, phone, manager_name, manager_email) VALUES ('춘천카텍', '강원도 춘천시 중앙로 220', '010-3000-0009', '윤대성', 'daesung.yoon@cctech.kr')",
                "INSERT INTO external_maintenance_shop (shop_name, address, phone, manager_name, manager_email) VALUES ('전주오토베스트', '전라북도 전주시 완산구 전주대로 88', '010-3000-0010', '조유림', 'yurim.cho@jbautobest.com')",
                "INSERT INTO external_maintenance_shop (shop_name, address, phone, manager_name, manager_email) VALUES ('창원카서비스', '경상남도 창원시 의창구 창원대로 112', '010-3000-0011', '신하늘', 'haneul.shin@cwautosvc.com')",
                "INSERT INTO external_maintenance_shop (shop_name, address, phone, manager_name, manager_email) VALUES ('제주정비마스터', '제주특별자치도 제주시 연북로 305', '010-3000-0012', '류지안', 'jian.ryu@jejumaster.com')"
        };

        String[] externalRequest = {
                "INSERT INTO external_maintenance_request (maintenance_detail, maintenance_date, fee, deadline, additional_detail, campingcar_id, shop_id, company_id, driver_license) VALUES ('엔진오일 교체', '2024-05-25', 85000, '2024-06-25', '합성유 사용', 1, 1, 1, '11-24-123456-78')",
                "INSERT INTO external_maintenance_request (maintenance_detail, maintenance_date, fee, deadline, additional_detail, campingcar_id, shop_id, company_id, driver_license) VALUES ('타이어 교체', '2024-05-20', 90000, '2024-06-20', NULL, 3, 3, 3, '26-23-234567-89')",
                "INSERT INTO external_maintenance_request (maintenance_detail, maintenance_date, fee, deadline, additional_detail, campingcar_id, shop_id, company_id, driver_license) VALUES ('브레이크 패드 점검', '2024-05-15', 70000, '2024-06-15', '앞 브레이크만', 5, 5, 5, '27-22-345678-90')",
                "INSERT INTO external_maintenance_request (maintenance_detail, maintenance_date, fee, deadline, additional_detail, campingcar_id, shop_id, company_id, driver_license) VALUES ('실내 세차', '2024-05-12', 30000, '2024-06-12', NULL, 8, 8, 8, '28-21-456789-01')",
                "INSERT INTO external_maintenance_request (maintenance_detail, maintenance_date, fee, deadline, additional_detail, campingcar_id, shop_id, company_id, driver_license) VALUES ('에어컨 필터 교체', '2024-05-05', 60000, '2024-06-05', '정품 사용', 10, 10, 10, '29-20-567890-12')",
                "INSERT INTO external_maintenance_request (maintenance_detail, maintenance_date, fee, deadline, additional_detail, campingcar_id, shop_id, company_id, driver_license) VALUES ('타이밍벨트 교체', '2024-05-08', 120000, '2024-06-08', NULL, 12, 12, 12, '30-19-678901-23')",
                "INSERT INTO external_maintenance_request (maintenance_detail, maintenance_date, fee, deadline, additional_detail, campingcar_id, shop_id, company_id, driver_license) VALUES ('외부 흠집 복원', '2024-06-05', 95000, '2024-07-05', NULL, 1, 1, 1, '31-18-789012-34')",
                "INSERT INTO external_maintenance_request (maintenance_detail, maintenance_date, fee, deadline, additional_detail, campingcar_id, shop_id, company_id, driver_license) VALUES ('냉각수 보충', '2024-06-06', 40000, '2024-07-06', NULL, 16, 4, 4, '13-17-890123-45')",
                "INSERT INTO external_maintenance_request (maintenance_detail, maintenance_date, fee, deadline, additional_detail, campingcar_id, shop_id, company_id, driver_license) VALUES ('전조등 교체', '2024-06-10', 55000, '2024-07-10', 'LED로 업그레이드', 20, 8, 8, '32-16-901234-56')",
                "INSERT INTO external_maintenance_request (maintenance_detail, maintenance_date, fee, deadline, additional_detail, campingcar_id, shop_id, company_id, driver_license) VALUES ('배터리 점검', '2024-06-04', 60000, '2024-07-04', NULL, 3, 3, 3, '33-15-012345-67')",
                "INSERT INTO external_maintenance_request (maintenance_detail, maintenance_date, fee, deadline, additional_detail, campingcar_id, shop_id, company_id, driver_license) VALUES ('오일 누유 점검', '2024-06-02', 75000, '2024-07-02', NULL, 24, 11, 11, '34-14-123456-78')",
                "INSERT INTO external_maintenance_request (maintenance_detail, maintenance_date, fee, deadline, additional_detail, campingcar_id, shop_id, company_id, driver_license) VALUES ('샤워실 수압 문제', '2024-06-24', 65000, '2024-07-24', '배관 수리 포함', 5, 5, 5, '14-13-234567-89')",
                "INSERT INTO external_maintenance_request (maintenance_detail, maintenance_date, fee, deadline, additional_detail, campingcar_id, shop_id, company_id, driver_license) VALUES ('타이어 교체', '2024-07-01', 100000, '2024-08-01', '고급 타이어 사용', 9, 9, 9, '29-20-567890-12')",
                "INSERT INTO external_maintenance_request (maintenance_detail, maintenance_date, fee, deadline, additional_detail, campingcar_id, shop_id, company_id, driver_license) VALUES ('엔진 점검', '2024-07-05', 80000, '2024-08-05', NULL, 11, 11, 11, '30-19-678901-23')",
                "INSERT INTO external_maintenance_request (maintenance_detail, maintenance_date, fee, deadline, additional_detail, campingcar_id, shop_id, company_id, driver_license) VALUES ('외부 도색', '2024-07-10', 120000, '2024-08-10', '스크래치 복원', 13, 1, 1, '31-18-789012-34')",
                "INSERT INTO external_maintenance_request (maintenance_detail, maintenance_date, fee, deadline, additional_detail, campingcar_id, shop_id, company_id, driver_license) VALUES ('브레이크 점검', '2024-07-15', 70000, '2024-08-15', NULL, 15, 3, 3, '13-17-890123-45')",
                "INSERT INTO external_maintenance_request (maintenance_detail, maintenance_date, fee, deadline, additional_detail, campingcar_id, shop_id, company_id, driver_license) VALUES ('냉각수 교체', '2024-07-20', 60000, '2024-08-20', '프리미엄 냉각수', 17, 5, 5, '32-16-901234-56')",
                "INSERT INTO external_maintenance_request (maintenance_detail, maintenance_date, fee, deadline, additional_detail, campingcar_id, shop_id, company_id, driver_license) VALUES ('에어컨 수리', '2024-07-25', 90000, '2024-08-25', NULL, 19, 7, 7, '33-15-012345-67')"
        };

        try {
            Statement stmt = conn.createStatement();

            for (String query : rentCompany) {
                stmt.executeUpdate(query);
            }

            String campingCarSql = "INSERT INTO camping_car (car_name, car_number, capacity, image, car_detail, rental_fee, registration_date, company_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(campingCarSql);
            for (Object[] data : campingcar) {
                pstmt.setString(1, (String) data[0]);
                pstmt.setString(2, (String) data[1]);
                pstmt.setInt(3, (Integer) data[2]);

                File imageFile = new File((String) data[3]);
                FileInputStream fin = null;
                if (imageFile.exists()) {
                    try {
                        fin = new FileInputStream(imageFile);
                        pstmt.setBinaryStream(4, fin);
                    } catch (IOException ex) {
                        System.out.println("이미지 파일 읽기 오류: " + imageFile.getAbsolutePath());
                        ex.printStackTrace();
                        pstmt.setNull(4, BLOB);
                    }
                } else {
                    pstmt.setNull(4, BLOB);
                    System.out.println("이미지 파일 없음: " + data[3]);
                }
                pstmt.setString(5, (String) data[4]);
                pstmt.setInt(6, (Integer) data[5]);
                pstmt.setString(7, (String) data[6]);
                pstmt.setInt(8, (Integer) data[7]);
                pstmt.executeUpdate();
            }
            pstmt.close();

            String[][] otherQueries = {customer, employee, parts, selfMaintenance, rental, externalShop, externalRequest};
            for (String[] queries : otherQueries) {
                for (String query : queries) {
                    stmt.executeUpdate(query);
                }
            }

            System.out.println("각 테이블 초기 데이터 삽입 완료");

        } catch (SQLException e) {
            System.out.println("SQL 실행오류");
            e.printStackTrace();
        }
    }
}