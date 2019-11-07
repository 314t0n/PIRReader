package space.sentinel.sensor

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.pi4j.io.gpio.GpioPinDigitalInput
import com.pi4j.io.gpio.PinState
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent
import com.pi4j.io.gpio.event.GpioPinListenerDigital
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.invocation.InvocationOnMock
import reactor.test.StepVerifier

internal class PIRReaderTest {

    private val positiveEvent = mock<GpioPinDigitalStateChangeEvent> {
        on { state }.doReturn(PinState.HIGH)
    }

    private val negativeEvent = mock<GpioPinDigitalStateChangeEvent> {
        on { state }.doReturn(PinState.LOW)
    }

    private val gpioInput = mock<GpioPinDigitalInput> {
        on { addListener(any<GpioPinListenerDigital>()) }
                .then {
                    sendEvent(it, positiveEvent)
                    sendEvent(it, negativeEvent)
                    sendEvent(it, positiveEvent)
                }
    }

    private fun sendEvent(it: InvocationOnMock, positiveEvent: GpioPinDigitalStateChangeEvent) {
        val listener: GpioPinListenerDigital = it.getArgument(0) as GpioPinListenerDigital
        listener.handleGpioPinDigitalStateChangeEvent(positiveEvent)
    }

    private val pirReader = PIRReader(gpioInput)

    @BeforeEach
    fun setup() {
        pirReader.setup()
    }

    @AfterEach
    fun tearDown() {
        pirReader.close()
    }

    @Test
    fun `reads state`() {
        StepVerifier.create( pirReader.read())
                .expectNext(PinState.HIGH)
                .expectNext(PinState.LOW)
                .expectNext(PinState.HIGH)
                .thenCancel()

        verify(gpioInput).addListener(any<GpioPinListenerDigital>())
    }
}