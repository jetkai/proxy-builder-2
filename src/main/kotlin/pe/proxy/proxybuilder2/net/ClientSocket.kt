package pe.proxy.proxybuilder2.net

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

/**
 * @author Kai
 */
class ClientSocket : Runnable {

    private var socket : Socket? = null
    private var inputStream : InputStream ?= null
    private var outputStream : OutputStream ?= null

    private var isWriter : Boolean = false
    private var hasIOError : Boolean = false
    private var closed : Boolean = false

    private var buffIndex : Int = 0
    private var writeIndex : Int = 0

    private var buffer : ByteArray = ByteArray(0)

    fun init(initSocket: Socket) : ClientSocket {
        closed = false
        isWriter = false
        hasIOError = false

        socket = initSocket

        socket!!.soTimeout = 5000
        socket!!.tcpNoDelay = true

        inputStream = socket!!.getInputStream()
        outputStream = socket!!.getOutputStream()

        return this
    }

    override fun run() {
        while (isWriter) {
            var data: Int
            var index: Int
            synchronized(this) {
                if (buffIndex == writeIndex) try {
                    (this as Object).wait()
                } catch (ignored: InterruptedException) { }
                if (!isWriter) return
                index = writeIndex
                data = if (buffIndex >= writeIndex) buffIndex - writeIndex else 5000 - writeIndex
            }
            if (data > 0) {
                try {
                    outputStream?.write(buffer, index, data)
                } catch (_ex: IOException) { hasIOError = true }
                writeIndex = (writeIndex + data) % 5000
                try {
                    if (buffIndex == writeIndex) outputStream?.flush()
                } catch (_ex: IOException) { hasIOError = true }
            }
        }
    }

    @Throws(IOException::class)
    fun queueBytes(size: Int, byteArray: ByteArray) {
        if (closed) return

        if (hasIOError) {
            hasIOError = false
            throw IOException("Error in writer thread")
        }

        if (buffer.isEmpty())
            buffer = ByteArray(5000)

        synchronized(this) {
            for (l in 0 until size) {
                buffer[buffIndex] = byteArray[l]
                buffIndex = (buffIndex + 1) % 5000
                if (buffIndex == (writeIndex + 4900) % 5000) throw IOException("buffer overflow")
            }
            if (!isWriter) {
                isWriter = true
                startRunnable(this, 3)
            }
            (this as Object).notify()
        }
    }

    private fun startRunnable(runnable: Runnable?, priority: Int) {
        val thread = Thread(runnable)
        thread.uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { _: Thread?, _: Throwable? -> close() }
        thread.start()
        thread.priority = priority
    }

    fun read(): Int? {
        return if (closed) 0 else inputStream?.read()
    }

    fun close() {
        closed = true
        try {
            inputStream?.close()
            outputStream?.close()
            socket?.close()
        } catch (_ex: IOException) { println("Error closing stream") }
        isWriter = false
        synchronized(this) { (this as Object).notify() }
        buffer = ByteArray(0)
    }

}