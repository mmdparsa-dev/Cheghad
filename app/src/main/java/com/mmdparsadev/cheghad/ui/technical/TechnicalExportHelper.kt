package com.mmdparsadev.cheghad.ui.technical

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.mmdparsadev.cheghad.R
import com.mmdparsadev.cheghad.data.models.CandleData
import com.mmdparsadev.cheghad.data.models.ChartTimeframe
import com.mmdparsadev.cheghad.data.models.CurrencyItem
import com.mmdparsadev.cheghad.data.models.EmaPeriod
import com.mmdparsadev.cheghad.getLocalizedTitle
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TechnicalExportHelper {

    fun exportAsPng(
        context: Context,
        bitmap: Bitmap,
        currency: CurrencyItem,
        timeframe: ChartTimeframe
    ) {
        sharePng(context, bitmap, currency, timeframe)
    }

    fun exportAsPdf(
        context: Context,
        chartBitmap: Bitmap,
        currency: CurrencyItem,
        timeframe: ChartTimeframe,
        candles: List<CandleData>,
        enabledEmas: Set<EmaPeriod> = setOf(EmaPeriod.EMA_200)
    ) {
        sharePdf(context, chartBitmap, currency, timeframe, candles, enabledEmas)
    }

    /**
     * Share PNG chart image via Android share sheet (Intent.ACTION_SEND).
     */
    fun sharePng(
        context: Context,
        bitmap: Bitmap,
        currency: CurrencyItem,
        timeframe: ChartTimeframe
    ) {
        try {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val file = File(exportDir, "Cheghad_${currency.symbol}_${timeframe.titleEn}_$timeStamp.png")

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val isEnglish = context.getString(R.string.tech_lang_code) == "en"
            val localizedTitle = getLocalizedTitle(currency.symbol, currency.title)
            val tfTitle = timeframe.localizedTitle(isEnglish)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.tech_share_png_subject, localizedTitle, currency.symbol))
                putExtra(Intent.EXTRA_TEXT, context.getString(R.string.tech_share_png_body, localizedTitle, currency.symbol, tfTitle))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, context.getString(R.string.tech_share_png_chooser))
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.tech_share_png_error, e.localizedMessage ?: ""), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Save PNG chart directly to public Pictures storage / Gallery.
     */
    fun savePngToDevice(
        context: Context,
        bitmap: Bitmap,
        currency: CurrencyItem,
        timeframe: ChartTimeframe
    ): Uri? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "Cheghad_${currency.symbol}_${timeframe.titleEn}_$timeStamp.png"

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Cheghad")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)

                    Toast.makeText(context, context.getString(R.string.tech_save_png_gallery_ok, fileName), Toast.LENGTH_LONG).show()
                    uri
                } else {
                    savePngToExternalLegacy(context, bitmap, fileName)
                }
            } else {
                savePngToExternalLegacy(context, bitmap, fileName)
            }
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.tech_save_png_error, e.localizedMessage ?: ""), Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun savePngToExternalLegacy(context: Context, bitmap: Bitmap, fileName: String): Uri? {
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            ?: context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val targetDir = File(picturesDir, "Cheghad").apply { mkdirs() }
        val file = File(targetDir, fileName)

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("image/png"), null)
        val uri = Uri.fromFile(file)
        Toast.makeText(context, context.getString(R.string.tech_save_png_dir_ok, file.name), Toast.LENGTH_LONG).show()
        return uri
    }

    /**
     * Share PDF analysis report via Android share sheet (Intent.ACTION_SEND).
     */
    fun sharePdf(
        context: Context,
        chartBitmap: Bitmap,
        currency: CurrencyItem,
        timeframe: ChartTimeframe,
        candles: List<CandleData>,
        enabledEmas: Set<EmaPeriod> = setOf(EmaPeriod.EMA_200)
    ) {
        try {
            val pdfDocument = createPdfDocument(context, chartBitmap, currency, timeframe, candles, enabledEmas)
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val file = File(exportDir, "Cheghad_Technical_${currency.symbol}_$timeStamp.pdf")

            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val localizedTitle = getLocalizedTitle(currency.symbol, currency.title)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.tech_share_pdf_subject, localizedTitle, currency.symbol))
                putExtra(Intent.EXTRA_TEXT, context.getString(R.string.tech_share_pdf_body, localizedTitle))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, context.getString(R.string.tech_share_pdf_chooser))
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.tech_share_pdf_error, e.localizedMessage ?: ""), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Save PDF analysis report directly to public Downloads storage.
     */
    fun savePdfToDevice(
        context: Context,
        chartBitmap: Bitmap,
        currency: CurrencyItem,
        timeframe: ChartTimeframe,
        candles: List<CandleData>,
        enabledEmas: Set<EmaPeriod> = setOf(EmaPeriod.EMA_200)
    ): Uri? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "Cheghad_Technical_${currency.symbol}_$timeStamp.pdf"

        return try {
            val pdfDocument = createPdfDocument(context, chartBitmap, currency, timeframe, candles, enabledEmas)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/Cheghad")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        pdfDocument.writeTo(out)
                    }
                    pdfDocument.close()
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)

                    Toast.makeText(context, context.getString(R.string.tech_save_pdf_downloads_ok, fileName), Toast.LENGTH_LONG).show()
                    uri
                } else {
                    pdfDocument.close()
                    savePdfToExternalLegacy(context, chartBitmap, currency, timeframe, candles, fileName, enabledEmas)
                }
            } else {
                pdfDocument.close()
                savePdfToExternalLegacy(context, chartBitmap, currency, timeframe, candles, fileName, enabledEmas)
            }
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.tech_save_pdf_error, e.localizedMessage ?: ""), Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun savePdfToExternalLegacy(
        context: Context,
        chartBitmap: Bitmap,
        currency: CurrencyItem,
        timeframe: ChartTimeframe,
        candles: List<CandleData>,
        fileName: String,
        enabledEmas: Set<EmaPeriod> = setOf(EmaPeriod.EMA_200)
    ): Uri? {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            ?: context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val targetDir = File(downloadsDir, "Cheghad").apply { mkdirs() }
        val file = File(targetDir, fileName)

        val pdfDoc = createPdfDocument(context, chartBitmap, currency, timeframe, candles, enabledEmas)
        FileOutputStream(file).use { out ->
            pdfDoc.writeTo(out)
        }
        pdfDoc.close()
        MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("application/pdf"), null)
        val uri = Uri.fromFile(file)
        Toast.makeText(context, context.getString(R.string.tech_save_pdf_dir_ok, file.name), Toast.LENGTH_LONG).show()
        return uri
    }

    /**
     * Common method to construct the PDF document.
     */
    private fun createPdfDocument(
        context: Context,
        chartBitmap: Bitmap,
        currency: CurrencyItem,
        timeframe: ChartTimeframe,
        candles: List<CandleData>,
        enabledEmas: Set<EmaPeriod> = setOf(EmaPeriod.EMA_200)
    ): PdfDocument {
        val pdfDocument = PdfDocument()
        val pageWidth = 842 // A4 Landscape
        val pageHeight = 595
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Background
        val bgPaint = Paint().apply { color = android.graphics.Color.WHITE }
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)

        // Header Banner
        val headerPaint = Paint().apply { color = android.graphics.Color.rgb(24, 32, 54) }
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 70f, headerPaint)

        val textPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 20f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText(context.getString(R.string.tech_pdf_header_title), 30f, 42f, textPaint)

        val subTextPaint = Paint().apply {
            color = android.graphics.Color.rgb(200, 210, 230)
            textSize = 12f
            isAntiAlias = true
        }
        val dateStr = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText(context.getString(R.string.tech_pdf_report_date, dateStr), pageWidth - 200f, 42f, subTextPaint)

        // Asset Info Strip
        val assetPaint = Paint().apply {
            color = android.graphics.Color.rgb(30, 41, 59)
            textSize = 18f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val localizedTitle = getLocalizedTitle(currency.symbol, currency.title)
        canvas.drawText("$localizedTitle (${currency.symbol})", 30f, 105f, assetPaint)

        val metaPaint = Paint().apply {
            color = android.graphics.Color.rgb(100, 116, 139)
            textSize = 12f
            isAntiAlias = true
        }
        val priceStr = String.format(Locale.US, "%,.2f", currency.currentPrice)
        val latestCandle = candles.lastOrNull()
        val activeEmas = if (enabledEmas.isEmpty()) listOf(EmaPeriod.EMA_200) else EmaPeriod.entries.filter { it in enabledEmas }
        val emaSummary = activeEmas.joinToString(" | ") { period ->
            val valStr = latestCandle?.let { period.extractFrom(it) }?.let { String.format(Locale.US, "%,.2f", it) } ?: "-"
            "${period.label}: $valStr"
        }
        val rsiStr = latestCandle?.rsi?.let { String.format(Locale.US, "%.1f", it) } ?: "-"
        val isEnglish = context.getString(R.string.tech_lang_code) == "en"

        canvas.drawText(
            context.getString(
                R.string.tech_pdf_meta_line,
                priceStr,
                timeframe.localizedTitle(isEnglish),
                emaSummary,
                rsiStr
            ),
            30f,
            125f,
            metaPaint
        )

        // Draw Chart Bitmap
        val chartTop = 140f
        val chartLeft = 30f
        val chartRight = pageWidth - 30f
        val chartBottom = 480f

        val chartDestRect = RectF(chartLeft, chartTop, chartRight, chartBottom)
        val chartBorderPaint = Paint().apply {
            color = android.graphics.Color.rgb(226, 232, 240)
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawRoundRect(chartDestRect, 12f, 12f, chartBorderPaint)

        val srcRect = Rect(0, 0, chartBitmap.width, chartBitmap.height)
        canvas.drawBitmap(chartBitmap, srcRect, chartDestRect, Paint(Paint.FILTER_BITMAP_FLAG))

        // Footer
        val footerPaint = Paint().apply {
            color = android.graphics.Color.rgb(148, 163, 184)
            textSize = 10f
            isAntiAlias = true
        }
        canvas.drawText(context.getString(R.string.tech_pdf_footer_source), 30f, 530f, footerPaint)
        canvas.drawText(context.getString(R.string.tech_pdf_footer_disclaimer), 30f, 550f, footerPaint)

        pdfDocument.finishPage(page)
        return pdfDocument
    }
}
