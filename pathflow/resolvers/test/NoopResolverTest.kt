package top.fifthlight.pathflow.resolvers.test

import org.junit.jupiter.api.assertNull
import top.fifthlight.pathflow.resolvers.NoopResolver
import kotlin.test.Test

class NoopResolverTest {
    @Test
    fun testEmpty() {
        assertNull(
            NoopResolver.resolve(
                inputFeatures = emptySet(),
                targetFeatures = emptySet(),
                transformers = emptySet(),
                maxSteps = 0,
            )
        )
    }
}
