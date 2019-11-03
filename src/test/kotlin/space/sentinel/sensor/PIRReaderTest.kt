package space.sentinel.sensor

import com.nhaarman.mockitokotlin2.*
import com.pi4j.io.gpio.*
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent
import com.pi4j.io.gpio.event.GpioPinListenerDigital
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.mockito.invocation.InvocationOnMock

internal class PIRReaderTest {

    private val positiveEvent = mock<GpioPinDigitalStateChangeEvent> {
        on { state }.doReturn(PinState.HIGH)
    }

    private val negativeEvent = mock<GpioPinDigitalStateChangeEvent> {
        on { state }.doReturn(PinState.HIGH)
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
    fun `read ignores LOW state`() {
        val result = pirReader.read().take(2).collectList().block()

        assert(result[0] == true)
        assert(result[1] == true)
        verify(gpioInput).addListener(any<GpioPinListenerDigital>())
    }
}