# PetMatch G9 — Bản nâng cấp MySQL 8.4

## 1. Phạm vi và cách dùng bộ mã nguồn

Đây là bộ mã nguồn đầy đủ trong phạm vi `src/`, `pom.xml`, `database/` và `scripts/`, xây dựng từ Final_Java.zip mà bạn gửi. Có thêm tài liệu và `.env.example` để chạy độc lập. Không cần chép từng đoạn code trong chat: mỗi file Java/Python/SQL trong gói đều chứa toàn bộ code của file đó.

## 2. Chuẩn bị trên Windows

- JDK 17 hoặc mới hơn có khả năng biên dịch Java 17; kiểm tra `java -version`, `javac -version`.
- Maven; kiểm tra `mvn -version`.
- MySQL Server 8.4 đang chạy tại localhost, mặc định port 3306.
- Python 3; kiểm tra `python --version`.
- Chạy lệnh ở thư mục có `pom.xml`, không chạy từ `src` hoặc `scripts`.

Trong PowerShell:

```powershell
Copy-Item .env.example .env
python -m venv .venv
.\.venv\Scripts\python.exe -m pip install -r scripts/requirements.txt
```

Mở `.env`, điền thông tin MySQL của chính bạn:

```dotenv
DB_HOST=127.0.0.1
DB_PORT=3306
DB_NAME=pet_matcher_db
DB_USER=root
DB_PASSWORD=YOUR_MYSQL_PASSWORD
```

Mật khẩu MySQL và mật khẩu đăng nhập ứng dụng là hai mật khẩu riêng. Không đưa `.env` có mật khẩu thật lên Git. Biến môi trường có độ ưu tiên cao hơn `.env`; Java dùng `application.properties` nếu không có hai nguồn trên. Python dùng `.env`/biến môi trường.

## 3. Chuẩn bị database

Nếu database đã có dữ liệu quan trọng, xuất bản backup bằng MySQL Workbench trước khi chạy migration. Các script không DROP DATABASE/TABLE hoặc tự xóa dữ liệu cũ. Tuy nhiên MySQL DDL có implicit commit: toàn bộ quá trình migration không phải một transaction duy nhất.

```powershell
.\.venv\Scripts\python.exe scripts/init_db.py
.\.venv\Scripts\python.exe scripts/check_db.py
```

`init_db.py` chạy lần lượt SQL 01 → 08, hỗ trợ DELIMITER, và thay tên database theo DB_NAME đã được kiểm tra. Có thể chạy lại; tên bảng/index/trigger/procedure đều được xử lý để chạy lặp trong trạng thái bình thường.

Nếu chạy SQL bằng Workbench, chạy theo số thứ tự. Các SQL file mặc định dùng `pet_matcher_db`; nếu dùng tên khác, sửa CREATE DATABASE/USE tương ứng hoặc dùng `init_db.py`.

Đặc biệt với database cũ:

- Script 07 sẽ từ chối thêm CHECK nếu có enum/trạng thái cũ không hợp lệ.
- Script 08 sẽ từ chối tạo unique index nếu một gia đình đang có nhiều placement ACTIVE.
- Không tự xóa những dữ liệu này. Kiểm tra lịch sử và điều chỉnh có chủ đích, rồi chạy lại migration.
- Truy vấn kiểm tra: `SELECT family_id, COUNT(*) FROM placements WHERE status='ACTIVE' GROUP BY family_id HAVING COUNT(*) > 1;`
- Số đang nuôi cần phản ánh dữ liệu placement thực tế. Bản nâng cấp không tự viết lại số liệu lịch sử.

## 4. Tạo tài khoản đăng nhập ứng dụng

```powershell
.\.venv\Scripts\python.exe scripts/create_admin.py
```

Nhập username, tên hiển thị, mật khẩu ít nhất 12 ký tự và xác nhận. Không có tài khoản hoặc mật khẩu mặc định. Script tạo tài khoản nhân viên trong `staff_users`.

