# Silver bars
Author: Kevin Cross
## to build
```bash
./gradlew build
```

### to publish 
publish locally:
```
gradlew build PublishToMavenLocal 
```

## adding the library to your project

```
dependencies {
    implementation "org.kware:silver-bars:1.0-SNAPSHOT"
}
```

## to create a marketplace for the sliver bars you should start with
```kotlin
import org.kware.silverbars.*
// create a market
val market = Market()
//register an order
val orderRef = market.register(Order(...))
// get the summary
market.summary()
// and to cancel an order
orderRef.cancel()
```

## Tests
located at [SilverBarsTest.kt](src/test/kotlin/SilverBarsTests.kt)
