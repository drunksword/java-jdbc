import java.sql.*;
import java.util.*;
import java.io.*;
import java.lang.Exception;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.core.IdentifyCmd;

public class DealCardImg200 {
  private static final String db = "jdbc:sqlserver://127.0.0.1:1433;DatabaseName=testdb";
  private static final String username = "**";
  private static final String password = "**";

  private static final String imageMagickPath = "C:/Program Files (x86)/ImageMagick-6.3.9-Q16";

  public static void main(String [] args){
    Connection conn = null;
    Statement stmt = null;
    ResultSet rs = null;

    BufferedWriter outSuccess = null;
    BufferedWriter outFail = null;
    try{
      Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
      conn = DriverManager.getConnection(db, username, password);
      conn.setAutoCommit(false);
      conn.setHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT);

      if(conn == null){
        System.out.println("数据库连接失败！");
        return;
      }

      int count = 0;
      File folder = new File("E:/test/files/cardimage");
      File successFile = new File("successLog.txt");
      File errorFile = new File("errorLog.txt");
      outSuccess = new BufferedWriter(new FileWriter(successFile, false));
      outFail = new BufferedWriter(new FileWriter(errorFile, false));

	  stmt = conn.createStatement();
      String sql = "select * from card_image where status = 0 and charindex('_v1', imageName) <= 0";
      rs = stmt.executeQuery(sql);
      
      String updateSqlFormat = "update card_image set imageName = '%s' where imageName = '%s' and nameCardId = %d";
      while(rs.next()){
        String imageName = rs.getString("imageName");
        int cardId = rs.getInt("nameCardId");
        System.out.println("deal the " + ++count + "th piece of cardimage, cardId=" + cardId + ";imageName=" + imageName);
       
        String originName = imageName.substring(0, imageName.lastIndexOf("."));
        String suffix = imageName.substring(imageName.lastIndexOf("."));

		File originFile = new File(folder, imageName);
		File newFile = new File(folder, originName + "_v1" + suffix);
		FileInputStream fi = new FileInputStream(originFile);
		FileOutputStream fo = new FileOutputStream(newFile);
		BufferedInputStream bis = new BufferedInputStream(fi);
		BufferedOutputStream bos = new BufferedOutputStream(fo);
		byte [] temp = new byte[1024];
		int index = 0;
		while((index = bis.read(temp)) != -1){
			bos.write(temp, 0, index);
		}
		bos.close();
		bis.close();
		fo.close();
		fi.close();

        File originCutFile = new File(folder, originName + "_cut" + suffix);
        if(!originCutFile.exists()){
          outFail.write(cardId + ", " + originName + ", not exists \r\n");
          continue;
        }
        File newCutFile = new File(folder, originName + "_v1_cut" + suffix);
        makeThumbnail(originCutFile.getPath(), newCutFile.getPath(), 200, 200);

        int num = conn.createStatement().executeUpdate(String.format(updateSqlFormat, originName + "_v1" + suffix, imageName, cardId));
        if(num != 1){
          conn.rollback();
          outFail.write(cardId + ", " + originName + ",\r\n");
        }else{ 
          conn.commit();
          outSuccess.write(cardId + ", " + originName + ",\r\n");
        }
      }
    }catch(Exception e){
      e.printStackTrace();
    }finally {
      try{
        rs.close();
        stmt.close();
        conn.close();

        outSuccess.flush();
        outSuccess.close();
        outFail.flush();
        outFail.close();
      }catch(Exception e){
        e.printStackTrace();
      }
    }

    System.out.println("finished!");
  }

  public static void makeThumbnail(String srcPath, String destPath, int width, int height) throws Exception {
    IMOperation op = new IMOperation();
    op.addImage(srcPath);
    op.resize(width, height);
    op.addImage(destPath);
    
    ConvertCmd convert = new ConvertCmd();
    convert.setSearchPath(imageMagickPath);
    try{
      convert.run(op);
    }catch(IM4JavaException e){
      System.out.println("使用im4java时发生错误，searchPath = " + imageMagickPath);
      throw e;
    }
  }
}
