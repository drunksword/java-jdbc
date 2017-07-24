import java.sql.*;
import java.util.*;
import java.io.*;
import java.lang.Exception;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.core.IdentifyCmd;

public class DealCardImg200 {
  private static final String db = "jdbc:sqlserver://zb.clschina.com:6433;DatabaseName=namecard";
  private static final String username = "sa";
  private static final String password = "u8soft";

  private static final String imageMagickPath = "C:/Program Files (x86)/ImageMagick-6.9.0-Q16";

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
      File folder = new File("D:/workspace/dashangapi/files/cardimage");
      File successFile = new File("successLog.txt");
      File errorFile = new File("errorLog.txt");
      outSuccess = new BufferedWriter(new FileWriter(successFile, true));
      outFail = new BufferedWriter(new FileWriter(errorFile, true));

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
        File originFile = new File(folder, originName + "_cut" + suffix);
        if(!originFile.exists()){
          outFail.write(cardId + ", " + originName + ", not exists \r\n");
          continue;
        }
        
        File newFile = new File(folder, originName + "_v1_cut" + suffix);
        makeThumbnail(originFile.getPath(), newFile.getPath(), 200, 200);

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
