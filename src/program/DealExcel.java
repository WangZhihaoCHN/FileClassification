package program;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import jxl.Sheet;
import jxl.Workbook;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class DealExcel {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//相关分类结果文件的输入输出路径
		String in = "OutputFile/excel.xls";
		String out = "OutputFile/result.xls";
		try {
			/*
			 * 创建相应的输出文件
			 * */
			OutputStream os = new FileOutputStream(out);
			//创建用于输出的excel文件
			WritableWorkbook wwb = Workbook.createWorkbook(os);	//创建xls文件
			WritableSheet ws = wwb.createSheet("DecisionSystem",0);	//设置sheet名
			//设置相关分类结果输出到excel表格中的颜色
			WritableFont wfcR = new WritableFont(WritableFont.ARIAL,10,
					WritableFont.BOLD,false,UnderlineStyle.NO_UNDERLINE,
					jxl.format.Colour.GREEN);   
	        WritableCellFormat wcfRight = new WritableCellFormat(wfcR);
	        WritableFont wfcW = new WritableFont(WritableFont.ARIAL,10,
					WritableFont.BOLD,false,UnderlineStyle.NO_UNDERLINE,
					jxl.format.Colour.RED);   
	        WritableCellFormat wcfWrong = new WritableCellFormat(wfcW);
	        WritableFont wfcN = new WritableFont(WritableFont.ARIAL,10,
					WritableFont.BOLD,false,UnderlineStyle.NO_UNDERLINE,
					jxl.format.Colour.YELLOW);   
	        WritableCellFormat wcfNull = new WritableCellFormat(wfcN);
	        WritableFont wfcBlue = new WritableFont(WritableFont.ARIAL,10,
					WritableFont.BOLD,false,UnderlineStyle.NO_UNDERLINE,
					jxl.format.Colour.BLUE);   
	        WritableCellFormat wcfBl = new WritableCellFormat(wfcBlue);	        
	        WritableFont wfcB = new WritableFont(WritableFont.ARIAL,10,
					WritableFont.BOLD,false,UnderlineStyle.NO_UNDERLINE,
					jxl.format.Colour.BLACK);   
	        WritableCellFormat wcfBlack = new WritableCellFormat(wfcB);
	        Label pen;
	        
	        /*
			 * 创建相应的输入文件操作
			 * */
	        InputStream is = new FileInputStream(in); 
            Workbook rwb = Workbook.getWorkbook(is); 
            Sheet sheet = rwb.getSheet(0);
            int col = sheet.getColumns();
            int row = sheet.getRows();
            String cell = null;
            
            //表头列说明单独格式写
            for(int i=0;i<col;i++){
            	cell = sheet.getCell(i,0).getContents(); 
            	pen = new Label(i,0,cell,wcfBlack);
				ws.addCell(pen);
            }
            //首行最后一列为决策森林的判断结果
        	pen = new Label(col,0,"Judge",wcfBlack);
			ws.addCell(pen);
            
            String real = null;	//每行数据的真实分类标签
            String judge = null;
            String[] label = new String[col-1]; //根据各棵决策树得到的分类标签
            int right = 0 , wrong = 0;
            //遍历整张excel表格
            for(int i=0;i<row;i++){
            	//跳过首行
            	if(i==0)
            		continue;
            	for(int j=0;j<col;j++){
            		if(j==0){	//表中每行的第一列为原始数据集合中的实际分类信息
            			real = sheet.getCell(j,i).getContents();
            			pen = new Label(j,i,real,wcfBl);
                		ws.addCell(pen);
            			continue;
            		}
            		label[j-1] = sheet.getCell(j,i).getContents();
            	}
            	judge = findLabel(label);
            	for(int j=1;j<col;j++){
            		cell = sheet.getCell(j,i).getContents(); 
                	if(cell.equals(real)){
                    	pen = new Label(j,i,cell,wcfRight);
                		ws.addCell(pen);
                	}else{
                    	pen = new Label(j,i,cell,wcfWrong);
                		ws.addCell(pen);
                	}
            	}
            	if(judge.equals("loss")){
                	pen = new Label(col,i,judge,wcfNull);
            		ws.addCell(pen);
            	}else if(judge.equals(real)){
            		pen = new Label(col,i,judge,wcfRight);
            		ws.addCell(pen);
            		right++;
            	}else{
            		pen = new Label(col,i,judge,wcfWrong);
            		ws.addCell(pen);
            		wrong++;
            	}
            }
            //输出结果的正确率,需要注意的是数据的总条数是行数减一（首行不是数据）
            pen = new Label(0,row+1,"正确",wcfBlack);
    		ws.addCell(pen);
    		pen = new Label(0,row+2,""+right,wcfRight);
    		ws.addCell(pen);
    		pen = new Label(1,row+1,"错误",wcfBlack);
    		ws.addCell(pen);
    		pen = new Label(1,row+2,""+wrong,wcfRight);
    		ws.addCell(pen);
    		pen = new Label(2,row+1,"未找到",wcfBlack);
    		ws.addCell(pen);
    		pen = new Label(2,row+2,""+(row-1-right-wrong),wcfRight);
    		ws.addCell(pen);
            pen = new Label(3,row+1,"正确率",wcfBlack);
    		ws.addCell(pen);
    		double ratio = (double)right/(row-1);
    		pen = new Label(3,row+2,""+ratio,wcfRight);
    		ws.addCell(pen);
            
	        wwb.write();
	        wwb.close();
	        rwb.close();
			System.out.println("处理完毕！");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	static String findLabel(String[] label){
		String judge = null;	//最终选择输出的类标签
		/*用于统计各个分类的名字，以及相应个数*/
		ArrayList<String> lab = new ArrayList<String>();
		ArrayList<Integer> count = new ArrayList<Integer>();
		boolean flag = false;
		int index = -1;
		for(int i=0;i<label.length;i++){
			flag = false;
			for(int j=0;j<lab.size();j++)
				if(label[i].equals(lab.get(j))){
					flag=true;
					index = j;
					break;
				}
			if(flag){
				count.set(index, count.get(index)+1);
			}else{
				lab.add(label[i]);
				count.add(1);
			}
		}
		//统计出现最多的分类，将其作为最终预测的分类，如果为空，不计作分类
		int num = 0;
		for(int i=0;i<lab.size();i++){
			if((count.get(i)>num)&&(!lab.get(i).equals("null"))){
				judge = lab.get(i);
				num = count.get(i);
			}
		}
		if(num==0)
			return "loss";
		else
			return judge;
	}

}