Ứng dụng sử dụng PBKDF2-HMAC-SHA256, salt ngẫu nhiên 16 byte, 210.000 vòng lặp; không lưu mật khẩu rõ. Sai 5 lần sẽ khóa tài khoản 5 phút. Tài khoản có `is_active=FALSE` không được đăng nhập. Tất cả tài khoản hiện có cùng quyền thao tác nghiệp vụ; bản này chưa xây dựng hệ thống phân quyền nhiều vai trò.

## 5. Sinh dữ liệu mẫu và chạy

```powershell
.\.venv\Scripts\python.exe scripts/seed_data.py --dry-run
.\.venv\Scripts\python.exe scripts/seed_data.py --pets 150 --families 60 --applications 100
mvn clean test
mvn exec:java
```

Seed tạo dữ liệu tổng hợp: thú cưng, care profile, gia đình và đơn đăng ký. Chạy lại sẽ thêm một lô mới, không xóa dữ liệu cũ. Đơn mẫu có cả cặp đạt và không đạt để trình diễn kết quả matching. Không dùng dữ liệu mẫu như hồ sơ cứu hộ thật.

Đăng nhập → Thú cưng → Gia đình → Đơn đăng ký → Ghép nối & Bàn giao → Tổng quan. Khi chuyển tab, dữ liệu được tải lại. Nếu thay đổi từ một cửa sổ/tiến trình khác, dùng nút Làm mới.

## 6. Logic nghiệp vụ sau nâng cấp

### Hồ sơ và lưu trữ

- Thú cưng: thêm/sửa tên, loài, giống, tuổi tháng, sức khỏe, tính cách, yêu cầu chăm sóc, nhà ở, trạng thái, vận động, ăn uống và ghi chú y tế.
- Pet và CareProfile được ghi trong cùng transaction; lỗi ghi care sẽ rollback pet.
- Thú cưng PLACED không được sửa để mở lại hoặc nhận placement khác. Pet có đơn liên quan không được xóa do foreign key.
- Gia đình: thêm/sửa thông tin liên hệ, nhà ở, kinh nghiệm, capacity, sân vườn, trẻ em và trạng thái hoạt động.
- Số đang nuôi do transaction bàn giao cập nhật. Form sửa gia đình không ghi đè số đang nuôi từ dữ liệu cũ trên màn hình. Không giảm capacity thấp hơn số đang nuôi thực tế.

### Các tiêu chí

| Tiêu chí | Điều kiện |
|---|---|
| Capacity — bắt buộc | Gia đình đang hoạt động và `activePetCount < capacity` |
| Commitment — bắt buộc | Pet AVAILABLE, gia đình hoạt động và `activePetCount = 0` |
| Housing | ANY chấp nhận mọi loại nhà hợp lệ; HOUSE chấp nhận HOUSE/FARM; APARTMENT yêu cầu APARTMENT; YARD yêu cầu có sân |
| Experience | Kinh nghiệm gia đình phải đạt mức do từng subtype Pet tính ra |
| Household | Gia đình có trẻ nhỏ không phù hợp pet SHY/AGGRESSIVE |

Đây là quy tắc đơn giản hóa cho đồ án, không thay thế đánh giá chăm sóc động vật thực tế.

`Dog.requiredExperience()` yêu cầu EXPERIENCED khi cần chăm sóc đặc biệt/SPECIAL_NEEDS; INTERMEDIATE với chó ACTIVE hoặc dưới 6 tháng; còn lại BEGINNER. `Cat.requiredExperience()` yêu cầu EXPERIENCED cho chăm sóc đặc biệt/SPECIAL_NEEDS; INTERMEDIATE cho mèo SHY; còn lại BEGINNER. Nhờ vậy polymorphism ảnh hưởng trực tiếp đến kết quả matching, không chỉ đổi câu mô tả.

`CriterionGroup` là composite của các `MatchCriterion`: có thể lồng nhóm AND/OR; nhóm rỗng fail, từng kết quả có PASS/FAIL và lý do.

### AND/OR và xác nhận bàn giao

AND/OR kết hợp ba tiêu chí chăm sóc Housing, Experience, Household. Các điều kiện bắt buộc về pet, commitment và capacity luôn nằm ngoài nhóm OR và phải đạt.

