package com.petmatch.service;

import java.time.LocalDateTime;

import com.petmatch.exception.PlacementRejectedException;
import com.petmatch.model.FosterFamily;
import com.petmatch.model.Pet;
import com.petmatch.model.Placement;
import com.petmatch.repository.FosterFamilyRepository;
import com.petmatch.repository.PetRepository;
import com.petmatch.repository.PlacementRepository;

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
        // 1. Kiểm tra cơ bản phía Service trước khi gọi Database
        if (family.getCapacity() <= family.getActivePetCount()) {
            throw new PlacementRejectedException("Từ chối bàn giao: Gia đình này đã đạt tối đa sức chứa thú cưng!");
        }

        // 2. Chuyển đổi ID sang kiểu long
        long petId = Long.valueOf(pet.getId());
        long familyId = Long.valueOf(family.getId());
        long applicationId = 0L; // Hoặc truyền ID đơn đăng ký nếu có

        try {
            // 3. Gọi trực tiếp transaction hoàn chỉnh từ JdbcPlacementRepository của Member 3
            placementRepository.createPlacement(applicationId, petId, familyId);
        } catch (Exception e) {
            // Bắt lỗi từ database và ném ra ngoại lệ thân thiện cho người dùng
            throw new PlacementRejectedException("Không thể thực hiện bàn giao: " + e.getMessage());
        }

        // 4. Trả về đối tượng Placement để hiển thị lên giao diện UI
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