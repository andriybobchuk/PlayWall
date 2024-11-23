import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.material3.MaterialTheme
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studios1299.playwall.core.presentation.designsystem.ZEDGE_BLACK
import com.studios1299.playwall.core.presentation.designsystem.ZEDGE_PURPLE
import com.studios1299.playwall.core.presentation.designsystem.ZEDGE_WHITE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InviteViewModel(
    linkToDisplay: String,
) : ViewModel() {

    var currentTab by mutableStateOf(0)
    private var _qrCodeBitmap = mutableStateOf<Bitmap?>(null)
    val qrCodeBitmap: State<Bitmap?> = _qrCodeBitmap

    init {
        generateQRCode(linkToDisplay)
    }

    private fun generateQRCode(link: String, foregroundColor: Int = ZEDGE_BLACK.toArgb(), backgroundColor: Int = ZEDGE_WHITE.toArgb()) = viewModelScope.launch(
        Dispatchers.IO) {
        val writer = QRCodeWriter()
        try {
            val bitMatrix = writer.encode(link, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) foregroundColor else backgroundColor)
                }
            }
            _qrCodeBitmap.value = bmp
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