OR là chế độ so sánh để demo: có thể PASS khi một yêu cầu chăm sóc khác FAIL. Khi APPROVE hoặc xác nhận bàn giao, hệ thống bắt buộc AND. Giao diện ghi rõ điều này. Không thể dùng OR để bỏ qua nhu cầu chăm sóc khi bàn giao thật.

Danh sách định nghĩa tiêu chí được lưu trong `match_criteria`, đọc lại qua JDBC và dựng thành các đối tượng criterion. Mỗi lần đánh giá lưu operator, kết quả tổng và kết quả từng tiêu chí trong `matching_results`/`matching_criteria_results`. Khi chọn lại đơn, màn hình đọc đánh giá gần nhất từ database. Đánh giá lịch sử không bảo đảm hiện tại vẫn hợp lệ; khi bàn giao luôn đánh giá lại.

### Vòng đời đơn

```text
PENDING → APPROVED / REJECTED / CANCELLED / PLACED
APPROVED → REJECTED / CANCELLED / PLACED
REJECTED, CANCELLED, PLACED là trạng thái kết thúc.
```

PLACED chỉ được đặt thông qua transaction bàn giao. Tạo đơn yêu cầu pet và gia đình đã lưu, đang khả dụng và chưa cam kết. Không cho tạo trùng cặp pet–family khi còn đơn PENDING/APPROVED. Các đơn đã bị từ chối/hủy có thể có một đơn mới nếu pet/family vẫn khả dụng.

### Transaction bàn giao

1. Kiểm tra ID và liên kết application–pet–family.
2. Khóa đơn, pet và gia đình bằng SELECT FOR UPDATE.
3. Đọc lại trạng thái hiện tại, capacity, occupancy, hồ sơ và các criterion được lưu.
4. Đánh giá lại bằng AND; fail thì rollback.
5. Lưu đánh giá; thêm placement; cập nhật pet PLACED; tăng số đang nuôi; chuyển đơn PLACED.
6. Từ chối các đơn PENDING/APPROVED khác của cùng pet hoặc cùng gia đình.
7. Commit toàn bộ. Trả về ID và thời điểm của placement đã lưu.

Unique pet/application và unique active family trong database bổ sung lớp bảo vệ. Hai tiến trình cạnh tranh có thể gặp deadlock/lock timeout và một giao dịch bị rollback; người dùng tải lại dữ liệu rồi thao tác lại. Bản này không tự retry giao dịch.

Theo cách hiểu sát câu “pet or family already committed” của đề, một gia đình nhận một placement đang hoạt động sẽ bị chặn nhận lần hai dù capacity lớn hơn 1. Capacity vẫn được kiểm tra riêng. Nếu thầy muốn một gia đình nhận nhiều pet đến khi đầy, phải thay đổi đồng bộ service, matching, SQL guard và index; không chỉ sửa giao diện.

## 7. Ba demo theo yêu cầu thầy

### Demo 1 — Giải thích đạt/không đạt

- Thêm chó tuổi 12 tháng, CALM, GOOD, không special care, housing HOUSE.
- Thêm gia đình HOUSE, BEGINNER, capacity 2, active, chưa nhận pet.
- Tạo đơn, chọn AND rồi đánh giá: các tiêu chí đều PASS.
- Sửa pet thành special care, quay lại đánh giá: Experience FAIL, có lý do yêu cầu EXPERIENCED.

### Demo 2 — Composite AND/OR và thay đổi tiêu chí

- Với pet special care và gia đình BEGINNER ở trên: AND FAIL, OR có thể PASS vì Housing/Household đạt.
- Thử bàn giao: bị từ chối vì bàn giao yêu cầu AND.
- Sửa gia đình thành EXPERIENCED, đánh giá AND lại: PASS. Chọn lại đơn để đọc kết quả đã lưu.

### Demo 3 — Chặn cam kết lần hai

