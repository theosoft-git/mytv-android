package top.yogiczy.mytv.tv.ui.screen.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultFilterQuality
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImagePainter
import coil.compose.AsyncImagePainter.Companion.DefaultTransform
import coil.compose.DefaultModelEqualityDelegate
import coil.compose.EqualityDelegate
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageScope
import java.io.File

@Composable
fun AsyncImageEnhance(
    model: String,
    modifier: Modifier = Modifier,
    transform: (AsyncImagePainter.State) -> AsyncImagePainter.State = DefaultTransform,
    loading: @Composable() (SubcomposeAsyncImageScope.(AsyncImagePainter.State.Loading) -> Unit)? = null,
    success: @Composable() (SubcomposeAsyncImageScope.(AsyncImagePainter.State.Success) -> Unit)? = null,
    error: @Composable() (SubcomposeAsyncImageScope.(AsyncImagePainter.State.Error) -> Unit)? = null,
    onLoading: ((AsyncImagePainter.State.Loading) -> Unit)? = null,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
    onError: ((AsyncImagePainter.State.Error) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DefaultFilterQuality,
    clipToBounds: Boolean = true,
    modelEqualityDelegate: EqualityDelegate = DefaultModelEqualityDelegate
) {
    if (model.startsWith("http")) {
        SubcomposeAsyncImage(
            model = model,
            modifier = modifier,
            contentScale = contentScale,
            contentDescription = null,
            transform = transform,
            loading = loading,
            success = success,
            error = error,
            onLoading = onLoading,
            onSuccess = onSuccess,
            onError = onError,
            alignment = alignment,
            alpha = alpha,
            colorFilter = colorFilter,
            filterQuality = filterQuality,
            clipToBounds = clipToBounds,
            modelEqualityDelegate = modelEqualityDelegate,
        )
    } else if (model.startsWith("file://")) {
        val filePath = model.substringAfter("file://")
        SubcomposeAsyncImage(
            model = File(filePath),
            modifier = modifier,
            contentScale = contentScale,
            contentDescription = null,
            transform = transform,
            loading = loading,
            success = success,
            error = error,
            onLoading = onLoading,
            onSuccess = onSuccess,
            onError = onError,
            alignment = alignment,
            alpha = alpha,
            colorFilter = colorFilter,
            filterQuality = filterQuality,
            clipToBounds = clipToBounds,
            modelEqualityDelegate = modelEqualityDelegate,
        )
    } else {
        runCatching {
            val imageBytes = Base64.decode(model, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            val imageBitmap = bitmap.asImageBitmap()

            Image(
                bitmap = imageBitmap,
                contentScale = contentScale,
                modifier = modifier,
                contentDescription = null,
                alignment = alignment,
                alpha = alpha,
                colorFilter = colorFilter,
                filterQuality = filterQuality,
            )
        }
    }
}