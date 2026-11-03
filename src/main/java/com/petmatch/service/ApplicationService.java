package com.petmatch.service;

import java.util.List;
import java.util.Optional;

import com.petmatch.exception.InvalidApplicationException;
import com.petmatch.model.AdoptionApplication;
import com.petmatch.model.ApplicationStatus;
import com.petmatch.model.FosterFamily;
import com.petmatch.model.Pet;
import com.petmatch.repository.ApplicationRepository;

public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    public AdoptionApplication createApplication(Pet pet, FosterFamily family) {
        if (pet == null) {
            throw new InvalidApplicationException("Lỗi: Bạn chưa chọn thú cưng!");
        }
        if (family == null) {
            throw new InvalidApplicationException("Lỗi: Bạn chưa chọn gia đình nhận nuôi!");
        }

        return applicationRepository.save(
            pet.getId(), 
            family.getId(), 
            ApplicationStatus.PENDING.name()
        );
    }

    public List<AdoptionApplication> getAllApplications() {
        return applicationRepository.findAll();
    }

    public void updateApplicationStatus(long id, ApplicationStatus newStatus) {
        Optional<AdoptionApplication> optionalApp = applicationRepository.findById(id);
        AdoptionApplication app = optionalApp.orElseThrow(() ->
            new InvalidApplicationException("Không tìm thấy tờ đơn có mã số: " + id)
        );

        app.setStatus(newStatus);
        applicationRepository.update(app);
    }
}