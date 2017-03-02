package com.imooc.drdc.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.imooc.drdc.model.ImportData;
import com.imooc.drdc.model.ImportDataDetail;
import com.imooc.drdc.utils.DB;


public class ImportDataService {
	/**
	 * 查询导入列表信息
	 * @author David
	 * @param currentPage
	 * @param pageSize
	 * @param sort
	 * @param order
	 * @return
	 */
	public List<ImportData> list(int currentPage, int pageSize,String sort,String order) {
		Connection conn = DB.createConn();
		String sql = "select * from t_importdata where 1=1 ";
		if(StringUtils.isNotBlank(sort)){
			sql += "order by " + sort ;
		}
		if(StringUtils.isNotBlank(order)){
			sql += " " + order ;
		}
		sql += " limit " + (currentPage-1)*pageSize +" , "  + pageSize ;
		PreparedStatement ps = DB.prepare(conn, sql);
		List<ImportData> importDatas = new ArrayList<ImportData>();
		try {
			ResultSet rs = ps.executeQuery();
			ImportData i = null;
			while(rs.next()) {
				i = new ImportData();
				i.setImportid(rs.getString("importid"));
				i.setImportDataType(rs.getString("importdatatype"));
				i.setImportDate(rs.getString("importdate"));
				i.setImportStatus(rs.getString("importstatus"));
				i.setHandleDate(rs.getString("handledate"));
				i.setHandleStatus(rs.getString("handlestatus"));
				importDatas.add(i);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		DB.close(ps);
		DB.close(conn);
		return importDatas;
	}
	/**
	 * 保存主表信息
	 * @author David
	 * @param i
	 */
	public void saveImportData(ImportData i){
		Connection conn = DB.createConn();
		String sql = "insert into t_importdata(importid,importdatatype,importdate,importstatus," +
				"handledate,handlestatus) values(?,?,?,?,?,?)";
		PreparedStatement ps = DB.prepare(conn, sql);
		
		try {
			ps.setString(1, i.getImportid());
			ps.setString(2, i.getImportDataType());
			ps.setString(3, i.getImportDate());
			ps.setString(4, i.getImportStatus());
			ps.setString(5, i.getHandleDate());
			ps.setString(6, i.getHandleStatus());
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		DB.close(ps);
		DB.close(conn);
	}
	
	/**
	 * 保存明细表数据
	 * @author David
	 * @param i
	 */
	public void saveImportDataDetail(ImportDataDetail i){
		Connection conn = DB.createConn();
		String sql = "insert into t_importdatadetail(importid,cgbz,hcode,msg," +
				"col0,col1,col2,col3,col4,col5,col6,col7,col8,col9,col10) " +
				"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement ps = DB.prepare(conn, sql);
		
		try {
			ps.setString(1, i.getImportId());
			ps.setString(2, i.getCgbz());
			ps.setString(3, i.getHcode());
			ps.setString(4, i.getMsg());
			ps.setString(5, i.getCol0());
			ps.setString(6, i.getCol1());
			ps.setString(7, i.getCol2());
			ps.setString(8, i.getCol3());
			ps.setString(9, i.getCol4());
			ps.setString(10, i.getCol5());
			ps.setString(11, i.getCol6());
			ps.setString(12, i.getCol7());
			ps.setString(13, i.getCol8());
			ps.setString(14, i.getCol9());
			ps.setString(15, i.getCol10());
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		DB.close(ps);
		DB.close(conn);
	}
	/**
	 * 根据主表id获取明细信息
	 * @author David
	 * @param importDataId
	 * @return
	 */
	public List<ImportDataDetail> getImportDataDetailsByMainId(String importDataId) {
		Connection conn = DB.createConn();
		String sql = "select * from t_importdatadetail where importid = '" + importDataId + "'";
		
		PreparedStatement ps = DB.prepare(conn, sql);
		List<ImportDataDetail> importDataDetails = new ArrayList<ImportDataDetail>();
		try {
			ResultSet rs = ps.executeQuery();
			ImportDataDetail i = null;
			while(rs.next()) {
				i = new ImportDataDetail();
				i.setImportDetailId(rs.getInt("importdetailid"));
				i.setImportId(rs.getString("importid"));
				i.setCgbz("1".equals(rs.getString("cgbz"))?"已处理":"未处理");
				i.setHcode(rs.getString("hcode"));
				i.setMsg(rs.getString("msg"));
				i.setCol0(rs.getString("col0"));
				i.setCol1(rs.getString("col1"));
				i.setCol2(rs.getString("col2"));
				i.setCol3(rs.getString("col3"));
				i.setCol4(rs.getString("col4"));
				i.setCol5(rs.getString("col5"));
				i.setCol6(rs.getString("col6"));
				i.setCol7(rs.getString("col7"));
				i.setCol8(rs.getString("col8"));
				i.setCol9(rs.getString("col9"));
				i.setCol10(rs.getString("col10"));
				importDataDetails.add(i);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		DB.close(ps);
		DB.close(conn);
		return importDataDetails;
	}
	
	/**
	 * 保存student
	 * @author David
	 * @param importId
	 */
	public void saveStudents(String importId) {
		Connection conn = DB.createConn();
		String id = Long.toString(System.currentTimeMillis());
		String sql = "insert into t_student(stunum,stuname,stuage,stusex,stubirthday,stuhobby) " +
				"select col0,col1,col2,col3,col4,col5 from t_importdatadetail " +
				" where importid = '" + importId + "'";
		PreparedStatement ps = DB.prepare(conn, sql);
		try {
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		DB.close(ps);
		DB.close(conn);
	}
	/*
	 * 更新主表处理标志
	 */
	public void updImportDataStatus(String handleDate,String importId) {
		Connection conn = DB.createConn();
		String id = Long.toString(System.currentTimeMillis());
		String sql = "update t_importdata set handledate =?,handlestatus='1' where importid = ?";
				
		PreparedStatement ps = DB.prepare(conn, sql);
		try {
			ps.setString(1,handleDate);
			ps.setString(2,importId);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		DB.close(ps);
		DB.close(conn);
	}
	/**
	 * 更新明细表处理标志
	 * @author David
	 * @param importId
	 */
	public void updImportDataDetailStatus(String importId) {
		Connection conn = DB.createConn();
		String id = Long.toString(System.currentTimeMillis());
		String sql = "update t_importdatadetail set cgbz ='1',hcode='无',msg='处理成功！' where importid = ?";
				
		PreparedStatement ps = DB.prepare(conn, sql);
		try {
			ps.setString(1,importId);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		DB.close(ps);
		DB.close(conn);
	}
	
}
