package space.sentinel.sensor

import com.pi4j.io.gpio.GpioPinDigitalInput
import com.pi4j.io.gpio.PinState
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent
import com.pi4j.io.gpio.event.GpioPinListenerDigital
import org.slf4j.LoggerFactory.getLogger
import reactor.core.publisher.EmitterProcessor
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxProcessor

/**
 * Reads the state of PIR sensor from GPIO
 */
class PIRReader(private val input: GpioPinDigitalInput) : AutoCloseable {

    private val logger = getLogger(this::class.java)
    private val processor: FluxProcessor<GpioPinDigitalStateChangeEvent, GpioPinDigitalStateChangeEvent> =
            EmitterProcessor
                    .create<GpioPinDigitalStateChangeEvent>()
    private val sink = processor.sink()

    fun setup(){
        logger.debug("setup")
        input.addListener(GpioPinListenerDigital {
            sink.next(it)
        })
    }
    /**
     * Emits state of the sensor
     * @see com.pi4j.io.gpi.PinState
     */
    fun read(): Flux<PinState> {
        return processor.map {
            logger.debug("Processing event: ${it.state.value}")
            it.state }
    }

    override fun close() {
        logger.debug("close")
        sink.complete()
        input.removeAllListeners()
    }
}