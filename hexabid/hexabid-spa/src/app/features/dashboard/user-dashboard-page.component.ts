import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SessionFacade } from '../../core/session/session.facade';
import {
  AuctionBrowsePageVm,
  AuctionSort
} from '../../data-access/contracts/auction-api.models';
import { AuctionsApiService } from '../../data-access/http/auctions-api.service';
import { toAuctionBrowsePageVm } from '../../data-access/mappers/auction-view.mapper';
import { AuctionCardComponent } from '../../shared/ui/auction-card.component';
import { EmptyStateComponent } from '../../shared/ui/empty-state.component';

@Component({
  selector: 'app-user-dashboard-page',
  imports: [RouterLink, AuctionCardComponent, EmptyStateComponent],
  templateUrl: './user-dashboard-page.component.html',
  styleUrl: './user-dashboard-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UserDashboardPageComponent {
  readonly session = inject(SessionFacade);
  private readonly api = inject(AuctionsApiService);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly myAuctions = signal<AuctionBrowsePageVm>({ items: [], nextCursor: null });
  readonly myBids = signal<AuctionBrowsePageVm>({ items: [], nextCursor: null });

  constructor() {
    void this.load();
  }

  async load(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);

    try {
      const [myAuctions, myBids] = await Promise.all([
        this.api.browseMyAuctions({ sort: AuctionSort.ENDING_SOON, limit: 6 }),
        this.api.browseMyBids({ sort: AuctionSort.ENDING_SOON, limit: 6 })
      ]);

      this.myAuctions.set(toAuctionBrowsePageVm(myAuctions));
      this.myBids.set(toAuctionBrowsePageVm(myBids));
    } catch (error) {
      this.error.set(this.api.toMessage(error, 'Nie udało się załadować dashboardu użytkownika.'));
    } finally {
      this.loading.set(false);
    }
  }
}
