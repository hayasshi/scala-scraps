//> using scala 3.3
//> using jvm 17
import javax.net.ssl.SSLSocketFactory
import java.util.concurrent.atomic.AtomicInteger
import java.net.Socket
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

val tls = args.map(_.toLowerCase()).exists(arg => arg == "ssl" || arg == "tls")

val (socket, charset) = 
  if (tls)
    (SSLSocketFactory.getDefault().createSocket("koukoku.shadan.open.ad.jp", 992), Charset.forName("UTF-8"))
  else
    (new Socket("koukoku.shadan.open.ad.jp", 23), Charset.forName("SJIS"))

val sockIn = socket.getInputStream()
val sockOut = socket.getOutputStream()

val totalBytes = new AtomicInteger()

val buf = new ByteArrayOutputStream()
val out = System.out

sockOut.write("notalk\n".getBytes())
sockOut.flush()

// Server から EOF が返らず待機してしまうため、CTRL+C で終了する
sys.addShutdownHook {
  out.println(buf.toString(charset))
  out.println("--- details ---")
  out.println(s"total bytes: ${totalBytes.get()}")
  out.flush()
  out.close()
  buf.close()
  socket.close()
}

var skipLf = false
Iterator
  .continually(sockIn.read())
  .takeWhile(_ != -1)
  .foreach { byte =>
    totalBytes.incrementAndGet()
    val char = byte.toChar
    if (char == '\r') {
      skipLf = true
      out.println(buf.toString(charset))
      out.flush()
      buf.reset()
    } else if (skipLf && char == '\n') {
      skipLf = false
    } else if (!skipLf && char == '\n') {
      skipLf = false
      out.println(buf.toString(charset))
      out.flush()
      buf.reset()
    } else {
      skipLf = false
      buf.write(byte)
    }
  }
