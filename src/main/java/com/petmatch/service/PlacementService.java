package com.petmatch.service;

import com.petmatch.exception.PlacementRejectedException;
import com.petmatch.model.FosterFamily;
import com.petmatch.model.Pet;
import com.petmatch.model.PetStatus;
import com.petmatch.model.Placement;
import com.petmatch.repository.FosterFamilyRepository;
import com.petmatch.repository.PetRepository;
import com.petmatch.repository.PlacementRepository;

import java.time.LocalDateTime;

public class PlacementService {

    private final PlacementRepository placementRepository;
    private final PetRepository petRepository;
    private final FosterFamilyRepository fosterFamilyRepository;

    public PlacementService(PlacementRepository placementRepository, 
                            PetRepository petRepository, 
                            FosterFamilyRepository fosterFamilyRepository) {
        this.placementRepository = placementRepository;
        this.petRepository = petRepository;
        this.fosterFamilyRepository = fosterFamilyRepository;
    }

    public Placement createPlacement(Pet pet, FosterFamily family) {

        // 1. Kiểm tra: Thú cưng đã có người nhận chưa
        if (pet.getStatus() == PetStatus.UNAVAILABLE) {
            throw new PlacementRejectedException("Từ chối bàn giao: Bé thú cưng này đã có gia đình khác nhận rồi!");
        }

        // 2. Kiểm tra: Gia đình còn chỗ trống không
        if (family.getCapacity() <= 0) {
            throw new PlacementRejectedException("Từ chối bàn giao: Gia đình này đã hết chỗ trống để nhận nuôi thêm!");
        }

        // 3. Chuyển đổi ID sang kiểu long cho khớp với cơ sở dữ liệu
        long petId = Long.parseLong(pet.getId());
        long familyId = Long.valueOf(family.getId());

        // 4. Tạo Tờ Biên Lai (Truyền giá trị tuần tự bằng dấu phẩy, KHÔNG ghi tên nhãn)
        Placement bienLai = new Placement(
            0L,                    // Giá trị cho id
            petId,                 // Giá trị cho petId
            familyId,              // Giá trị cho familyId
            0L,                    // Giá trị cho applicationId
            LocalDateTime.now(),   // Giá trị cho thời gian bàn giao
            "Bàn giao thành công"  // Giá trị cho ghi chú
        );

        // 5. Cập nhật lại trạng thái Thú cưng và Sức chứa của Gia đình
        pet.setStatus(PetStatus.UNAVAILABLE);
        petRepository.update(pet);

        family.setCapacity(family.getCapacity() - 1);
        fosterFamilyRepository.update(family);

        // 6. Lưu biên lai vào Database
        placementRepository.save(bienLai);

        return bienLai;
    }
}