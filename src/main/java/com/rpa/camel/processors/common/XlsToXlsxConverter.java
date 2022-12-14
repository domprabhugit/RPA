package com.rpa.camel.processors.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFPictureData;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFShape;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFName;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTTwoCellAnchor;

public class XlsToXlsxConverter {
	@SuppressWarnings("unused")
	private static class FormulaInfo {

		private String sheetName;
		private Integer rowIndex;
		private Integer cellIndex;
		private String formula;

		private FormulaInfo(String sheetName, Integer rowIndex, Integer cellIndex, String formula) {
			this.sheetName = sheetName;
			this.rowIndex = rowIndex;
			this.cellIndex = cellIndex;
			this.formula = formula;
		}

		public String getSheetName() {
			return sheetName;
		}

		public void setSheetName(String sheetName) {
			this.sheetName = sheetName;
		}

		public Integer getRowIndex() {
			return rowIndex;
		}

		public void setRowIndex(Integer rowIndex) {
			this.rowIndex = rowIndex;
		}

		public Integer getCellIndex() {
			return cellIndex;
		}

		public void setCellIndex(Integer cellIndex) {
			this.cellIndex = cellIndex;
		}

		public String getFormula() {
			return formula;
		}

		public void setFormula(String formula) {
			this.formula = formula;
		}
	}

	static List<FormulaInfo> formulaInfoList = new ArrayList<FormulaInfo>();

	public static void refreshFormula(XSSFWorkbook workbook) {
		for (FormulaInfo formulaInfo : formulaInfoList) {
			workbook.getSheet(formulaInfo.getSheetName()).getRow(formulaInfo.getRowIndex())
			.getCell(formulaInfo.getCellIndex()).setCellFormula(formulaInfo.getFormula());
		}
		formulaInfoList.removeAll(formulaInfoList);
	}

	public static XSSFWorkbook convertWorkbookHSSFToXSSF(HSSFWorkbook source,boolean copyStyleForDateAlone,Integer[] dateColumnIndex) {
		XSSFWorkbook retVal = new XSSFWorkbook();

		for (int i = 0; i < source.getNumberOfSheets(); i++) {

			HSSFSheet hssfsheet = source.getSheetAt(i);
			XSSFSheet xssfSheet = retVal.createSheet(hssfsheet.getSheetName());

			copySheetSettings(xssfSheet, hssfsheet);
			copySheet(hssfsheet, xssfSheet,copyStyleForDateAlone,dateColumnIndex);
			copyPictures(xssfSheet, hssfsheet);
		}

		refreshFormula(retVal);

		return retVal;
	}

	private static void copySheet(HSSFSheet source, XSSFSheet destination, boolean copyStyleForDateAlone, Integer[] dateColumnIndex) {
		copySheet(source, destination, true,copyStyleForDateAlone,dateColumnIndex);
	}

	/**
	 * @param destination
	 * the sheet to create from the copy.
	 * @param the
	 * sheet to copy.
	 * @param copyStyle
	 * true copy the style.
	 * @param dateColumnIndex 
	 * @param copyStyleForDateAlone 
	 */
	private static void copySheet(HSSFSheet source, XSSFSheet destination, boolean copyStyle, boolean copyStyleForDateAlone, Integer[] dateColumnIndex) {
		int maxColumnNum = 0;
		List<CellStyle> styleMap2 = (copyStyle) ? new ArrayList<CellStyle>() : null;
		for (int i = source.getFirstRowNum(); i <= source.getLastRowNum(); i++) {
			HSSFRow srcRow = source.getRow(i);
			XSSFRow destRow = destination.createRow(i);
			if (srcRow != null) {
				// copyRow(source, destination, srcRow, destRow, styleMap);
				copyRow(source, destination, srcRow, destRow, styleMap2,copyStyleForDateAlone,dateColumnIndex);
				if (srcRow.getLastCellNum() > maxColumnNum) {
					maxColumnNum = srcRow.getLastCellNum();
				}
			}
		}
		for (int i = 0; i <= maxColumnNum; i++) {
			destination.setColumnWidth(i, source.getColumnWidth(i));
		}
	}

