/* tslint:disable */
/* eslint-disable */
/**
 * 
 * @export
 * @interface AuctionListItemResponse
 */
export interface AuctionListItemResponse {
    /**
     * 
     * @type {string}
     * @memberof AuctionListItemResponse
     */
    auctionId: string;
    /**
     * 
     * @type {string}
     * @memberof AuctionListItemResponse
     */
    sellerId: string;
    /**
     * 
     * @type {string}
     * @memberof AuctionListItemResponse
     */
    title: string;
    /**
     * 
     * @type {string}
     * @memberof AuctionListItemResponse
     */
    currentPrice: string;
    /**
     * 
     * @type {string}
     * @memberof AuctionListItemResponse
     */
    currency: string;
    /**
     * 
     * @type {string}
     * @memberof AuctionListItemResponse
     */
    endsAt: string;
    /**
     * 
     * @type {AuctionStatus}
     * @memberof AuctionListItemResponse
     */
    status: AuctionStatus;
    /**
     * 
     * @type {string}
     * @memberof AuctionListItemResponse
     */
    leadingBidderId?: string;
}


/**
 * 
 * @export
 * @interface AuctionListResponse
 */
export interface AuctionListResponse {
    /**
     * 
     * @type {Array<AuctionListItemResponse>}
     * @memberof AuctionListResponse
     */
    items: Array<AuctionListItemResponse>;
    /**
     * 
     * @type {string}
     * @memberof AuctionListResponse
     */
    nextCursor?: string;
}
/**
 * 
 * @export
 * @interface AuctionResponse
 */
export interface AuctionResponse {
    /**
     * 
     * @type {string}
     * @memberof AuctionResponse
     */
    auctionId: string;
    /**
     * 
     * @type {string}
     * @memberof AuctionResponse
     */
    sellerId: string;
    /**
     * 
     * @type {string}
     * @memberof AuctionResponse
     */
    title: string;
    /**
     * 
     * @type {string}
     * @memberof AuctionResponse
     */
    currentPrice: string;
    /**
     * 
     * @type {string}
     * @memberof AuctionResponse
     */
    currency: string;
    /**
     * 
     * @type {string}
     * @memberof AuctionResponse
     */
    endsAt: string;
    /**
     * 
     * @type {AuctionStatus}
     * @memberof AuctionResponse
     */
    status: AuctionStatus;
    /**
     * 
     * @type {string}
     * @memberof AuctionResponse
     */
    leadingBidderId?: string;
    /**
     * 
     * @type {Array<BidResponse>}
     * @memberof AuctionResponse
     */
    bids: Array<BidResponse>;
}


/**
 * 
 * @export
 * @enum {string}
 */
export enum AuctionSort {
    ENDING_SOON = 'ENDING_SOON',
    ENDING_LATEST = 'ENDING_LATEST'
}

/**
 * 
 * @export
 * @enum {string}
 */
export enum AuctionStatus {
    OPEN = 'OPEN',
    CLOSED = 'CLOSED'
}

/**
 * 
 * @export
 * @interface BidResponse
 */
export interface BidResponse {
    /**
     * 
     * @type {string}
     * @memberof BidResponse
     */
    bidderId: string;
    /**
     * 
     * @type {string}
     * @memberof BidResponse
     */
    amount: string;
    /**
     * 
     * @type {string}
     * @memberof BidResponse
     */
    currency: string;
    /**
     * 
     * @type {string}
     * @memberof BidResponse
     */
    placedAt: string;
}
/**
 * 
 * @export
 * @interface CreateAuctionRequest
 */
export interface CreateAuctionRequest {
    /**
     * 
     * @type {string}
     * @memberof CreateAuctionRequest
     */
    title: string;
    /**
     * 
     * @type {Money}
     * @memberof CreateAuctionRequest
     */
    startingPrice: Money;
    /**
     * 
     * @type {string}
     * @memberof CreateAuctionRequest
     */
    endsAt: string;
}
/**
 * 
 * @export
 * @interface CurrentUserProfileResponse
 */
export interface CurrentUserProfileResponse {
    /**
     * 
     * @type {string}
     * @memberof CurrentUserProfileResponse
     */
    partyId: string;
    /**
     * 
     * @type {string}
     * @memberof CurrentUserProfileResponse
     */
    provider: string;
    /**
     * 
     * @type {string}
     * @memberof CurrentUserProfileResponse
     */
    displayName: string;
    /**
     * 
     * @type {string}
     * @memberof CurrentUserProfileResponse
     */
    email?: string;
    /**
     * 
     * @type {boolean}
     * @memberof CurrentUserProfileResponse
     */
    verified: boolean;
}
/**
 * 
 * @export
 * @interface Money
 */
export interface Money {
    /**
     * 
     * @type {string}
     * @memberof Money
     */
    amount: string;
    /**
     * 
     * @type {string}
     * @memberof Money
     */
    currency: string;
}
