package pe.proxy.proxybuilder2.net.login

interface ILoginComponent {

    //Creates Connection
    fun connect()

    //Closes Connection
    fun close()

    //Returns the IP Address of the Proxy
    fun remoteAddress()

}