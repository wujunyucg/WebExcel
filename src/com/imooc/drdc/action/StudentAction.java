package com.imooc.drdc.action;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.struts2.ServletActionContext;

import com.imooc.drdc.model.Student;
import com.imooc.drdc.service.StudentService;
import com.imooc.drdc.utils.ExportUtils;
import com.opensymphony.xwork2.ActionSupport;

/**
 *  学生管理
 * @author David
 *
 */
public class StudentAction extends ActionSupport {

	private static final long serialVersionUID = 1L;
	private StudentService studentService = new StudentService();
	private int page;
	private int rows;
	private String sort;
	private String order;
	
	private String className;
	private String methodName;
	
	private String fields;
	private String titles;
	
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
	public String getFields() {
		return fields;
	}

	public void setFields(String fields) {
		this.fields = fields;
	}

	public String getTitles() {
		return titles;
	}

	public void setTitles(String titles) {
		this.titles = titles;
	}
	
	
	public StudentService getStudentService() {
		return studentService;
	}

	public void setStudentService(StudentService studentService) {
		this.studentService = studentService;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
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


	
	
	//数据列表
	
	public void list(){
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("text/html;charset=utf-8");
		List<Student>slist = getStudents();		
		int total = studentService.getTotal();
		String json = "{\"total\":"+total+" , " +
				"\"rows\":"+JSONArray.fromObject(slist).toString()+ "," +
				"\"className\":\"" + StudentAction.class.getName() + "\"" +
				"}";
		try {	
			response.getWriter().write(json);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 获取学生信息
	 * @author David
	 * @return
	 */
	public List<Student> getStudents(){
		List<Student>slist = studentService.list(page,rows,sort,order);	
		return slist;
	}
	/**
	 * 导出前台列表为excel文件
	 * @author David
	 */
	public void export(){
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment;filename=export.xls");
		//创建Excel
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("sheet0");
		try {
			//获取类
			Class clazz = Class.forName(className);
			Object o = clazz.newInstance();
			Method m = clazz.getDeclaredMethod(methodName);
			List list = (List)m.invoke(o);
			titles = new String(titles.getBytes("ISO-8859-1"),"UTF-8");
			ExportUtils.outputHeaders(titles.split(","), sheet);
			ExportUtils.outputColumns(fields.split(","), list, sheet, 1);
			//获取输出流，写入excel 并关闭
			ServletOutputStream out = response.getOutputStream();
			wb.write(out);
			out.flush();
			out.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
