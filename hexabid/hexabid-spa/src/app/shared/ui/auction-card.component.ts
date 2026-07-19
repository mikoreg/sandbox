import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuctionSummaryVm } from '../../data-access/contracts/auction-api.models';

@Component({
  selector: 'app-auction-card',
  imports: [RouterLink],
  templateUrl: './auction-card.component.html',
  styleUrl: './auction-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AuctionCardComponent {
  readonly auction = input.required<AuctionSummaryVm>();
}
