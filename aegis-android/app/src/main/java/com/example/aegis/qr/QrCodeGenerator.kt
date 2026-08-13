package com.example.aegis.qr

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * Real ISO 18004 QR Code Generator using ZXing.
 * Produces scannable 2D barcodes from any string payload.
 */
object QrCodeGenerator {

  /**
   * Generates a real, scannable QR code bitmap from the given payload.
   *
   * @param payload The string data to encode into the QR code
   * @param width   Output bitmap width in pixels
   * @param height  Output bitmap height in pixels
   * @return An [ImageBitmap] containing the rendered QR code
   */
  fun generateQrCodeBitmap(payload: String, width: Int = 512, height: Int = 512): ImageBitmap {
    val hints = mapOf(
      EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
      EncodeHintType.MARGIN to 1,
      EncodeHintType.CHARACTER_SET to "UTF-8",
    )

    val bitMatrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, width, height, hints)

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val darkColor = android.graphics.Color.BLACK
    val lightColor = android.graphics.Color.WHITE

    for (y in 0 until height) {
      for (x in 0 until width) {
        bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) darkColor else lightColor)
      }
    }

    return bitmap.asImageBitmap()
  }
}
