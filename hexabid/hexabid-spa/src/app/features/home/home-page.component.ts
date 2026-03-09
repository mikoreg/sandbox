import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SessionFacade } from '../../core/session/session.facade';
import { AuctionSort, AuctionStatus } from '../../data-access/contracts/auction-api.models';
import { AuctionCardComponent } from '../../shared/ui/auction-card.component';
import { EmptyStateComponent } from '../../shared/ui/empty-state.component';
import { AuctionSearchFacade } from './auction-search.facade';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-home-page',
  imports: [CommonModule, ReactiveFormsModule, RouterLink, AuctionCardComponent, EmptyStateComponent],
  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [AuctionSearchFacade]
})
export class HomePageComponent {
  readonly AuctionSort = AuctionSort;
  readonly AuctionStatus = AuctionStatus;
  readonly facade = inject(AuctionSearchFacade);
  readonly session = inject(SessionFacade);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly filtersForm = new FormGroup({
    query: new FormControl('', { nonNullable: true }),
    status: new FormControl<AuctionStatus | ''>('', { nonNullable: true }),
    sort: new FormControl<AuctionSort>(AuctionSort.ENDING_SOON, { nonNullable: true })
  });

  constructor() {
    this.route.queryParamMap.pipe(takeUntilDestroyed()).subscribe((params) => {
      const statusParam = params.get('status');
      const sortParam = params.get('sort');
      const status: AuctionStatus | '' =
        statusParam === AuctionStatus.OPEN || statusParam === AuctionStatus.CLOSED
          ? statusParam
          : '';
      const sort: AuctionSort =
        sortParam === AuctionSort.ENDING_LATEST
          ? AuctionSort.ENDING_LATEST
          : AuctionSort.ENDING_SOON;
      const nextFormValue = {
        query: params.get('query') ?? '',
        status,
        sort
      };

      this.filtersForm.setValue(nextFormValue, { emitEvent: false });
      void this.facade.search({
        ...nextFormValue,
        limit: 12
      });
    });
  }

  applyFilters(): void {
    const { query, status, sort } = this.filtersForm.getRawValue();

    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        query: query || null,
        status: status || null,
        sort
      }
    });
  }

  clearFilters(): void {
    this.filtersForm.setValue({ query: '', status: '', sort: AuctionSort.ENDING_SOON });
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {}
    });
  }
}
