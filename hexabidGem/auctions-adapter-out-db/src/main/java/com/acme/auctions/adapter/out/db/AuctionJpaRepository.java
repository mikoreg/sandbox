package main.java.com.acme.auctions.adapter.out.db;

import com.acme.auctions.core.auctioning.model.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuctionJpaRepository extends JpaRepository<Auction, UUID> {
}