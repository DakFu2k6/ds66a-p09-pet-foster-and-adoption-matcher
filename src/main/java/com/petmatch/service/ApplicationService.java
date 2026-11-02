package com.petmatch.service;

import java.time.LocalDate;
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
            throw new InvalidApplicationException("Lỗi rồi: Bạn chưa chọn con vật nào cả!");
        }

        if (family == null) {
            throw new InvalidApplicationException("Lỗi rồi: Bạn chưa chọn gia đình nhận nuôi!");
        }

        AdoptionApplication newApplication = new AdoptionApplication();
        newApplication.setPet(pet);            
        newApplication.setFamily(family);      
        
        newApplication.setStatus(ApplicationStatus.PENDING); 
        
        newApplication.setApplicationDate(LocalDate.now());

        return applicationRepository.save(
            pet.getId(),
            family.getId(),
            newApplication.getStatus().name()
        );
    }

    public List<AdoptionApplication> getAllApplications() {
        return applicationRepository.findAll();
    }

    public void updateApplicationStatus(Integer id, ApplicationStatus newStatus) {
        
        Optional<AdoptionApplication> application = applicationRepository.findById(id);
        AdoptionApplication oldApplication = application.orElseThrow(() ->
            new InvalidApplicationException("Không tìm thấy tờ đơn nào có mã số này!"));
        
        oldApplication.setStatus(newStatus);
        
    }
}