- Với đơn đạt AND, xác nhận bàn giao.
- Dashboard xuất hiện placement, pet chuyển PLACED, gia đình tăng số đang nuôi, đơn chuyển PLACED.
- Thử bàn giao lại đơn cũ: bị từ chối, không thêm placement hay tăng occupancy.
- Gia đình đã cam kết cũng không còn trong lựa chọn tạo đơn mới, dù capacity còn dư.
- Đóng ứng dụng, mở lại và đăng nhập: dữ liệu vẫn được tải từ MySQL.

## 8. Kiểm thử và phạm vi đã xác minh

Trong môi trường sửa mã:

- Đã biên dịch toàn bộ Java và mã test với Java 17.
- 49 kiểm thử Java chạy thành công, 0 lỗi; 6 kiểm thử MySQL bị skip do chưa có server/biến bật test.
- 7 kiểm thử Python offline thành công.
- Đã kiểm tra parse pom.xml, cú pháp Python, SQL statement splitting và seed dry-run 150 pet/60 gia đình/100 đơn.
- Không khẳng định SQL/migration/khóa đồng thời/login JDBC đã chạy thành công trên MySQL 8.4 thực. Cũng chưa xác minh trực quan các cửa sổ Swing trên máy có màn hình; cần smoke test trên Windows của bạn.
- Kiểm tra từng màn hình với dữ liệu mẫu để xác nhận kích thước font và bố cục theo DPI máy bạn. Dữ liệu được đọc đồng bộ trên một số tab; với dữ liệu lớn hoặc database phản hồi chậm, cần chuyển các tác vụ đó sang SwingWorker như màn đăng nhập.

Xem kết quả thực tế trong `verification/`. Lệnh kiểm thử trên máy bạn:

```powershell
mvn clean test
.\.venv\Scripts\python.exe -m unittest discover -s scripts -p "test_*.py" -v
```

Để chạy 6 test MySQL, tạo database riêng có hậu tố `_test`. Không dùng database đồ án chính:

```powershell
$env:DB_NAME="pet_matcher_test"
.\.venv\Scripts\python.exe scripts/init_db.py
$env:PETMATCH_MYSQL_TEST="true"
mvn test
Remove-Item Env:PETMATCH_MYSQL_TEST
Remove-Item Env:DB_NAME
```

Test tích hợp có thêm lớp kiểm tra hậu tố `_test`, tạo fixture và dọn dữ liệu fixture khi kết thúc. Chúng kiểm tra transaction placement, từ chối lặp, care profile, re-evaluation rollback, chặn gia đình cam kết, nạp criteria/kết quả và chặn dữ liệu pet cũ ghi đè PLACED. Chưa có kiểm thử race nhiều luồng hoặc toàn bộ luồng login bằng MySQL thật; hãy kiểm tra chúng trước khi tuyên bố hệ thống sẵn sàng triển khai.

## 9. Đối chiếu yêu cầu tối thiểu

| Yêu cầu | Thành phần |
|---|---|
| Đăng ký pet và nhu cầu chăm sóc | PetsPanel → PetService → JdbcPetRepository → pets/care_profiles |
| Gia đình, điều kiện nhà ở, kinh nghiệm, capacity | FamiliesPanel → FosterFamilyService → JdbcFosterFamilyRepository |
| Tạo/cập nhật đơn, ≥3 tiêu chí | ApplicationsPanel, ApplicationService, Housing/Experience/Household |
| Abstraction AND/OR | MatchCriterion, CriterionGroup; nhóm có thể lồng nhau |
| Chặn pet/family cam kết và quá capacity | MatchingService, PlacementService, JdbcPlacementRepository, SQL guards/index |
| Lưu/nạp database JDBC | MySQL 8.4 theo sự cho phép mới của thầy; hồ sơ, rule definitions, evaluation, placement |
| Đa hình có tác động thực tế | Dog/Cat.requiredExperience được ExperienceCriterion sử dụng |
| Các màn chính | Pets, Foster Families, Applications, Matching Result, Placement Dashboard |

Chưa bổ sung lịch hẹn, kết thúc/hoàn trả placement, lịch sử foster nhiều đợt hoặc báo cáo ẩn danh; đây là các mở rộng, không phải chức năng đang có.
