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
		//��ط������ļ����������·��
		String in = "OutputFile/excel.xls";
		String out = "OutputFile/result.xls";
		try {
			/*
			 * ������Ӧ������ļ�
			 * */
			OutputStream os = new FileOutputStream(out);
			//�������������excel�ļ�
			WritableWorkbook wwb = Workbook.createWorkbook(os);	//����xls�ļ�
			WritableSheet ws = wwb.createSheet("DecisionSystem",0);	//����sheet��
			//������ط����������excel����е���ɫ
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
			 * ������Ӧ�������ļ�����
			 * */
	        InputStream is = new FileInputStream(in); 
            Workbook rwb = Workbook.getWorkbook(is); 
            Sheet sheet = rwb.getSheet(0);
            int col = sheet.getColumns();
            int row = sheet.getRows();
            String cell = null;
            
            //��ͷ��˵��������ʽд
            for(int i=0;i<col;i++){
            	cell = sheet.getCell(i,0).getContents(); 
            	pen = new Label(i,0,cell,wcfBlack);
				ws.addCell(pen);
            }
            //�������һ��Ϊ����ɭ�ֵ��жϽ��
        	pen = new Label(col,0,"Judge",wcfBlack);
			ws.addCell(pen);
            
            String real = null;	//ÿ�����ݵ���ʵ�����ǩ
            String judge = null;
            String[] label = new String[col-1]; //���ݸ��þ������õ��ķ����ǩ
            int right = 0 , wrong = 0;
            //��������excel���
            for(int i=0;i<row;i++){
            	//��������
            	if(i==0)
            		continue;
            	for(int j=0;j<col;j++){
            		if(j==0){	//����ÿ�еĵ�һ��Ϊԭʼ���ݼ����е�ʵ�ʷ�����Ϣ
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
            //����������ȷ��,��Ҫע��������ݵ���������������һ�����в������ݣ�
            pen = new Label(0,row+1,"��ȷ",wcfBlack);
    		ws.addCell(pen);
    		pen = new Label(0,row+2,""+right,wcfRight);
    		ws.addCell(pen);
    		pen = new Label(1,row+1,"����",wcfBlack);
    		ws.addCell(pen);
    		pen = new Label(1,row+2,""+wrong,wcfRight);
    		ws.addCell(pen);
    		pen = new Label(2,row+1,"δ�ҵ�",wcfBlack);
    		ws.addCell(pen);
    		pen = new Label(2,row+2,""+(row-1-right-wrong),wcfRight);
    		ws.addCell(pen);
            pen = new Label(3,row+1,"��ȷ��",wcfBlack);
    		ws.addCell(pen);
    		double ratio = (double)right/(row-1);
    		pen = new Label(3,row+2,""+ratio,wcfRight);
    		ws.addCell(pen);
            
	        wwb.write();
	        wwb.close();
	        rwb.close();
			System.out.println("������ϣ�");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	static String findLabel(String[] label){
		String judge = null;	//����ѡ����������ǩ
		/*����ͳ�Ƹ�����������֣��Լ���Ӧ����*/
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
		//ͳ�Ƴ������ķ��࣬������Ϊ����Ԥ��ķ��࣬���Ϊ�գ�����������
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
