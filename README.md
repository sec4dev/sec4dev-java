# Sec4Dev Java SDK

Java client for the [Sec4Dev Security Checks API](https://api.sec4.dev): disposable email detection and IP classification.

## Documentation

Full API documentation: [https://docs.sec4.dev/](https://docs.sec4.dev/)

## Install

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>com.sec4dev</groupId>
    <artifactId>sec4dev-java</artifactId>
    <version>1.0.0</version>
</dependency>
```

Or build and install locally:

```bash
mvn install
```

## Usage

```java
import com.sec4dev.*;
import com.sec4dev.models.*;

Sec4DevClient client = Sec4DevClient.builder()
    .apiKey("sec4_your_api_key")
    .build();

// Email check
try {
    EmailCheckResult result = client.getEmail().check("user@tempmail.com");
    if (result.isDisposable()) {
        System.out.println("Blocked: " + result.getDomain() + " is disposable");
    }
} catch (ValidationException e) {
    System.out.println("Invalid email: " + e.getMessage());
}

// IP check
try {
    IPCheckResult result = client.getIp().check("203.0.113.42");
    System.out.println("IP Type: " + result.getClassification());
    System.out.println("Confidence: " + (int)(result.getConfidence() * 100) + "%");
    if (result.getSignals().isHosting()) {
        System.out.println("Provider: " + result.getNetwork().getProvider());
    }
} catch (RateLimitException e) {
    System.out.println("Rate limited. Retry in " + e.getRetryAfter() + "s");
}
```

## Options

Builder options:

- `apiKey(String)` — API key (required, must start with `sec4_`)
- `baseUrl(String)` — API base URL (default: `https://api.sec4.dev/api/v1`)
- `timeout(long, TimeUnit)` — Request timeout (default: 30s)
- `retries(int)` — Retry attempts (default: 3)
- `retryDelay(long)` — Base retry delay in ms (default: 1000)
- `onRateLimit(RateLimitCallback)` — Callback for rate limit updates
