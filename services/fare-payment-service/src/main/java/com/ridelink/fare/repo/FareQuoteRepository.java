package com.ridelink.fare.repo;

import com.ridelink.fare.domain.FareQuote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FareQuoteRepository extends JpaRepository<FareQuote, UUID> {
}
