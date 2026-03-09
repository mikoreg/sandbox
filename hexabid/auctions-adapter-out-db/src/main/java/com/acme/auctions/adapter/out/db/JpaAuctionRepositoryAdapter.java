package com.acme.auctions.adapter.out.db;

import com.acme.auctions.core.auctioning.exception.AuctionConcurrencyConflictException;
import com.acme.auctions.core.auctioning.model.Auction;
import com.acme.auctions.core.auctioning.model.AuctionId;
import com.acme.auctions.core.auctioning.model.AuctionStatus;
import com.acme.auctions.core.auctioning.port.out.AuctionRepository;
import jakarta.persistence.OptimisticLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaAuctionRepositoryAdapter implements AuctionRepository {

    private final SpringDataAuctionJpaRepository repository;
    private final AuctionJpaMapper mapper;

    public JpaAuctionRepositoryAdapter(SpringDataAuctionJpaRepository repository, AuctionJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Auction save(Auction auction) {
        try {
            AuctionJpaEntity entity = mapper.toEntity(auction, new AuctionJpaEntity());
            return mapper.toDomain(repository.saveAndFlush(entity));
        } catch (ObjectOptimisticLockingFailureException | OptimisticLockException exception) {
            throw new AuctionConcurrencyConflictException(auction.id());
        }
    }

    @Override
    public Optional<Auction> findById(AuctionId auctionId) {
        return repository.findById(auctionId.value()).map(mapper::toDomain);
    }

    @Override
    public List<Auction> findExpiredOpenAuctions(Instant currentTime) {
        return repository.findByStatusAndEndsAtLessThanEqual(AuctionStatus.OPEN, currentTime).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
