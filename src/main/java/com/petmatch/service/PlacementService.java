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

        // 1. Kiểm tra trạng thái thú cưng
        if (pet.getStatus() == PetStatus.UNAVAILABLE) {
            throw new PlacementRejectedException("Từ chối bàn giao: Bé thú cưng này đã có gia đình khác nhận rồi!");
        }

        // 2. Kiểm tra sức chứa gia đình
        if (family.getCapacity() <= 0) {
            throw new PlacementRejectedException("Từ chối bàn giao: Gia đình này đã hết chỗ trống để nhận nuôi thêm!");
        }

        // 3. Chuyển đổi ID sang kiểu long
        long petId = Long.valueOf(pet.getId());
        long familyId = Long.valueOf(family.getId());
        long applicationId = 0L; // Nếu chưa có mã đơn cụ thể, tạm để 0L

        // 4. Cập nhật lại trạng thái Thú cưng thành UNAVAILABLE và lưu xuống Database
        pet.setStatus(PetStatus.UNAVAILABLE);
        petRepository.update(pet);

        // 5. Giảm sức chứa của Gia đình đi 1 và lưu xuống Database
        family.setCapacity(family.getCapacity() - 1);
        fosterFamilyRepository.update(family);

        // 6. Gọi đúng hàm createPlacement của Chú Thủ Kho PlacementRepository
        placementRepository.createPlacement(applicationId, petId, familyId);

        // 7. Tạo một tờ biên lai giả lập để trả về cho giao diện hiển thị
        Placement bienLai = new Placement(
            0L,                    
            applicationId,         
            petId,                 
            familyId,              
            LocalDateTime.now(),   
            "Bàn giao thành công"  
        );

        return bienLai;
    }
}