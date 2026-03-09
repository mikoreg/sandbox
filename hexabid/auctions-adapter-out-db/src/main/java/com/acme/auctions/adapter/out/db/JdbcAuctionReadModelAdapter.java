package com.acme.auctions.adapter.out.db;

import com.acme.auctions.core.auctioning.model.AuctionStatus;
import com.acme.auctions.core.auctioning.port.in.AuctionBrowsePage;
import com.acme.auctions.core.auctioning.port.in.AuctionSort;
import com.acme.auctions.core.auctioning.port.in.AuctionSummaryView;
import com.acme.auctions.core.auctioning.port.in.BrowseAuctionsQuery;
import com.acme.auctions.core.auctioning.port.out.AuctionReadModel;
import com.acme.auctions.core.party.model.PartyId;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
public class JdbcAuctionReadModelAdapter implements AuctionReadModel {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcAuctionReadModelAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate must not be null");
    }

    @Override
    public AuctionBrowsePage browseAuctions(BrowseAuctionsQuery query) {
        return queryPage(query, "", new MapSqlParameterSource());
    }

    @Override
    public AuctionBrowsePage browseSellerAuctions(PartyId sellerId, BrowseAuctionsQuery query) {
        return queryPage(
                query,
                " and a.seller_id = :partyId",
                new MapSqlParameterSource("partyId", sellerId.value())
        );
    }

    @Override
    public AuctionBrowsePage browseBidderAuctions(PartyId bidderId, BrowseAuctionsQuery query) {
        return queryPage(
                query,
                " and exists (select 1 from auction_bids b where b.auction_id = a.id and b.bidder_id = :partyId)",
                new MapSqlParameterSource("partyId", bidderId.value())
        );
    }

    private AuctionBrowsePage queryPage(
            BrowseAuctionsQuery query,
            String additionalWhereClause,
            MapSqlParameterSource parameters
    ) {
        Cursor cursor = Cursor.parse(query.after(), query.sort());
        parameters.addValue("limitPlusOne", query.limit() + 1);

        StringBuilder sql = new StringBuilder("""
                select a.id, a.seller_id, a.title, a.current_price, a.currency, a.ends_at, a.status, a.leading_bidder_id
                from auctions a
                where 1 = 1
                """);

        if (query.text() != null && !query.text().isBlank()) {
            sql.append(" and lower(a.title) like :query");
            parameters.addValue("query", "%" + query.text().strip().toLowerCase() + "%");
        }
        if (query.status() != null) {
            sql.append(" and a.status = :status");
            parameters.addValue("status", query.status().name());
        }
        sql.append(additionalWhereClause);
        sql.append(keysetClause(query.sort(), cursor, parameters));
        sql.append(orderByClause(query.sort()));
        sql.append(" limit :limitPlusOne");

        List<AuctionSummaryView> rows = jdbcTemplate.query(sql.toString(), parameters, this::mapRow);
        if (rows.size() <= query.limit()) {
            return new AuctionBrowsePage(rows, null);
        }

        List<AuctionSummaryView> pageItems = rows.subList(0, query.limit());
        AuctionSummaryView lastVisibleItem = pageItems.getLast();
        return new AuctionBrowsePage(pageItems, Cursor.from(lastVisibleItem, query.sort()).encode());
    }

    private String keysetClause(AuctionSort sort, @Nullable Cursor cursor, MapSqlParameterSource parameters) {
        if (cursor == null) {
            return "";
        }
        parameters.addValue("cursorEndsAt", cursor.endsAt().toString());
        parameters.addValue("cursorId", cursor.auctionId().toString());
        return switch (sort) {
            case ENDING_SOON ->
                    " and (a.ends_at > :cursorEndsAt or (a.ends_at = :cursorEndsAt and a.id > :cursorId))";
            case ENDING_LATEST ->
                    " and (a.ends_at < :cursorEndsAt or (a.ends_at = :cursorEndsAt and a.id < :cursorId))";
        };
    }

    private String orderByClause(AuctionSort sort) {
        return switch (sort) {
            case ENDING_SOON -> " order by a.ends_at asc, a.id asc";
            case ENDING_LATEST -> " order by a.ends_at desc, a.id desc";
        };
    }

    private AuctionSummaryView mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new AuctionSummaryView(
                rs.getObject("id", UUID.class),
                rs.getString("seller_id"),
                rs.getString("title"),
                rs.getBigDecimal("current_price").toPlainString(),
                rs.getString("currency"),
                rs.getTimestamp("ends_at").toInstant(),
                AuctionStatus.valueOf(rs.getString("status")),
                rs.getString("leading_bidder_id")
        );
    }

    private record Cursor(AuctionSort sort, Instant endsAt, UUID auctionId) {

        static @Nullable Cursor parse(@Nullable String encoded, AuctionSort expectedSort) {
            if (encoded == null || encoded.isBlank()) {
                return null;
            }
            String decoded = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
            String[] parts = decoded.split("\\|", 3);
            if (parts.length != 3) {
                throw new IllegalArgumentException("invalid cursor");
            }
            AuctionSort sort = AuctionSort.valueOf(parts[0]);
            if (sort != expectedSort) {
                throw new IllegalArgumentException("cursor sort does not match requested sort");
            }
            return new Cursor(sort, Instant.parse(parts[1]), UUID.fromString(parts[2]));
        }

        static Cursor from(AuctionSummaryView lastVisibleItem, AuctionSort sort) {
            return new Cursor(sort, lastVisibleItem.endsAt(), lastVisibleItem.auctionId());
        }

        String encode() {
            String raw = sort.name() + "|" + endsAt + "|" + auctionId;
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        }
    }
}
