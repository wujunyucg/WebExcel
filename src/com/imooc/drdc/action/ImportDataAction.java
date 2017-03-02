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
	
	
	//�����б�
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
	 * �ļ��ϴ�������
	 * @author David
	 */
	public void upload(){
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("text/html;charset=utf-8");
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String dateNow = df.format(new Date());
		
		//����������Ϣ
		ImportData importData = new ImportData();
		importData.setImportid(String.valueOf(System.currentTimeMillis()));
		importData.setImportDataType(templateId);
		importData.setImportDate(dateNow);
		importData.setImportStatus("1");//����ɹ�
		importData.setHandleDate(null);
		importData.setHandleStatus("0");//δ����
		importDataService.saveImportData(importData);
		
		
		try {
			//��ȡExcel�ļ�
			HSSFWorkbook wb = new HSSFWorkbook(FileUtils.openInputStream(fileInput));
			HSSFSheet sheet = wb.getSheetAt(0);
			
			//��ȡģ���ļ�
			String path = ServletActionContext.getServletContext().getRealPath("/template");
			path = path + "\\" + templateId + ".xml";
			File file = new File(path);
			
			//����xmlģ���ļ�
			SAXBuilder builder = new SAXBuilder();
			Document parse =  builder.build(file);
			Element root = parse.getRootElement();
			Element tbody = root.getChild("tbody");
			Element tr = tbody.getChild("tr");
			List<Element> children = tr.getChildren("td");
			//����excel��ʼ�У���ʼ��
			int firstRow = tr.getAttribute("firstrow").getIntValue();
			int firstCol = tr.getAttribute("firstcol").getIntValue();
			//��ȡexcel���һ���к�
			int lastRowNum = sheet.getLastRowNum();
			//ѭ��ÿһ�д�������
			for (int i = firstRow; i <= lastRowNum; i++) {
				//��ʼ����ϸ����
				ImportDataDetail importDataDetail = new ImportDataDetail();
				importDataDetail.setImportId(importData.getImportid());
				importDataDetail.setCgbz("0");//δ����
				//��ȡĳ��
				HSSFRow row = sheet.getRow(i);
				//�жϸ����Ƿ�Ϊ��
				if(isEmptyRow(row)){
					continue;
				}
				int lastCellNum = row.getLastCellNum();
				//����ǿ��У���ȡ���е�Ԫ���ֵ
				for (int j = firstCol; j <lastCellNum; j++) {
					Element td = children.get(j-firstCol);
					HSSFCell cell = row.getCell(j);
					//�����Ԫ��Ϊnull,����������һ��cell
					if(cell == null){
						continue;
					}
					//��ȡ��Ԫ������ֵ
					String value = getCellValue(cell,td);
					//������ϸʵ�帳ֵ
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
			
			String str = "{\"status\":\"ok\",\"message\":\"����ɹ���\"}";
			response.getWriter().write(str);
		} catch (Exception e) {
			String str = "{\"status\":\"noOk\",\"message\":\"����ʧ�ܣ�\"}";
			try {
				response.getWriter().write(str);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}

	/**
	 * �ж�ĳ���Ƿ�Ϊ��
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
	 * ��ȡ��Ԫ��ֵ�����ҽ���У��
	 * @author David
	 * @param cell
	 * @param td
	 * @return
	 */
	private String getCellValue(HSSFCell cell, Element td) {
		//���Ȼ�ȡ��Ԫ��λ��
		int i = cell.getRowIndex() + 1;
		int j = cell.getColumnIndex()+1;
		String returnValue = "";//����ֵ
		
		try {
			//��ȡģ���ļ��Ե�Ԫ���ʽ����
			String type = td.getAttribute("type").getValue();
			boolean isNullAble = td.getAttribute("isnullable").getBooleanValue();
			int maxlength = 9999;
			
			if(td.getAttribute("maxlength")!=null){
				maxlength = td.getAttribute("maxlength").getIntValue();
			}
			String value = null;
			//���ݸ�ʽȡ����Ԫ���ֵ
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
			
			//�Էǿա����Ƚ���У��
			if(!isNullAble && StringUtils.isBlank(value)){
				//�������,����λ��ԭ��,��λ���ֵ
				returnValue = "#0001,��" + i + "�е�" +j +"�в���Ϊ�գ�," + value;
			}else if(StringUtils.isNotBlank(value) && (value.length()>maxlength)){
				returnValue = "#0002,��" + i + "�е�" +j +"�г��ȳ�����󳤶ȣ�," + value;
			}else{
				returnValue =  value;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnValue;
	}
	/**
	 * ��̬��ȡ��ͷ��Ϣ
	 * @author David
	 */
	public void columns(){
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("text/html;charset=utf-8");
		//��ȡ��ͷ��Ϣ
		List<ColumnInfo> list = getColumns();
		//ת��json���󷵻�
		String json ="["+ JSONArray.fromObject(list).toString() + "]";
		try {
			response.getWriter().write(json);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * ��̬��ȡ��ͷ
	 * @author David
	 * @return
	 */
	private List<ColumnInfo> getColumns() {
		List<ColumnInfo> list = new ArrayList<ColumnInfo>();
		//��ȡģ���ļ�
		String path = ServletActionContext.getServletContext().getRealPath("/template");
		path = path + "\\" + templateId + ".xml";
		File file = new File(path);
		
		//����ģ���ļ�
		SAXBuilder builder = new SAXBuilder();
		try {
			Document parse = builder.build(file);
			Element root = parse.getRootElement();
			Element thead = root.getChild("thead");
			Element tr = thead.getChild("tr");
			List<Element> children = tr.getChildren();
			
			ColumnInfo c = new ColumnInfo();
			//��Ӵ����־��ʧ�ܴ��룬ʧ��˵��
			c = createColumnInfo("cgbz","�����־",120,"center");
			list.add(c);
			c = createColumnInfo("hcode","ʧ�ܴ���",120,"center");
			list.add(c);
			c = createColumnInfo("msg","ʧ��˵��",120,"center");
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
	 * ����column����
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
	 * ��ȡ��ϸ����
	 * @author David
	 */
	public void columndatas(){
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("text/html;charset=utf-8");
		//��ȡ��ϸ����
		List<ImportDataDetail> list = importDataService.getImportDataDetailsByMainId(importDataId);
		String json = "{\"total\":"+list.size()+", \"rows\":"+JSONArray.fromObject(list).toString()+"}";
		try {
			response.getWriter().write(json);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * ȷ�ϵ���
	 * @author David
	 */
	public void doimport(){
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("text/html;charset=utf-8");
		//���������ϸ�����ѵ�student����
		importDataService.saveStudents(importDataId);
		//�޸�������ϸ�����־��ʱ��
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateNow = sf.format(new Date());
		importDataService.updImportDataStatus(dateNow, importDataId);
		importDataService.updImportDataDetailStatus(importDataId);
		String str = "{\"status\":\"ok\",\"message\":\"ȷ�ϳɹ���\"}";
		try {
			response.getWriter().write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
