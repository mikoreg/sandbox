package main.java.com.acme.auctions.adapter.out.db;

import com.acme.auctions.core.auctioning.model.Auction;
import com.acme.auctions.core.auctioning.port.out.AuctionRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AuctionRepositoryImpl implements AuctionRepository {
    private final AuctionJpaRepository jpaRepository;

    public AuctionRepositoryImpl(AuctionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Auction save(Auction auction) {
        return jpaRepository.save(auction);
    }

    @Override
    public Optional<Auction> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}