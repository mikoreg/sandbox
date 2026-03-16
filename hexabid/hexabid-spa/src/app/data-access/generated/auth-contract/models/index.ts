/* tslint:disable */
/* eslint-disable */
/**
 * 
 * @export
 * @interface AuthProviderResponse
 */
export interface AuthProviderResponse {
    /**
     * Unique identifier of the provider (e.g., 'google', 'local')
     * @type {string}
     * @memberof AuthProviderResponse
     */
    registrationId: string;
    /**
     * Human-readable name of the provider
     * @type {string}
     * @memberof AuthProviderResponse
     */
    name: string;
    /**
     * Relative URL to initiate the login process
     * @type {string}
     * @memberof AuthProviderResponse
     */
    loginUrl: string;
}
