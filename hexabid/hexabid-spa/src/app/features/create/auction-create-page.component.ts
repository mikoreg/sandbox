import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuctionsApiService } from '../../data-access/http/auctions-api.service';

@Component({
  selector: 'app-auction-create-page',
  imports: [ReactiveFormsModule],
  templateUrl: './auction-create-page.component.html',
  styleUrl: './auction-create-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AuctionCreatePageComponent {
  private readonly router = inject(Router);
  private readonly api = inject(AuctionsApiService);

  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);

  readonly form = new FormGroup({
    title: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(4)] }),
    amount: new FormControl('100.00', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^\d+(\.\d{1,2})?$/)]
    }),
    currency: new FormControl('PLN', { nonNullable: true, validators: [Validators.required] }),
    endsAt: new FormControl('', { nonNullable: true, validators: [Validators.required] })
  });

  async submit(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.error.set(null);

    try {
      const value = this.form.getRawValue();
      const created = await this.api.createAuction({
        title: value.title,
        startingPrice: {
          amount: value.amount,
          currency: value.currency
        },
        endsAt: new Date(value.endsAt).toISOString()
      });

      await this.router.navigate(['/auction', created.auctionId]);
    } catch (error) {
      this.error.set(this.api.toMessage(error, 'Nie udało się utworzyć aukcji.'));
    } finally {
      this.submitting.set(false);
    }
  }
}
