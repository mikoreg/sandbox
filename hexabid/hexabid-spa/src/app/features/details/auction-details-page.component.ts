import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuctionDetailsFacade } from './auction-details.facade';
import { EmptyStateComponent } from '../../shared/ui/empty-state.component';

@Component({
  selector: 'app-auction-details-page',
  imports: [CommonModule, ReactiveFormsModule, EmptyStateComponent],
  templateUrl: './auction-details-page.component.html',
  styleUrl: './auction-details-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [AuctionDetailsFacade]
})
export class AuctionDetailsPageComponent {
  readonly facade = inject(AuctionDetailsFacade);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  readonly bidForm = new FormGroup({
    amount: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^\d+(\.\d{1,2})?$/)]
    }),
    currency: new FormControl('PLN', { nonNullable: true, validators: [Validators.required] })
  });

  constructor() {
    this.route.paramMap.pipe(takeUntilDestroyed()).subscribe((params) => {
      const auctionId = params.get('auctionId');
      if (auctionId) {
        void this.facade.loadAuction(auctionId);
      }
    });

    this.destroyRef.onDestroy(() => {
      void this.facade.destroy();
    });
  }

  submitBid(): void {
    if (this.bidForm.invalid) {
      this.bidForm.markAllAsTouched();
      return;
    }

    const { amount, currency } = this.bidForm.getRawValue();
    this.facade.submitBid(amount, currency);
    this.bidForm.controls.amount.reset('');
  }
}
