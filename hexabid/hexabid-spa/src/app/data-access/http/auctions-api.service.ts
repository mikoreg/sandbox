import { Injectable } from '@angular/core';
import {
  ApiProblemDetail,
  AuctionSort,
  AuctionStatus
} from '../contracts/auction-api.models';
import {
  AuctionListResponse,
  AuctionResponse,
  AuctionsApi,
  Configuration,
  CreateAuctionRequest,
  ResponseError
} from '../generated/auction-contract';

@Injectable({ providedIn: 'root' })
export class AuctionsApiService {
  private readonly client = new AuctionsApi(
    new Configuration({
      basePath: '',
      credentials: 'include'
    })
  );

  async browseAuctions(options: {
    query?: string;
    status?: AuctionStatus | '';
    sort?: AuctionSort;
    limit?: number;
    after?: string | null;
  }): Promise<AuctionListResponse> {
    return this.execute(
      () =>
        this.client.browseAuctions({
          query: options.query,
          status: options.status || undefined,
          sort: options.sort,
          limit: options.limit,
          after: options.after || undefined
        }),
      'Nie udało się pobrać listy aukcji.'
    );
  }

  async getAuctionById(auctionId: string): Promise<AuctionResponse> {
    return this.execute(
      () => this.client.getAuctionById({ auctionId }),
      'Nie udało się pobrać szczegółów aukcji.'
    );
  }

  async createAuction(request: CreateAuctionRequest): Promise<AuctionResponse> {
    return this.execute(
      () => this.client.createAuction({ createAuctionRequest: request }),
      'Nie udało się utworzyć aukcji.'
    );
  }

  async browseMyAuctions(options: {
    status?: AuctionStatus | '';
    sort?: AuctionSort;
    limit?: number;
    after?: string | null;
  }): Promise<AuctionListResponse> {
    return this.execute(
      () =>
        this.client.browseMyAuctions({
          status: options.status || undefined,
          sort: options.sort,
          limit: options.limit,
          after: options.after || undefined
        }),
      'Nie udało się pobrać listy aukcji użytkownika.'
    );
  }

  async browseMyBids(options: {
    status?: AuctionStatus | '';
    sort?: AuctionSort;
    limit?: number;
    after?: string | null;
  }): Promise<AuctionListResponse> {
    return this.execute(
      () =>
        this.client.browseMyBids({
          status: options.status || undefined,
          sort: options.sort,
          limit: options.limit,
          after: options.after || undefined
        }),
      'Nie udało się pobrać licytacji użytkownika.'
    );
  }

  toMessage(error: unknown, fallback: string): string {
    return error instanceof Error ? error.message : fallback;
  }

  private async execute<T>(operation: () => Promise<T>, fallback: string): Promise<T> {
    try {
      return await operation();
    } catch (error) {
      throw await this.normalizeError(error, fallback);
    }
  }

  private async normalizeError(error: unknown, fallback: string): Promise<Error> {
    if (error instanceof ResponseError) {
      if (error.response.status === 401) {
        return new Error('Ta operacja wymaga zalogowania w backendzie OAuth2.');
      }

      try {
        const problem = (await error.response.clone().json()) as ApiProblemDetail;
        return new Error(problem.detail ?? fallback);
      } catch {
        return new Error(fallback);
      }
    }

    return error instanceof Error ? error : new Error(fallback);
  }
}
