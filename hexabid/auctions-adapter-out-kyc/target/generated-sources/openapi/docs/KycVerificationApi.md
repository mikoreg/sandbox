# KycVerificationApi

All URIs are relative to *https://kyc.example.test*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**verifyActor**](KycVerificationApi.md#verifyActor) | **GET** /api/kyc/actors/{actorId} |  |
| [**verifyActorWithHttpInfo**](KycVerificationApi.md#verifyActorWithHttpInfo) | **GET** /api/kyc/actors/{actorId} |  |



## verifyActor

> KycVerificationResponse verifyActor(actorId)



### Example

```java
// Import classes:
import com.acme.auctions.kyc.client.invoker.ApiClient;
import com.acme.auctions.kyc.client.invoker.ApiException;
import com.acme.auctions.kyc.client.invoker.Configuration;
import com.acme.auctions.kyc.client.invoker.models.*;
import com.acme.auctions.kyc.client.api.KycVerificationApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://kyc.example.test");

        KycVerificationApi apiInstance = new KycVerificationApi(defaultClient);
        String actorId = "actorId_example"; // String | 
        try {
            KycVerificationResponse result = apiInstance.verifyActor(actorId);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling KycVerificationApi#verifyActor");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **actorId** | **String**|  | |

### Return type

[**KycVerificationResponse**](KycVerificationResponse.md)


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Verification result |  -  |

## verifyActorWithHttpInfo

> ApiResponse<KycVerificationResponse> verifyActor verifyActorWithHttpInfo(actorId)



### Example

```java
// Import classes:
import com.acme.auctions.kyc.client.invoker.ApiClient;
import com.acme.auctions.kyc.client.invoker.ApiException;
import com.acme.auctions.kyc.client.invoker.ApiResponse;
import com.acme.auctions.kyc.client.invoker.Configuration;
import com.acme.auctions.kyc.client.invoker.models.*;
import com.acme.auctions.kyc.client.api.KycVerificationApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("https://kyc.example.test");

        KycVerificationApi apiInstance = new KycVerificationApi(defaultClient);
        String actorId = "actorId_example"; // String | 
        try {
            ApiResponse<KycVerificationResponse> response = apiInstance.verifyActorWithHttpInfo(actorId);
            System.out.println("Status code: " + response.getStatusCode());
            System.out.println("Response headers: " + response.getHeaders());
            System.out.println("Response body: " + response.getData());
        } catch (ApiException e) {
            System.err.println("Exception when calling KycVerificationApi#verifyActor");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Response headers: " + e.getResponseHeaders());
            System.err.println("Reason: " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **actorId** | **String**|  | |

### Return type

ApiResponse<[**KycVerificationResponse**](KycVerificationResponse.md)>


### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Verification result |  -  |

