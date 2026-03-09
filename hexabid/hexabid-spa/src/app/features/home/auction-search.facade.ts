import { Injectable, inject, signal } from '@angular/core';
import {
  AuctionSort,
  AuctionBrowsePageVm,
  SearchCriteriaVm
} from '../../data-access/contracts/auction-api.models';
import { AuctionsApiService } from '../../data-access/http/auctions-api.service';
import { toAuctionBrowsePageVm } from '../../data-access/mappers/auction-view.mapper';

@Injectable()
export class AuctionSearchFacade {
  private readonly api = inject(AuctionsApiService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly page = signal<AuctionBrowsePageVm>({ items: [], nextCursor: null });
  readonly criteria = signal<SearchCriteriaVm>({
    query: '',
    status: '',
    sort: AuctionSort.ENDING_SOON,
    limit: 12
  });

  async search(criteria: SearchCriteriaVm, append = false): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    this.criteria.set(criteria);

    try {
      const response = toAuctionBrowsePageVm(
        await this.api.browseAuctions({
          query: criteria.query,
          status: criteria.status,
          sort: criteria.sort,
          limit: criteria.limit
        })
      );

      this.page.set(response);
    } catch (error) {
      this.error.set(this.api.toMessage(error, 'Nie udało się pobrać listy aukcji.'));
      if (!append) {
        this.page.set({ items: [], nextCursor: null });
      }
    } finally {
      this.loading.set(false);
    }
  }

  async loadMore(): Promise<void> {
    const current = this.page();
    if (!current.nextCursor) {
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    try {
      const next = toAuctionBrowsePageVm(
        await this.api.browseAuctions({
          query: this.criteria().query,
          status: this.criteria().status,
          sort: this.criteria().sort,
          limit: this.criteria().limit,
          after: current.nextCursor
        })
      );

      this.page.set({
        items: [...current.items, ...next.items],
        nextCursor: next.nextCursor
      });
    } catch (error) {
      this.error.set(this.api.toMessage(error, 'Nie udało się pobrać kolejnej strony wyników.'));
    } finally {
      this.loading.set(false);
    }
  }
}
