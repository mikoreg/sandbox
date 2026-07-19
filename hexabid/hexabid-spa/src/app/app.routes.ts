import { Routes } from '@angular/router';
import { AppShellComponent } from './core/layout/app-shell.component';

export const routes: Routes = [
  {
    path: '',
    component: AppShellComponent,
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/home/home-page.component').then((m) => m.HomePageComponent)
      },
      {
        path: 'auction/:auctionId',
        loadComponent: () =>
          import('./features/details/auction-details-page.component').then(
            (m) => m.AuctionDetailsPageComponent
          )
      },
      {
        path: 'sell',
        loadComponent: () =>
          import('./features/create/auction-create-page.component').then(
            (m) => m.AuctionCreatePageComponent
          )
      },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/user-dashboard-page.component').then(
            (m) => m.UserDashboardPageComponent
          )
      }
    ]
  },
  {
    path: '**',
    redirectTo: ''
  }
];
