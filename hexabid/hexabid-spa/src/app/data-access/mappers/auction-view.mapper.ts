import {
  AuctionBrowsePageVm,
  AuctionDetailsVm,
  AuctionStatus,
  AuctionSummaryVm,
  BidVm,
  ProfileVm
} from '../contracts/auction-api.models';
import type {
  AuctionListItemResponse,
  AuctionListResponse,
  AuctionResponse,
  BidResponse,
  CurrentUserProfileResponse
} from '../generated/auction-contract';

function moneyLabel(amount: string, currency: string): string {
  return `${amount} ${currency}`;
}

function fullDateLabel(date: Date): string {
  return new Intl.DateTimeFormat('pl-PL', {
    dateStyle: 'medium',
    timeStyle: 'short'
  }).format(date);
}

function relativeLabel(date: Date): string {
  const delta = date.getTime() - Date.now();
  const minutes = Math.round(delta / 60000);

  if (Math.abs(minutes) < 60) {
    return minutes >= 0 ? `za ${minutes} min` : `${Math.abs(minutes)} min temu`;
  }

  const hours = Math.round(minutes / 60);
  if (Math.abs(hours) < 48) {
    return hours >= 0 ? `za ${hours} h` : `${Math.abs(hours)} h temu`;
  }

  const days = Math.round(hours / 24);
  return days >= 0 ? `za ${days} dni` : `${Math.abs(days)} dni temu`;
}

function statusLabel(status: AuctionStatus): string {
  return status === AuctionStatus.OPEN ? 'Aktywna' : 'Zamknięta';
}

function bidderLabel(bidderId?: string | null): string {
  return bidderId ? bidderId : 'Brak lidera';
}

function providerLabel(provider: string): string {
  return provider === 'google' ? 'Google' : provider === 'github' ? 'GitHub' : provider;
}

export function toAuctionSummaryVm(item: AuctionListItemResponse): AuctionSummaryVm {
  const endsAt = new Date(item.endsAt);

  return {
    auctionId: item.auctionId,
    sellerId: item.sellerId,
    title: item.title,
    currentPrice: item.currentPrice,
    currency: item.currency,
    priceLabel: moneyLabel(item.currentPrice, item.currency),
    endsAt,
    endsAtLabel: fullDateLabel(endsAt),
    timeLeftLabel: relativeLabel(endsAt),
    status: item.status,
    statusLabel: statusLabel(item.status),
    statusTone: item.status === AuctionStatus.OPEN ? 'open' : 'closed',
    leadingBidderId: item.leadingBidderId ?? null
  };
}

export function toAuctionBrowsePageVm(response: AuctionListResponse): AuctionBrowsePageVm {
  return {
    items: response.items.map(toAuctionSummaryVm),
    nextCursor: response.nextCursor ?? null
  };
}

export function toBidVm(bid: BidResponse): BidVm {
  const placedAt = new Date(bid.placedAt);

  return {
    bidderId: bid.bidderId,
    bidderLabel: bidderLabel(bid.bidderId),
    amount: bid.amount,
    currency: bid.currency,
    priceLabel: moneyLabel(bid.amount, bid.currency),
    placedAt,
    placedAtLabel: fullDateLabel(placedAt)
  };
}

export function toAuctionDetailsVm(response: AuctionResponse): AuctionDetailsVm {
  const summary = toAuctionSummaryVm(response);

  return {
    ...summary,
    bidHistory: response.bids.map(toBidVm).reverse(),
    leadingBidderLabel: bidderLabel(response.leadingBidderId),
    totalBids: response.bids.length,
    isOpen: response.status === AuctionStatus.OPEN
  };
}

export function toProfileVm(response: CurrentUserProfileResponse): ProfileVm {
  return {
    partyId: response.partyId,
    provider: response.provider,
    providerLabel: providerLabel(response.provider),
    displayName: response.displayName,
    email: response.email ?? null,
    verified: response.verified,
    verifiedLabel: response.verified ? 'KYC potwierdzone' : 'KYC niepotwierdzone'
  };
}
