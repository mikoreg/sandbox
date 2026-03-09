import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AppEndpoints {
  readonly apiBaseUrl = '/api';
  readonly wsBaseUrl = `${window.location.protocol === 'https:' ? 'wss' : 'ws'}://${window.location.host}/ws-auctions`;
  readonly loginProviders = [
    { id: 'dev', label: 'Dev', href: '/dev-auth?redirect=http://localhost:4200/' },
    { id: 'google', label: 'Google', href: '/oauth2/authorization/google' },
    { id: 'github', label: 'GitHub', href: '/oauth2/authorization/github' }
  ] as const;
}
