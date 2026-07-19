package com.acme.auctions.adapter.out.db;

import com.acme.auctions.core.auctioning.model.AuctionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

interface SpringDataAuctionJpaRepository extends JpaRepository<AuctionJpaEntity, UUID> {
    List<AuctionJpaEntity> findByStatusAndEndsAtLessThanEqual(AuctionStatus status, Instant endsAt);
}
