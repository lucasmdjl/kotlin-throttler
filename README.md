# Throttler

## Overview

The Throttler library provides a mechanism to limit the rate of access to a resource or operation. This can be useful in various scenarios, such as rate-limiting API requests, controlling access to shared resources, and preventing overuse of services.

## Installation

### Gradle
```kotlin
dependencies {
    implementation("io.github.lucasmdjl:throttler:0.1.0")
}
```

### Maven
```xml
<dependency>
    <groupId>io.github.lucasmdjl</groupId>
    <artifactId>throttler</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Example Usage

```kotlin
interface Service {
    fun call()
}

class ThrottledService(
    private val service: Service,
    private val throttler: Throttler = FixedRateThrottler(5, 1000)
) : Service {

    override fun call() {
        if (throttler.access()) {
            service.call()
        } else {
            println("Access denied due to throttling at ${System.currentTimeMillis()}")
        }
    }
}

// Example Usage
class RealService : Service {
    override fun call() {
        println("Service called at ${System.currentTimeMillis()}")
    }
}

fun main() {
    val realService = RealService()
    val throttledService = ThrottledService(realService)

    repeat(10) {
        throttledService.call()
        Thread.sleep(150) // Simulate some delay between calls
    }
}

```
