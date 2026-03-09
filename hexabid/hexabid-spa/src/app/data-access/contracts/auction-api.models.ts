import { AuctionSort, AuctionStatus } from '../generated/auction-contract';

export { AuctionSort, AuctionStatus } from '../generated/auction-contract';

export interface ApiProblemDetail {
  title?: string;
  status?: number;
  detail?: string;
}

export interface AuctionSummaryVm {
  auctionId: string;
  sellerId: string;
  title: string;
  currentPrice: string;
  currency: string;
  priceLabel: string;
  endsAt: Date;
  endsAtLabel: string;
  timeLeftLabel: string;
  status: AuctionStatus;
  statusLabel: string;
  statusTone: 'open' | 'closed';
  leadingBidderId?: string | null;
}

export interface BidVm {
  bidderId: string;
  bidderLabel: string;
  amount: string;
  currency: string;
  priceLabel: string;
  placedAt: Date;
  placedAtLabel: string;
}

export interface AuctionDetailsVm extends AuctionSummaryVm {
  bidHistory: BidVm[];
  leadingBidderLabel: string;
  totalBids: number;
  isOpen: boolean;
}

export interface ProfileVm {
  partyId: string;
  provider: string;
  providerLabel: string;
  displayName: string;
  email?: string | null;
  verified: boolean;
  verifiedLabel: string;
}

export interface SearchCriteriaVm {
  query: string;
  status: AuctionStatus | '';
  sort: AuctionSort;
  limit: number;
}

export interface AuctionBrowsePageVm {
  items: AuctionSummaryVm[];
  nextCursor: string | null;
}

export type AuctionRealtimeMessage =
  | {
      kind: 'bid-accepted';
      bidderId: string;
      amount: string;
      currency: string;
      placedAt: string;
    }
  | {
      kind: 'bid-rejected';
      reason: string;
      message: string;
    }
  | {
      kind: 'auction-event';
      eventType: string;
      occurredAt: string;
      payload: Record<string, unknown>;
    }
  | {
      kind: 'connection-error';
      message: string;
    };