	/**
	 * @param srcSheet
	 * the sheet to copy.
	 * @param destSheet
	 * the sheet to create.
	 * @param srcRow
	 * the row to copy.
	 * @param destRow
	 * the row to create.
	 * @param styleMap
	 * -
	 * @param dateColumnIndex 
	 * @param copyStyleForDateAlone 
	 */
	private static void copyRow(HSSFSheet srcSheet, XSSFSheet destSheet, HSSFRow srcRow, XSSFRow destRow,
			// Map<Integer, HSSFCellStyle> styleMap) {
			List<CellStyle> styleMap, boolean copyStyleForDateAlone, Integer[] dateColumnIndex) {
		// manage a list of merged zone in order to not insert two times a
		// merged zone
		Set<CellRangeAddressWrapper> mergedRegions = new TreeSet<CellRangeAddressWrapper>();
		destRow.setHeight(srcRow.getHeight());
		// pour chaque row
		// for (int j = srcRow.getFirstCellNum(); j <= srcRow.getLastCellNum();
		// j++) {
		int j = srcRow.getFirstCellNum();
		if (j < 0) {
			j = 0;
		}
		for (; j <= srcRow.getLastCellNum(); j++) {
			HSSFCell oldCell = srcRow.getCell(j); // ancienne cell
			XSSFCell newCell = destRow.getCell(j); // new cell
			if (oldCell != null) {
				if (newCell == null) {
					newCell = destRow.createCell(j);
				}
				// copy chaque cell
				if(copyStyleForDateAlone){
					/*style copy for date columns*/
					if (ArrayUtils.contains(dateColumnIndex, j)) {
						copyCell(oldCell, newCell, styleMap);	
					}else{
						copyCell(oldCell, newCell, null);	
					}
				}else{
					/*style copy for all columns*/
					copyCell(oldCell, newCell, styleMap);
				}
				
				
				CellRangeAddress mergedRegion = getMergedRegion(srcSheet, srcRow.getRowNum(),
						(short) oldCell.getColumnIndex());

				if (mergedRegion != null) {
					// System.out.println("Selected merged region: " +
					// mergedRegion.toString());
					CellRangeAddress newMergedRegion = new CellRangeAddress(mergedRegion.getFirstRow(),
							mergedRegion.getLastRow(), mergedRegion.getFirstColumn(), mergedRegion.getLastColumn());
					// System.out.println("New merged region: " +
					// newMergedRegion.toString());
					CellRangeAddressWrapper wrapper = new CellRangeAddressWrapper(newMergedRegion);
					if (isNewMergedRegion(wrapper, mergedRegions)) {
						mergedRegions.add(wrapper);
						destSheet.addMergedRegion(wrapper.range);
					}
				}
			}
		}

	}

