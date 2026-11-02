# PetMatch — Java Desktop / MySQL 8.4

Java 17+, Maven 3.9+, MySQL Server 8.4, Python 3.10+. Mở cả thư mục chứa pom.xml bằng VS Code. Tất cả lệnh dưới đây chạy từ thư mục đó.

## 1. Kiểm tra MySQL trên Windows
```powershell
Get-Service MySQL84
Start-Service MySQL84  # PowerShell Admin nếu service chưa chạy
Test-NetConnection 127.0.0.1 -Port 3306
```
Nếu không có service tên này: `Get-Service *mysql*`. MySQL Workbench là công cụ client; vẫn cần Server đang chạy.

## 2. Cấu hình
File `.env` gốc được giữ lại từ dự án của bạn. Kiểm tra 5 dòng DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD. Nếu thiếu file:
```powershell
Copy-Item .env.example .env
```
Đặt DB_HOST=127.0.0.1, DB_PORT=3306, DB_NAME=pet_matcher_db; nhập tài khoản và mật khẩu MySQL thực tế. Không nhập MySQL84 vào DB_NAME. Mật khẩu có @ được giữ nguyên.
Java ưu tiên environment variables > .env > classpath application.properties > mặc định. Python ưu tiên environment > .env > mặc định. Dùng .env thống nhất cho cả hai; không tạo bản mật khẩu trong resources.
Không gửi hoặc commit .env. Nếu biến môi trường cũ ghi đè cấu hình: kiểm tra bằng `Get-ChildItem Env:DB_*` (có thể hiện mật khẩu, không chia sẻ output).

## 3. Tạo schema
Trong MySQL Workbench, mở và chạy theo thứ tự:
1. database/01_create_database.sql
2. database/02_schema.sql
3. database/03_constraints.sql
4. database/05_views.sql

Bỏ qua 04_seed_data.sql vì sẽ dùng Python. Script USE cố định pet_matcher_db; nếu đổi tên DB phải sửa USE tương ứng. CREATE TABLE IF NOT EXISTS không tự migrate schema cũ: backup trước, kiểm tra cấu trúc nếu có database cũ.

## 4. Python: kiểm tra và seed
```powershell
python -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install -r requirements.txt
python scripts/check_db.py
python scripts/seed_data.py --dry-run --pets 200 --families 80 --applications 150
python scripts/seed_data.py --pets 200 --families 80 --applications 150
```
Mặc định: 150 pets + 150 care profiles + 60 families + 100 applications. `--seed 42` giúp dữ liệu tái lập. Mọi dữ liệu là giả. INSERT có tham số, commit một transaction, rollback khi lỗi. Chạy lại sẽ THÊM batch, không xóa hay reset dữ liệu cũ. Applications có thể không phù hợp matching để demo cả pass/fail. Chưa seed placements để bạn tự demo quy trình.

## 5. Test và chạy
```powershell
java -version
mvn -version
python -m unittest discover -s tests -v
mvn clean test
mvn exec:java "-Dexec.mainClass=com.petmatch.config.DatabaseDiagnostics"
mvn exec:java
```
Có thể chạy `powershell -ExecutionPolicy Bypass -File tests/run_all.ps1`. Java tests ở src/test/java theo chuẩn Maven; tests/ chứa Python tests và runner. Kết quả Maven ở target/surefire-reports.
Đây là desktop app; không cần mở URL trong trình duyệt. Chỉ MySQL dùng localhost.

## 6. Chẩn đoán
| Mã / triệu chứng | Xử lý |
|---|---|
| 2003 / communications link failure | Khởi động MySQL84; kiểm tra cổng 3306 |
| 1045 | Sai user/password hoặc quyền host; thử cùng tài khoản trong Workbench với TCP 127.0.0.1 |
| 1049 | DB_NAME chưa tồn tại; chạy 01_create_database.sql |
| 1146 | Thiếu bảng; chạy 02_schema.sql |
| No suitable driver | Chạy bằng Maven để nạp mysql-connector-j, không chạy java Main trực tiếp |
| Lỗi DELIMITER | Dùng file 03 đã sửa, chạy toàn bộ trong Workbench |

## Cấu trúc và kiến thức
- model/: entity, enum; Pet abstract + Dog/Cat minh họa inheritance/polymorphism; CareProfile minh họa composition.
- matching/: Strategy cho tiêu chí, CriterionGroup kết hợp AND/OR.
- service/: validation và điều phối nghiệp vụ; phụ thuộc repository interface để test độc lập.
- repository/: PreparedStatement, try-with-resources; placement transaction + FOR UPDATE.
- config/: cấu hình kết nối và chẩn đoán theo mã lỗi.
- ui/: Swing, event handlers, JTable; bảng chỉ đọc để tránh hiểu nhầm đã lưu.
- scripts/: seed và kiểm tra DB, cấu hình dùng chung.

Đọc docs/UPGRADE_REVIEW_VI.md để biết lỗi đã sửa, giới hạn và checklist tích hợp trước khi bảo vệ.

## Integration test MySQL tùy chọn
Tạo database riêng `pet_matcher_test`, chạy 02_schema.sql sau khi đổi dòng USE sang tên này. Không chạy test trên database demo/production.
```powershell
$env:DB_NAME="pet_matcher_test"
$env:PETMATCH_MYSQL_TEST="true"
mvn test
Remove-Item Env:PETMATCH_MYSQL_TEST
Remove-Item Env:DB_NAME
```
Test tạo fixture, xác nhận placement, kiểm tra đồng bộ các bảng và từ chối xác nhận lại; xóa fixture trong finally. Test tự bỏ qua nếu chưa bật biến trên. Không thay thế các case concurrency/rollback trong checklist.
