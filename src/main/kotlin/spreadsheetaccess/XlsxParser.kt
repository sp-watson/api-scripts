package spreadsheetaccess

import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.util.XMLHelper
import org.apache.poi.xssf.eventusermodel.XSSFReader
import org.apache.poi.xssf.model.SharedStringsTable
import org.xml.sax.Attributes
import org.xml.sax.ContentHandler
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.XMLReader
import org.xml.sax.helpers.DefaultHandler
import java.util.regex.Pattern

class XlsxParser(
    val fileName: String,
    val worksheetName: String,
) {
    fun onlineParse(offenderRowFound: (CellContents) -> Unit) {
        // XSSF (XML SpreadSheet Format) – Used to reading and writing Open Office XML (XLSX) format files.
        // HSSF (Horrible SpreadSheet Format) – Use to read and write Microsoft Excel (XLS) format files.
        val pkg: OPCPackage = OPCPackage.open(fileName)
        val reader = XSSFReader(pkg)
        val sst: SharedStringsTable = reader.sharedStringsTable
        val contentHandler = EntryHandler(sst, offenderRowFound)
        doParse(reader, contentHandler)
    }

    private fun doParse(reader: XSSFReader, contentHandler: DefaultHandler) {
        val parser = fetchSheetParser(contentHandler)
        // To look up the Sheet Name / Sheet Order / rID,
        //  you need to process the core Workbook stream.
        // Normally it's of the form rId# or rSheet#
        val iter = reader.sheetsData as XSSFReader.SheetIterator
        if (iter.hasNext()) { // We only use the first one
            iter.next().use { stream ->
                val sheetName = iter.sheetName
                println("Found sheet $sheetName")
                if (sheetName.equals(worksheetName)) {
                    val sheetSource = InputSource(stream)
                    parser.parse(sheetSource)
                }
            }
        }
    }

    private fun fetchSheetParser(contentHandler: ContentHandler): XMLReader {
        val parser = XMLHelper.newXMLReader()
        parser.contentHandler = contentHandler
        return parser
    }
}

data class CellContents (
    val columnName: String,
    val rowNumber: Int,
    val contents: String?,
)

/**
 * See org.xml.sax.helpers.DefaultHandler javadocs
 */
private class EntryHandler(
    private val sst: SharedStringsTable,
    private val cellContentsProcessor: (CellContents) -> Unit
) :
    DefaultHandler() {
    private var lastContents: String = ""
    private var lastCol: String = ""
    private var lastRow = 0
    private var nextIsString = false

    @Throws(SAXException::class)
    override fun startElement(
        uri: String, localName: String, name: String,
        attributes: Attributes
    ) {
        // c => cell
        if (name == "c") {
            val str = attributes.getValue("r")
            val re = Pattern.compile("([^0-9]*)([0-9]*)")
            val out = re.matcher(str)
            if (out.find()) {
                lastCol = out.group(1)
                lastRow = out.group(2).toInt()
            }
            // Figure out if the value is an index in the SST
            val cellType = attributes.getValue("t")
            nextIsString = if (cellType != null && cellType == "s") {
                true
            } else {
                false
            }
        }
        // Clear contents cache
        lastContents = ""
    }

    @Throws(SAXException::class)
    override fun endElement(uri: String, localName: String, name: String) {
        // Process the last contents as required.
        // Do now, as characters() may be called more than once
        if (nextIsString) {
            val idx = lastContents!!.toInt()
            lastContents = sst.getItemAt(idx).string
            nextIsString = false
        }
        // v => contents of a cell
        // Output after we've seen the string contents
        if (name == "v") {
            val cleansedContents = if ("#N/A".equals(lastContents)) null else lastContents
            cellContentsProcessor(CellContents(lastCol, lastRow, cleansedContents))
        }
    }

    override fun characters(ch: CharArray, start: Int, length: Int) {
        lastContents += String(ch, start, length)
    }
}
