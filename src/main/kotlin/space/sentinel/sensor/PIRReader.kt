package space.sentinel.sensor

import com.pi4j.io.gpio.GpioPinDigitalInput
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent
import com.pi4j.io.gpio.event.GpioPinListenerDigital
import org.slf4j.LoggerFactory.getLogger
import reactor.core.publisher.EmitterProcessor
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxProcessor

class PIRReader(val input: GpioPinDigitalInput) : AutoCloseable {

    private val logger = getLogger(this::class.java)

    private val processor: FluxProcessor<GpioPinDigitalStateChangeEvent, GpioPinDigitalStateChangeEvent> = EmitterProcessor.create<GpioPinDigitalStateChangeEvent>(10).serialize()

    private val sink = processor.sink()

    fun setup(){
        input.addListener(GpioPinListenerDigital {
            logger.debug("Input event from pin: ${input.pin}")
            sink.next(it)
        })
    }

    /**
     * Emits true if movement detected
     */
    fun read(): Flux<Boolean> {
        return processor.map {
            logger.debug("Processing event: ${it.state.value}")
            it.state.isHigh }
    }

    override fun close() {
        logger.debug("close")
        sink.complete()
        input.removeAllListeners()
    }
}