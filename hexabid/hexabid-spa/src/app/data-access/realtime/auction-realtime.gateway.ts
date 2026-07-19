import { Injectable, inject } from '@angular/core';
import { Client, StompSubscription } from '@stomp/stompjs';
import { Observable, Subject } from 'rxjs';
import { AppEndpoints } from '../../core/config/app-endpoints';
import { AuctionRealtimeMessage } from '../contracts/auction-api.models';

export interface AuctionRealtimeSession {
  messages$: Observable<AuctionRealtimeMessage>;
  placeBid(command: { amount: string; currency: string }): void;
  disconnect(): Promise<void>;
}

@Injectable({ providedIn: 'root' })
export class AuctionRealtimeGateway {
  private readonly endpoints = inject(AppEndpoints);

  openAuctionChannel(auctionId: string): AuctionRealtimeSession {
    const channel = new Subject<AuctionRealtimeMessage>();
    const client = new Client({
      brokerURL: this.endpoints.wsBaseUrl,
      reconnectDelay: 5000,
      debug: () => undefined
    });

    const subscriptions: StompSubscription[] = [];

    client.onConnect = () => {
      subscriptions.push(
        client.subscribe(`/topic/auctions/${auctionId}/bids`, (message) => {
          const payload = JSON.parse(message.body) as {
            bidderId: string;
            amount: string;
            currency: string;
            placedAt: string;
          };

          channel.next({ kind: 'bid-accepted', ...payload });
        })
      );

      subscriptions.push(
        client.subscribe(`/topic/auctions/${auctionId}/errors`, (message) => {
          const payload = JSON.parse(message.body) as { reason: string; message: string };
          channel.next({ kind: 'bid-rejected', ...payload });
        })
      );

      subscriptions.push(
        client.subscribe(`/topic/auctions/${auctionId}/events`, (message) => {
          const payload = JSON.parse(message.body) as {
            type: string;
            payload: Record<string, unknown>;
            occurredAt: string;
          };

          channel.next({
            kind: 'auction-event',
            eventType: payload.type,
            occurredAt: payload.occurredAt,
            payload: payload.payload
          });
        })
      );
    };

    client.onStompError = (frame) => {
      channel.next({
        kind: 'connection-error',
        message: frame.headers['message'] ?? 'Błąd warstwy STOMP.'
      });
    };

    client.onWebSocketError = () => {
      channel.next({
        kind: 'connection-error',
        message: 'Połączenie WebSocket zostało przerwane.'
      });
    };

    client.activate();

    return {
      messages$: channel.asObservable(),
      placeBid(command) {
        client.publish({
          destination: `/app/auctions/${auctionId}/bids`,
          body: JSON.stringify(command)
        });
      },
      async disconnect() {
        subscriptions.forEach((subscription) => subscription.unsubscribe());
        await client.deactivate();
        channel.complete();
      }
    };
  }
}
