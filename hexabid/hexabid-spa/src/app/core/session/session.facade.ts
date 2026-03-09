import { Injectable, computed, inject, signal } from '@angular/core';
import { AppEndpoints } from '../config/app-endpoints';
import { toProfileVm } from '../../data-access/mappers/auction-view.mapper';
import { SessionApiService } from '../../data-access/http/session-api.service';
import { ProfileVm } from '../../data-access/contracts/auction-api.models';

@Injectable({ providedIn: 'root' })
export class SessionFacade {
  private readonly api = inject(SessionApiService);
  private readonly endpoints = inject(AppEndpoints);

  readonly loginProviders = this.endpoints.loginProviders;
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly profile = signal<ProfileVm | null>(null);
  readonly isAuthenticated = computed(() => this.profile() !== null);

  constructor() {
    void this.refresh();
  }

  async refresh(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);

    try {
      const profile = await this.api.getCurrentUserProfile();
      this.profile.set(profile ? toProfileVm(profile) : null);
    } catch (error) {
      this.error.set(this.api.toMessage(error, 'Nie udało się odczytać sesji użytkownika.'));
      this.profile.set(null);
    } finally {
      this.loading.set(false);
    }
  }

  async logout(): Promise<void> {
    await fetch('/logout', {
      method: 'POST',
      credentials: 'include'
    });

    this.profile.set(null);
    window.location.assign('/');
  }
}
