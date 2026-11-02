package com.petmatch;

import org.junit.jupiter.api.Test;
import com.petmatch.service.*;
import com.petmatch.repository.*;
import com.petmatch.model.*;
import java.util.*;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class PlacementServiceTest {
    static class Apps implements ApplicationRepository {
        ApplicationStatus status = ApplicationStatus.PENDING;

        public List<AdoptionApplication> findAll() {
            return List.of();
        }

        public Optional<AdoptionApplication> findById(long id) {
            return id == 1 ? Optional.of(new AdoptionApplication(1, 1, 1, LocalDateTime.now(), status, ""))
                    : Optional.empty();
        }

        public AdoptionApplication save(long p, long f, String n) {
            throw new UnsupportedOperationException();
        }

        public void updateStatus(long id, ApplicationStatus s) {
        }
    }

    @Test
    void missingApplicationRejectsBeforeAnyWrite() {
        var s = new PlacementService((a, p, f) -> fail("Must not write"), new Apps(), null, null,
                new MatchingService());
        assertThrows(RuntimeException.class, () -> s.confirm(999));
    }
}
