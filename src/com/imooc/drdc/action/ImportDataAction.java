package com.imooc.drdc.action;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.struts2.ServletActionContext;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.imooc.drdc.model.ColumnInfo;
import com.imooc.drdc.model.ImportData;
import com.imooc.drdc.model.ImportDataDetail;
import com.imooc.drdc.model.Template;
import com.imooc.drdc.service.ImportDataService;
import com.opensymphony.xwork2.ActionSupport;



public class ImportDataAction extends ActionSupport {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ImportDataService importDataService = new ImportDataService();
	private List<ImportData> importDataList = new ArrayList<ImportData>();
	private int page;
	private int rows;
	private String sort;
	private String order;
	
	private String templateId;
	private File fileInput;
	
	private String importDataId;

	public String getImportDataId() {
		return importDataId;
	}

	public void setImportDataId(String importDataId) {
		this.importDataId = importDataId;
	}

	public File getFileInput() {
		return fileInput;
	}

	public void setFileInput(File fileInput) {
		this.fileInput = fileInput;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}
	
	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}
	public ImportDataService getImportDataService() {
		return importDataService;
	}

	public void setImportDataService(ImportDataService importDataService) {
		this.importDataService = importDataService;
	}

	public List<ImportData> getImportDataList() {
		return importDataList;
	}

	public void setImportDataList(List<ImportData> importDataList) {
		this.importDataList = importDataList;
	}
	
	
	//数据列表
	public void list(){
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("text/html;charset=utf-8");
		importDataList = importDataService.list(page,rows,sort,order);		
		String json = "{\"total\":"+importDataList.size()+" , " +
				"\"rows\":"+JSONArray.fromObject(importDataList).toString()+"}";
		try {	
			response.getWriter().write(json);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void templates(){
		HttpServletResponse response = 
			ServletActionContext.getResponse();
		response.setContentType("text/html;charset=utf-8");
		
		List<Template> list = new ArrayList<Template>();
		Template t = new Template();
		t.setTemplateId("student");
		t.setTemplateName("student");
		list.add(t);
		
		try {
			response.getWriter().write(JSONArray.fromObject(list).toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 文件上传处理方法
	 * @author David
	 */
	public void upload(){
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("text/html;charset=utf-8");
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String dateNow = df.format(new Date());
		
		//保存主表信息
		ImportData importData = new ImportData();
		importData.setImportid(String.valueOf(System.currentTimeMillis()));
		importData.setImportDataType(templateId);
		importData.setImportDate(dateNow);
		importData.setImportStatus("1");//导入成功
		importData.setHandleDate(null);
		importData.setHandleStatus("0");//未处理
		importDataService.saveImportData(importData);
		
		
		try {
			//读取Excel文件
			HSSFWorkbook wb = new HSSFWorkbook(FileUtils.openInputStream(fileInput));
			HSSFSheet sheet = wb.getSheetAt(0);
			
			//获取模板文件
			String path = ServletActionContext.getServletContext().getRealPath("/template");
			path = path + "\\" + templateId + ".xml";
			File file = new File(path);
			
			//解析xml模板文件
			SAXBuilder builder = new SAXBuilder();
			Document parse =  builder.build(file);
			Element root = parse.getRootElement();
			Element tbody = root.getChild("tbody");
			Element tr = tbody.getChild("tr");
			List<Element> children = tr.getChildren("td");
			//解析excel开始行，开始列
			int firstRow = tr.getAttribute("firstrow").getIntValue();
			int firstCol = tr.getAttribute("firstcol").getIntValue();
			//获取excel最后一行行号
			int lastRowNum = sheet.getLastRowNum();
			//循环每一行处理数据
			for (int i = firstRow; i <= lastRowNum; i++) {
				//初始化明细数据
				ImportDataDetail importDataDetail = new ImportDataDetail();
				importDataDetail.setImportId(importData.getImportid());
				importDataDetail.setCgbz("0");//未处理
				//读取某行
				HSSFRow row = sheet.getRow(i);
				//判断改行是否为空
				if(isEmptyRow(row)){
					continue;
				}
				int lastCellNum = row.getLastCellNum();
				//如果非空行，则取所有单元格的值
				for (int j = firstCol; j <lastCellNum; j++) {
					Element td = children.get(j-firstCol);
					HSSFCell cell = row.getCell(j);
					//如果单元格为null,继续处理下一个cell
					if(cell == null){
						continue;
					}
					//获取单元格属性值
					String value = getCellValue(cell,td);
					//导入明细实体赋值
					if(StringUtils.isNotBlank(value)){
						if(value.indexOf("#000")>=0){
							String[] info = value.split(",");
							importDataDetail.setHcode(info[0]);
							importDataDetail.setMsg(info[1]);
							BeanUtils.setProperty(importDataDetail, "col" + j, info[2]);
						}else{
							BeanUtils.setProperty(importDataDetail, "col" + j, value);
						}
					}
					
				}
				importDataService.saveImportDataDetail(importDataDetail);
			}
			
			String str = "{\"status\":\"ok\",\"message\":\"导入成功！\"}";
			response.getWriter().write(str);
		} catch (Exception e) {
			String str = "{\"status\":\"noOk\",\"message\":\"导入失败！\"}";
			try {
				response.getWriter().write(str);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}

	/**
	 * 判断某行是否为空
	 * @author David
	 * @return
	 */
	private boolean isEmptyRow(HSSFRow row) {
		boolean flag = true;
		for (int i = 0; i < row.getLastCellNum(); i++) {
			HSSFCell cell = row.getCell(i);
			if(cell != null){
				if(StringUtils.isNotBlank(cell.toString())){
					return false;
				}
			}
		}
		
		return flag;
	}
	/**
	 * 获取单元格值，并且进行校验
	 * @author David
	 * @param cell
	 * @param td
	 * @return
	 */
	private String getCellValue(HSSFCell cell, Element td) {
		//首先获取单元格位置
		int i = cell.getRowIndex() + 1;
		int j = cell.getColumnIndex()+1;
		String returnValue = "";//返回值
		
		try {
			//获取模板文件对单元格格式限制
			String type = td.getAttribute("type").getValue();
			boolean isNullAble = td.getAttribute("isnullable").getBooleanValue();
			int maxlength = 9999;
			
			if(td.getAttribute("maxlength")!=null){
				maxlength = td.getAttribute("maxlength").getIntValue();
			}
			String value = null;
			//根据格式取出单元格的值
			switch (cell.getCellType()) {
				case HSSFCell.CELL_TYPE_STRING:{
					value = cell.getStringCellValue();
					break;
				}
				case HSSFCell.CELL_TYPE_NUMERIC:{
					if("datetime,date".indexOf(type)>=0){
						Date date = cell.getDateCellValue();
						SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
						value = df.format(date);
					}else{
						double numericCellValue = cell.getNumericCellValue();
						value = String.valueOf(numericCellValue);
					}
					break;
				}
			}
			
			//对非空、长度进行校验
			if(!isNullAble && StringUtils.isBlank(value)){
				//错误编码,错误位置原因,单位格的值
				returnValue = "#0001,第" + i + "行第" +j +"列不能为空！," + value;
			}else if(StringUtils.isNotBlank(value) && (value.length()>maxlength)){
				returnValue = "#0002,第" + i + "行第" +j +"列长度超过最大长度！," + value;
			}else{
				returnValue =  value;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnValue;
	}
	/**
	 * 动态获取表头信息
	 * @author David
	 */
	public void columns(){
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("text/html;charset=utf-8");
		//获取表头信息
		List<ColumnInfo> list = getColumns();
		//转换json对象返回
		String json ="["+ JSONArray.fromObject(list).toString() + "]";
		try {
			response.getWriter().write(json);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 动态获取表头
	 * @author David
	 * @return
	 */
	private List<ColumnInfo> getColumns() {
		List<ColumnInfo> list = new ArrayList<ColumnInfo>();
		//获取模板文件
		String path = ServletActionContext.getServletContext().getRealPath("/template");
		path = path + "\\" + templateId + ".xml";
		File file = new File(path);
		
		//解析模板文件
		SAXBuilder builder = new SAXBuilder();
		try {
			Document parse = builder.build(file);
			Element root = parse.getRootElement();
			Element thead = root.getChild("thead");
			Element tr = thead.getChild("tr");
			List<Element> children = tr.getChildren();
			
			ColumnInfo c = new ColumnInfo();
			//添加处理标志、失败代码，失败说明
			c = createColumnInfo("cgbz","处理标志",120,"center");
			list.add(c);
			c = createColumnInfo("hcode","失败代码",120,"center");
			list.add(c);
			c = createColumnInfo("msg","失败说明",120,"center");
			list.add(c);
			for (int i = 0; i < children.size(); i++) {
				Element th = children.get(i);
				String value = th.getAttribute("value").getValue();
				c = createColumnInfo("col"+i,value,120,"center");
				list.add(c);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		return list;
	}
	/**
	 * 创建column对象
	 * @author David
	 * @param string
	 * @param string2
	 * @param i
	 * @param string3
	 */
	private ColumnInfo createColumnInfo(String fieldId, String title, int width,
			String align) {
		ColumnInfo c = new ColumnInfo();
		c.setField(fieldId);
		c.setTitle(title);
		c.setWidth(width);
		c.setAlign(align);
		return c;
	}
	/**
	 * 获取明细数据
	 * @author David
	 */
	public void columndatas(){
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("text/html;charset=utf-8");
		//获取明细数据
		List<ImportDataDetail> list = importDataService.getImportDataDetailsByMainId(importDataId);
		String json = "{\"total\":"+list.size()+", \"rows\":"+JSONArray.fromObject(list).toString()+"}";
		try {
			response.getWriter().write(json);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 确认导入
	 * @author David
	 */
	public void doimport(){
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("text/html;charset=utf-8");
		//将导入的明细数据已到student表中
		importDataService.saveStudents(importDataId);
		//修改主表、明细表处理标志及时间
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateNow = sf.format(new Date());
		importDataService.updImportDataStatus(dateNow, importDataId);
		importDataService.updImportDataDetailStatus(importDataId);
		String str = "{\"status\":\"ok\",\"message\":\"确认成功！\"}";
		try {
			response.getWriter().write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
