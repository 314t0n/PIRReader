package space.sentinel

import com.pi4j.io.gpio.*
import com.pi4j.util.ConsoleColor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.scheduler.Schedulers
import space.sentinel.sensor.PIRReader

fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("Sentinel")
    val pinAddress = args.elementAtOrElse(0){ "0" }.toInt()

    logger.info("Setup GPIO &pinAddress")

    val gpioSensor: GpioController = GpioFactory.getInstance()
    val sensor: GpioPinDigitalInput = gpioSensor.provisionDigitalInputPin(RaspiPin.getPinByAddress(pinAddress))
    val reader = PIRReader(sensor)

    try {
        reader.setup()
        logger.info("Listening ...")
        reader.read().publishOn(Schedulers.elastic())
                .doOnNext {
                    logger.info("GPIO PIN STATE CHANGE = " +
                            ConsoleColor.conditional(
                                    it.isHigh, // conditional expression
                                    ConsoleColor.GREEN, // positive conditional color
                                    ConsoleColor.RED, // negative conditional color
                                    it))
                }
                .subscribe {
                    doSomeBuisnessLogic(it, logger)
                }

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                println("Interrupted, stopping...\n")
                reader.close()
                gpioSensor.shutdown()
                logger.info("Stop")
            }
        })

        while (true)
            Thread.sleep(100)

    } catch (ex: Exception) {
        logger.error(ex.message)
    }
}

private fun doSomeBuisnessLogic(it: PinState, logger: Logger) {
    if (it.isHigh) logger.info("Movement is in progress")
    else logger.info("Movement stopped")
}