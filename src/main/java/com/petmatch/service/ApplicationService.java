package com.petmatch.service;

import com.petmatch.exception.InvalidApplicationException;
import com.petmatch.model.AdoptionApplication;
import com.petmatch.model.ApplicationStatus;
import com.petmatch.model.FosterFamily;
import com.petmatch.model.Pet;
import com.petmatch.repository.ApplicationRepository;
import java.util.List;
import java.util.Optional;

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

        if (pet.getId() <= 0
                || family.getId() <= 0
                || pet.getStatus() != com.petmatch.model.PetStatus.AVAILABLE
                || !family.hasCapacity()
                || family.getActivePetCount() != 0)
            throw new InvalidApplicationException("Pet/family unavailable, unsaved or committed.");
        if (applicationRepository.findAll().stream()
                .anyMatch(
                        a -> a.getPet().getId() == pet.getId()
                                && a.getFamily().getId() == family.getId()
                                && (a.getStatus() == ApplicationStatus.PENDING
                                        || a.getStatus() == ApplicationStatus.APPROVED)))
            throw new InvalidApplicationException("An open application already exists for this pair.");
        return applicationRepository.save(
                pet.getId(), family.getId(), ApplicationStatus.PENDING.name());
    }

    public List<AdoptionApplication> getAllApplications() {
        return applicationRepository.findAll();
    }

    public void updateApplicationStatus(long id, ApplicationStatus newStatus) {
        Optional<AdoptionApplication> optionalApp = applicationRepository.findById(id);
        AdoptionApplication app = optionalApp.orElseThrow(
                () -> new InvalidApplicationException("Không tìm thấy tờ đơn có mã số: " + id));

        if (newStatus == null
                || newStatus == ApplicationStatus.PLACED
                || newStatus == ApplicationStatus.PENDING
                || (app.getStatus() != ApplicationStatus.PENDING
                        && app.getStatus() != ApplicationStatus.APPROVED))
            throw new InvalidApplicationException(
                    "Invalid status transition; placement requires the handover transaction.");
        if (newStatus == ApplicationStatus.APPROVED
                && !new MatchingService()
                        .evaluateMatch(app.getPet(), app.getFamily(), com.petmatch.model.MatchOperator.AND)
                        .isSatisfied())
            throw new InvalidApplicationException("Strict matching must pass before approval.");
        app.setStatus(newStatus);
        applicationRepository.update(app);
    }
}