	/**
	 * R??cup??re les informations de fusion des cellules dans la sheet source
	 * pour les appliquer ?? la sheet destination... R??cup??re toutes les zones
	 * merged dans la sheet source et regarde pour chacune d'elle si elle se
	 * trouve dans la current row que nous traitons. Si oui, retourne l'objet
	 * CellRangeAddress.
	 *
	 * @param sheet
	 * the sheet containing the data.
	 * @param rowNum
	 * the num of the row to copy.
	 * @param cellNum
	 * the num of the cell to copy.
	 * @return the CellRangeAddress created.
	 */
	public static CellRangeAddress getMergedRegion(HSSFSheet sheet, int rowNum, short cellNum) {
		for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
			CellRangeAddress merged = sheet.getMergedRegion(i);
			if (merged.isInRange(rowNum, cellNum)) {
				return merged;
			}
		}
		return null;
	}

	/**
	 * Check that the merged region has been created in the destination sheet.
	 *
	 * @param newMergedRegion
	 * the merged region to copy or not in the destination sheet.
	 * @param mergedRegions
	 * the list containing all the merged region.
	 * @return true if the merged region is already in the list or not.
	 */
	private static boolean isNewMergedRegion(CellRangeAddressWrapper newMergedRegion,
			Set<CellRangeAddressWrapper> mergedRegions) {
		return !mergedRegions.contains(newMergedRegion);
	}

	@SuppressWarnings("rawtypes")
	private static void copyPictures(Sheet newSheet, Sheet sheet) {
		Drawing drawingOld = sheet.createDrawingPatriarch();
		Drawing drawingNew = newSheet.createDrawingPatriarch();
		CreationHelper helper = newSheet.getWorkbook().getCreationHelper();

		// if (drawingNew instanceof HSSFPatriarch) {
		if (drawingOld instanceof HSSFPatriarch) {
			List<HSSFShape> shapes = ((HSSFPatriarch) drawingOld).getChildren();
			for (int i = 0; i < shapes.size(); i++) {
				System.out.println(shapes.size());
				if (shapes.get(i) instanceof HSSFPicture) {
					HSSFPicture pic = (HSSFPicture) shapes.get(i);
					HSSFPictureData picdata = pic.getPictureData();
					int pictureIndex = newSheet.getWorkbook().addPicture(picdata.getData(), picdata.getFormat());
					ClientAnchor anchor = null;
					if (pic.getAnchor() != null) {
						anchor = helper.createClientAnchor();
						anchor.setDx1(((HSSFClientAnchor) pic.getAnchor()).getDx1());
						anchor.setDx2(((HSSFClientAnchor) pic.getAnchor()).getDx2());
						anchor.setDy1(((HSSFClientAnchor) pic.getAnchor()).getDy1());
						anchor.setDy2(((HSSFClientAnchor) pic.getAnchor()).getDy2());
						anchor.setCol1(((HSSFClientAnchor) pic.getAnchor()).getCol1());
						anchor.setCol2(((HSSFClientAnchor) pic.getAnchor()).getCol2());
						anchor.setRow1(((HSSFClientAnchor) pic.getAnchor()).getRow1());
						anchor.setRow2(((HSSFClientAnchor) pic.getAnchor()).getRow2());
						anchor.setAnchorType(((HSSFClientAnchor) pic.getAnchor()).getAnchorType());
					}
					drawingNew.createPicture(anchor, pictureIndex);
				}
			}
		} else {
			if (drawingNew instanceof XSSFDrawing) {
				List<XSSFShape> shapes = ((XSSFDrawing) drawingOld).getShapes();
				for (int i = 0; i < shapes.size(); i++) {
					if (shapes.get(i) instanceof XSSFPicture) {
						XSSFPicture pic = (XSSFPicture) shapes.get(i);
						XSSFPictureData picdata = pic.getPictureData();
						int pictureIndex = newSheet.getWorkbook().addPicture(picdata.getData(),
								picdata.getPictureType());
						XSSFClientAnchor anchor = null;
						CTTwoCellAnchor oldAnchor = ((XSSFDrawing) drawingOld).getCTDrawing().getTwoCellAnchorArray(i);
						if (oldAnchor != null) {
							anchor = (XSSFClientAnchor) helper.createClientAnchor();
							CTMarker markerFrom = oldAnchor.getFrom();
							CTMarker markerTo = oldAnchor.getTo();
							anchor.setDx1((int) markerFrom.getColOff());
							anchor.setDx2((int) markerTo.getColOff());
							anchor.setDy1((int) markerFrom.getRowOff());
							anchor.setDy2((int) markerTo.getRowOff());
							anchor.setCol1(markerFrom.getCol());
							anchor.setCol2(markerTo.getCol());
							anchor.setRow1(markerFrom.getRow());
							anchor.setRow2(markerTo.getRow());
						}
						drawingNew.createPicture(anchor, pictureIndex);
					}
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	private static void copyCell(Cell oldCell, Cell newCell, List<CellStyle> styleList) {
		if (styleList != null) {
			if (oldCell.getSheet().getWorkbook() == newCell.getSheet().getWorkbook()) {
				newCell.setCellStyle(oldCell.getCellStyle());
			} else {
				DataFormat newDataFormat = newCell.getSheet().getWorkbook().createDataFormat();

				CellStyle newCellStyle = getSameCellStyle(oldCell, newCell, styleList);
				if (newCellStyle == null) {
					// Create a new cell style
					Font oldFont = oldCell.getSheet().getWorkbook().getFontAt(oldCell.getCellStyle().getFontIndex());
					// Find a existing font corresponding to avoid to create a
					// new one
					Font newFont = newCell
							.getSheet()
							.getWorkbook()
							.findFont(oldFont.getBold(), oldFont.getColor(), oldFont.getFontHeight(),
									oldFont.getFontName(), oldFont.getItalic(), oldFont.getStrikeout(),
									oldFont.getTypeOffset(), oldFont.getUnderline());
					if (newFont == null) {
						newFont = newCell.getSheet().getWorkbook().createFont();
						newFont.setBold(oldFont.getBold());
						newFont.setColor(oldFont.getColor());
						newFont.setFontHeight(oldFont.getFontHeight());
						newFont.setFontName(oldFont.getFontName());
						newFont.setItalic(oldFont.getItalic());
						newFont.setStrikeout(oldFont.getStrikeout());
						newFont.setTypeOffset(oldFont.getTypeOffset());
						newFont.setUnderline(oldFont.getUnderline());
						newFont.setCharSet(oldFont.getCharSet());
					}

					short newFormat = newDataFormat.getFormat(oldCell.getCellStyle().getDataFormatString());
					newCellStyle = newCell.getSheet().getWorkbook().createCellStyle();
					newCellStyle.setFont(newFont);
					newCellStyle.setDataFormat(newFormat);

					newCellStyle.setAlignment(oldCell.getCellStyle().getAlignmentEnum());
					newCellStyle.setHidden(oldCell.getCellStyle().getHidden());
					newCellStyle.setLocked(oldCell.getCellStyle().getLocked());
					newCellStyle.setWrapText(oldCell.getCellStyle().getWrapText());
					newCellStyle.setBorderBottom(oldCell.getCellStyle().getBorderBottomEnum());
					newCellStyle.setBorderLeft(oldCell.getCellStyle().getBorderLeftEnum());
					newCellStyle.setBorderRight(oldCell.getCellStyle().getBorderRightEnum());
					newCellStyle.setBorderTop(oldCell.getCellStyle().getBorderTopEnum());
					newCellStyle.setBottomBorderColor(oldCell.getCellStyle().getBottomBorderColor());
					newCellStyle.setFillBackgroundColor(oldCell.getCellStyle().getFillBackgroundColor());
					newCellStyle.setFillForegroundColor(oldCell.getCellStyle().getFillForegroundColor());
					newCellStyle.setFillPattern(oldCell.getCellStyle().getFillPatternEnum());
					newCellStyle.setIndention(oldCell.getCellStyle().getIndention());
					newCellStyle.setLeftBorderColor(oldCell.getCellStyle().getLeftBorderColor());
					newCellStyle.setRightBorderColor(oldCell.getCellStyle().getRightBorderColor());
					newCellStyle.setRotation(oldCell.getCellStyle().getRotation());
					newCellStyle.setTopBorderColor(oldCell.getCellStyle().getTopBorderColor());
					newCellStyle.setVerticalAlignment(oldCell.getCellStyle().getVerticalAlignmentEnum());

					styleList.add(newCellStyle);
				}
				newCell.setCellStyle(newCellStyle);
			}
		}
		switch (oldCell.getCellType()) {
		case Cell.CELL_TYPE_STRING:
			newCell.setCellValue(oldCell.getStringCellValue());
			break;
		case Cell.CELL_TYPE_NUMERIC:
			newCell.setCellValue(oldCell.getNumericCellValue());
			break;
		case Cell.CELL_TYPE_BLANK:
			newCell.setCellType(Cell.CELL_TYPE_BLANK);
			break;
		case Cell.CELL_TYPE_BOOLEAN:
			newCell.setCellValue(oldCell.getBooleanCellValue());
			break;
		case Cell.CELL_TYPE_ERROR:
			newCell.setCellErrorValue(oldCell.getErrorCellValue());
			break;
		case Cell.CELL_TYPE_FORMULA:
			newCell.setCellFormula(oldCell.getCellFormula());
			formulaInfoList.add(new FormulaInfo(oldCell.getSheet().getSheetName(), oldCell.getRowIndex(), oldCell
					.getColumnIndex(), oldCell.getCellFormula()));
			break;
		default:
			break;
		}
	}

	@SuppressWarnings("deprecation")
	private static CellStyle getSameCellStyle(Cell oldCell, Cell newCell, List<CellStyle> styleList) {
		CellStyle styleToFind = oldCell.getCellStyle();
		CellStyle currentCellStyle = null;
		CellStyle returnCellStyle = null;
		Iterator<CellStyle> iterator = styleList.iterator();
		Font oldFont = null;
		Font newFont = null;
		while (iterator.hasNext() && returnCellStyle == null) {
			currentCellStyle = iterator.next();

			if (currentCellStyle.getAlignment() != styleToFind.getAlignment()) {
				continue;
			}
			if (currentCellStyle.getHidden() != styleToFind.getHidden()) {
				continue;
			}
			if (currentCellStyle.getLocked() != styleToFind.getLocked()) {
				continue;
			}
			if (currentCellStyle.getWrapText() != styleToFind.getWrapText()) {
				continue;
			}
			if (currentCellStyle.getBorderBottom() != styleToFind.getBorderBottom()) {
				continue;
			}
			if (currentCellStyle.getBorderLeft() != styleToFind.getBorderLeft()) {
				continue;
			}
			if (currentCellStyle.getBorderRight() != styleToFind.getBorderRight()) {
				continue;
			}
			if (currentCellStyle.getBorderTop() != styleToFind.getBorderTop()) {
				continue;
			}
			if (currentCellStyle.getBottomBorderColor() != styleToFind.getBottomBorderColor()) {
				continue;
			}
			if (currentCellStyle.getFillBackgroundColor() != styleToFind.getFillBackgroundColor()) {
				continue;
			}
			if (currentCellStyle.getFillForegroundColor() != styleToFind.getFillForegroundColor()) {
				continue;
			}
			if (currentCellStyle.getFillPattern() != styleToFind.getFillPattern()) {
				continue;
			}
			if (currentCellStyle.getIndention() != styleToFind.getIndention()) {
				continue;
			}
			if (currentCellStyle.getLeftBorderColor() != styleToFind.getLeftBorderColor()) {
				continue;
			}
			if (currentCellStyle.getRightBorderColor() != styleToFind.getRightBorderColor()) {
				continue;
			}
			if (currentCellStyle.getRotation() != styleToFind.getRotation()) {
				continue;
			}
			if (currentCellStyle.getTopBorderColor() != styleToFind.getTopBorderColor()) {
				continue;
			}
			if (currentCellStyle.getVerticalAlignment() != styleToFind.getVerticalAlignment()) {
				continue;
			}

			oldFont = oldCell.getSheet().getWorkbook().getFontAt(oldCell.getCellStyle().getFontIndex());
			newFont = newCell.getSheet().getWorkbook().getFontAt(currentCellStyle.getFontIndex());

			if (newFont.getBold() == oldFont.getBold()) {
				continue;
			}
			if (newFont.getColor() == oldFont.getColor()) {
				continue;
			}
			if (newFont.getFontHeight() == oldFont.getFontHeight()) {
				continue;
			}
			if (newFont.getFontName() == oldFont.getFontName()) {
				continue;
			}
			if (newFont.getItalic() == oldFont.getItalic()) {
				continue;
			}
			if (newFont.getStrikeout() == oldFont.getStrikeout()) {
				continue;
			}
			if (newFont.getTypeOffset() == oldFont.getTypeOffset()) {
				continue;
			}
			if (newFont.getUnderline() == oldFont.getUnderline()) {
				continue;
			}
			if (newFont.getCharSet() == oldFont.getCharSet()) {
				continue;
			}
			if (oldCell.getCellStyle().getDataFormatString().equals(currentCellStyle.getDataFormatString())) {
				continue;
			}

			returnCellStyle = currentCellStyle;
		}
		return returnCellStyle;
	}

	private static void copySheetSettings(Sheet newSheet, Sheet sheetToCopy) {

		newSheet.setAutobreaks(sheetToCopy.getAutobreaks());
		newSheet.setDefaultColumnWidth(sheetToCopy.getDefaultColumnWidth());
		newSheet.setDefaultRowHeight(sheetToCopy.getDefaultRowHeight());
		newSheet.setDefaultRowHeightInPoints(sheetToCopy.getDefaultRowHeightInPoints());
		newSheet.setDisplayGuts(sheetToCopy.getDisplayGuts());
		newSheet.setFitToPage(sheetToCopy.getFitToPage());

		newSheet.setForceFormulaRecalculation(sheetToCopy.getForceFormulaRecalculation());

		PrintSetup sheetToCopyPrintSetup = sheetToCopy.getPrintSetup();
		PrintSetup newSheetPrintSetup = newSheet.getPrintSetup();

		newSheetPrintSetup.setPaperSize(sheetToCopyPrintSetup.getPaperSize());
		newSheetPrintSetup.setScale(sheetToCopyPrintSetup.getScale());
		newSheetPrintSetup.setPageStart(sheetToCopyPrintSetup.getPageStart());
		newSheetPrintSetup.setFitWidth(sheetToCopyPrintSetup.getFitWidth());
		newSheetPrintSetup.setFitHeight(sheetToCopyPrintSetup.getFitHeight());
		newSheetPrintSetup.setLeftToRight(sheetToCopyPrintSetup.getLeftToRight());
		newSheetPrintSetup.setLandscape(sheetToCopyPrintSetup.getLandscape());
		newSheetPrintSetup.setValidSettings(sheetToCopyPrintSetup.getValidSettings());
		newSheetPrintSetup.setNoColor(sheetToCopyPrintSetup.getNoColor());
		newSheetPrintSetup.setDraft(sheetToCopyPrintSetup.getDraft());
		newSheetPrintSetup.setNotes(sheetToCopyPrintSetup.getNotes());
		newSheetPrintSetup.setNoOrientation(sheetToCopyPrintSetup.getNoOrientation());
		newSheetPrintSetup.setUsePage(sheetToCopyPrintSetup.getUsePage());
		newSheetPrintSetup.setHResolution(sheetToCopyPrintSetup.getHResolution());
		newSheetPrintSetup.setVResolution(sheetToCopyPrintSetup.getVResolution());
		newSheetPrintSetup.setHeaderMargin(sheetToCopyPrintSetup.getHeaderMargin());
		newSheetPrintSetup.setFooterMargin(sheetToCopyPrintSetup.getFooterMargin());
		newSheetPrintSetup.setCopies(sheetToCopyPrintSetup.getCopies());

		Header sheetToCopyHeader = sheetToCopy.getHeader();
		Header newSheetHeader = newSheet.getHeader();
		newSheetHeader.setCenter(sheetToCopyHeader.getCenter());
		newSheetHeader.setLeft(sheetToCopyHeader.getLeft());
		newSheetHeader.setRight(sheetToCopyHeader.getRight());

		Footer sheetToCopyFooter = sheetToCopy.getFooter();
		Footer newSheetFooter = newSheet.getFooter();
		newSheetFooter.setCenter(sheetToCopyFooter.getCenter());
		newSheetFooter.setLeft(sheetToCopyFooter.getLeft());
		newSheetFooter.setRight(sheetToCopyFooter.getRight());

		newSheet.setHorizontallyCenter(sheetToCopy.getHorizontallyCenter());
		newSheet.setMargin(Sheet.LeftMargin, sheetToCopy.getMargin(Sheet.LeftMargin));
		newSheet.setMargin(Sheet.RightMargin, sheetToCopy.getMargin(Sheet.RightMargin));
		newSheet.setMargin(Sheet.TopMargin, sheetToCopy.getMargin(Sheet.TopMargin));
		newSheet.setMargin(Sheet.BottomMargin, sheetToCopy.getMargin(Sheet.BottomMargin));

		newSheet.setPrintGridlines(sheetToCopy.isPrintGridlines());
		newSheet.setRowSumsBelow(sheetToCopy.getRowSumsBelow());
		newSheet.setRowSumsRight(sheetToCopy.getRowSumsRight());
		newSheet.setVerticallyCenter(sheetToCopy.getVerticallyCenter());
		newSheet.setDisplayFormulas(sheetToCopy.isDisplayFormulas());
		newSheet.setDisplayGridlines(sheetToCopy.isDisplayGridlines());
		newSheet.setDisplayRowColHeadings(sheetToCopy.isDisplayRowColHeadings());
		newSheet.setDisplayZeros(sheetToCopy.isDisplayZeros());
		newSheet.setPrintGridlines(sheetToCopy.isPrintGridlines());
		newSheet.setRightToLeft(sheetToCopy.isRightToLeft());
		newSheet.setZoom(10);
		copyPrintTitle(newSheet, sheetToCopy);
	}

	@SuppressWarnings("unused")
	private static void copyPrintTitle(Sheet newSheet, Sheet sheetToCopy) {
		int nbNames = sheetToCopy.getWorkbook().getNumberOfNames();
		Name name = null;
		String formula = null;

		String part1S = null;
		String part2S = null;
		String formS = null;
		String formF = null;
		String part1F = null;
		String part2F = null;
		int rowB = -1;
		int rowE = -1;
		int colB = -1;
		int colE = -1;

		for (int i = 0; i < nbNames; i++) {
			name = sheetToCopy.getWorkbook().getNameAt(i);
			if (name.getSheetIndex() == sheetToCopy.getWorkbook().getSheetIndex(sheetToCopy)) {
				if (name.getNameName().equals("Print_Titles")
						|| name.getNameName().equals(XSSFName.BUILTIN_PRINT_TITLE)) {
					formula = name.getRefersToFormula();
					int indexComma = formula.indexOf(",");
					if (indexComma == -1) {
						indexComma = formula.indexOf(";");
					}
					String firstPart = null;
					;
					String secondPart = null;
					if (indexComma == -1) {
						firstPart = formula;
					} else {
						firstPart = formula.substring(0, indexComma);
						secondPart = formula.substring(indexComma + 1);
					}

					formF = firstPart.substring(firstPart.indexOf("!") + 1);
					part1F = formF.substring(0, formF.indexOf(":"));
					part2F = formF.substring(formF.indexOf(":") + 1);

					if (secondPart != null) {
						formS = secondPart.substring(secondPart.indexOf("!") + 1);
						part1S = formS.substring(0, formS.indexOf(":"));
						part2S = formS.substring(formS.indexOf(":") + 1);
					}

					rowB = -1;
					rowE = -1;
					colB = -1;
					colE = -1;
					String rowBs, rowEs, colBs, colEs;
					if (part1F.lastIndexOf("$") != part1F.indexOf("$")) {
						rowBs = part1F.substring(part1F.lastIndexOf("$") + 1, part1F.length());
						rowEs = part2F.substring(part2F.lastIndexOf("$") + 1, part2F.length());
						rowB = Integer.parseInt(rowBs);
						rowE = Integer.parseInt(rowEs);
						if (secondPart != null) {
							colBs = part1S.substring(part1S.lastIndexOf("$") + 1, part1S.length());
							colEs = part2S.substring(part2S.lastIndexOf("$") + 1, part2S.length());
							colB = CellReference.convertColStringToIndex(colBs);// CExportExcelHelperPoi.convertColumnLetterToInt(colBs);
							colE = CellReference.convertColStringToIndex(colEs);// CExportExcelHelperPoi.convertColumnLetterToInt(colEs);
						}
					} else {
						colBs = part1F.substring(part1F.lastIndexOf("$") + 1, part1F.length());
						colEs = part2F.substring(part2F.lastIndexOf("$") + 1, part2F.length());
						colB = CellReference.convertColStringToIndex(colBs);// CExportExcelHelperPoi.convertColumnLetterToInt(colBs);
						colE = CellReference.convertColStringToIndex(colEs);// CExportExcelHelperPoi.convertColumnLetterToInt(colEs);

						if (secondPart != null) {
							rowBs = part1S.substring(part1S.lastIndexOf("$") + 1, part1S.length());
							rowEs = part2S.substring(part2S.lastIndexOf("$") + 1, part2S.length());
							rowB = Integer.parseInt(rowBs);
							rowE = Integer.parseInt(rowEs);
						}
					}
					/*newSheet.getWorkbook().setRepeatingRowsAndColumns(newSheet.getWorkbook().getSheetIndex(newSheet),
							colB, colE, rowB - 1, rowE - 1);*/
				}
			}
		}
	}
}