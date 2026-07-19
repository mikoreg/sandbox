import { Injectable } from '@angular/core';
import {
  ApiProblemDetail
} from '../contracts/auction-api.models';
import {
  AuctionsApi,
  Configuration,
  CurrentUserProfileResponse,
  ResponseError
} from '../generated/auction-contract';

@Injectable({ providedIn: 'root' })
export class SessionApiService {
  private readonly client = new AuctionsApi(
    new Configuration({
      basePath: '',
      credentials: 'include'
    })
  );

  async getCurrentUserProfile(): Promise<CurrentUserProfileResponse | null> {
    try {
      return await this.client.getCurrentUserProfile();
    } catch (error) {
      if (error instanceof ResponseError && error.response.status === 401) {
        return null;
      }

      throw await this.normalizeError(error, 'Nie udało się odczytać profilu użytkownika.');
    }
  }

  toMessage(error: unknown, fallback: string): string {
    return error instanceof Error ? error.message : fallback;
  }

  private async normalizeError(error: unknown, fallback: string): Promise<Error> {
    if (error instanceof ResponseError) {
      try {
        const problem = (await error.response.clone().json()) as ApiProblemDetail;
        return new Error(problem.detail ?? fallback);
      } catch {
        return new Error(fallback);
      }
    }

    return error instanceof Error ? error : new Error(fallback);
  }
}
