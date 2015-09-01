package Servlet;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import Tookit.PostData;
import Tookit.OperDataBase;
public class RegisterServlet extends HttpServlet{
    String url = "https://api.cn.ronghub.com/user/getToken.json";
    List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(1);
    PostData postData = new PostData();
     PrintWriter out = null;
    private void returnFail(){
        out.print("{\"code\":404}");
        OperDataBase. closeConn(); //关闭数据库
        return;
    }
    private String getJSONToken(String json){
        try{
            JSONObject jsonObject = JSONObject.fromObject(json);
            String token = jsonObject.getString("token");
            return token;
        }catch(Exception e){
            return "null";
        }
    }
    public void doPost(HttpServletRequest  request,HttpServletResponse response)throws IOException,ServletException
    {
        out = response.getWriter();
        /*
         *获取参数
         * */        
        String username =  request.getParameter("username");
        String nickname =  request.getParameter("nickname");
        String password =  request.getParameter("password");
        /*
         *查询数据库
         * */
        String sql = "select * from User where username=? and password =?";
        OperDataBase.init("FunnyChat",sql);
        OperDataBase.addSQLData(1,username);
        OperDataBase.addSQLData(2,password);
        ResultSet rs = OperDataBase.exeGetDataSQL();
        //查询到了 已经被注册
        try{
        if(rs.next()){
            returnFail();
            return;
        }
        }catch(Exception e){
        }
        OperDataBase. closeConn(); //关闭数据库
        /*
         *没有被注册
         *向数据库添加数据 获取Userid
         * */
        sql = "insert into User(username,nickname,password,token) values(?,?,?,?)";
        OperDataBase.init("FunnyChat",sql);
        OperDataBase.addSQLData(1,username);
        OperDataBase.addSQLData(2,nickname);
        OperDataBase.addSQLData(3,password);
        OperDataBase.addSQLData(4,"null");
        if(OperDataBase.exeSQL() == -2){ //添加失败
            returnFail();
            return;
        }
       OperDataBase. closeConn(); //关闭数据库

        /*
         *获取数据库userid
         * */
        sql = "select * from User where username=?";
        OperDataBase.init("FunnyChat",sql);
        OperDataBase.addSQLData(1,username);
        rs = OperDataBase.exeGetDataSQL();
        String userId = null;
        try{
        if(rs.next()){
            userId = Integer.toString(rs.getInt("userId"));
        }else{
            returnFail(); // 查询失败
            return ;
        }
        }catch(Exception e){
        }

        nameValuePair.add(new BasicNameValuePair("userId",userId));
        nameValuePair.add(new BasicNameValuePair("name",nickname));
        String res = postData.getData(url,nameValuePair);
        out.print(res);
        //获取token 存储
        String token = getJSONToken(res); 
        sql = "update User set token=? where username=?";
        OperDataBase.init("FunnyChat",sql);
        OperDataBase.addSQLData(1,token);
        OperDataBase.addSQLData(2,username);
        if(OperDataBase.exeSQL() == -2){
            returnFail(); //插入失败
        }
    }
    public void doGet(HttpServletRequest request,HttpServletResponse response)throws IOException,ServletException 
    {
        doPost(request,response);
    }
}
