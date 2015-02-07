package scaster.protocol

import java.net.InetAddress
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl._

object WeakSSLSocket {
  private val trustAllCerts = Array(new X509TrustManager {
    override def getAcceptedIssuers: Array[X509Certificate] = Array.empty[X509Certificate]
    override def checkClientTrusted(x509Certificates: Array[X509Certificate], s: String): Unit = Unit
    override def checkServerTrusted(x509Certificates: Array[X509Certificate], s: String): Unit = Unit
  }).asInstanceOf[Array[TrustManager]]

  def weakSSLFactory(): SSLSocketFactory = {
    val ssc = SSLContext.getInstance("TLS")
    ssc.init(null, trustAllCerts, new SecureRandom())
    ssc.getSocketFactory
  }

  def connect(address: InetAddress, port: Int): SSLSocket = {
    weakSSLFactory().createSocket(address, port).asInstanceOf[SSLSocket]
  }
}
