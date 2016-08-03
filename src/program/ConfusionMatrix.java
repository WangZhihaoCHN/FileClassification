package program;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import jxl.Workbook;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import type.Rule;

public class ConfusionMatrix {

	public static void main(String[] args) {
		//ͳ�������ļ��������ж��ٸ������ļ������û����չ����˶��ٿ���
		String attFile = "splits/att/";
		File file = new File(attFile);
		String[] attFileName = file.list();
		int treeNum = attFileName.length;
		//��ȡ��ع����ļ��������þ������Ѿ����ɵĹ�������ά����model��
		String ruleFile = "splits/rules/rule"; 
		Rule[][] model = new Rule[treeNum][];
		for(int i=0;i<treeNum;i++){
			ArrayList<Rule> eachModel = new ArrayList<Rule>();
			String eachRuleFile = ruleFile + i +".txt";
			ruleReader(eachRuleFile,eachModel);
			Rule oneModel[] = new Rule[eachModel.size()];
			for(int j=0;j<eachModel.size();j++)
				oneModel[j] = eachModel.get(j);
			model[i] = oneModel;
		}
		
		//����������򼯺�������������
		String[][] att = new String[treeNum][];
		for(int i=0;i<treeNum;i++)
			att[i] = attReader(attFile+"att_"+i+".txt");
		//����ԭʼ�����ܹ�����������
		String[] oriAtt = attReader("InputFile/oriatt.txt");
		//�������շ���ĸ���ȡֵ
		String[] classValue = classReader("InputFile/oriatt.txt");
		//�����������ʹ�õ������ڶ�Ӧԭʼ���ݵ������±�Ϊ����
		int attIndex[][] = new int[treeNum][];
		for(int i=0;i<treeNum;i++)
			attIndex[i] = countIndex(att[i],oriAtt);
		
		//ͳ��ÿ�����ķ��࣬��ͨ��ͶƱ������շ���
		String in = "InputFile/split.txt";
		String out = "OutputFile/ForestOutput.xls";
		int matrix[][] = result(in,out,attIndex,model,classValue,oriAtt);
		//�����������
		System.out.print("\t");
		for(String s:classValue)
			System.out.print(s+"\t");
		for(int i=0;i<matrix.length;i++){
			System.out.print("\n"+classValue[i]+"\t");
			for(int j=0;j<matrix[i].length;j++)
				System.out.print(matrix[i][j]+"\t");
		}
		
	}
	
	static void ruleReader(String in, ArrayList<Rule> model){
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(in)));
			String line;
			while((line = reader.readLine())!=null){
				Rule rule = Rule.parse(line);
				model.add(rule);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static String[] attReader(String in){
		ArrayList<String> attList = new ArrayList<String>();
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(in)));
			String line;
			while((line = reader.readLine())!=null)
				attList.add(line.split(":")[0]);
			attList.remove(attList.size()-1);
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String att[] = new String[attList.size()];
		for(int i=0;i<attList.size();i++)
			att[i] = attList.get(i);
		return att;
	}
	
	static int[] countIndex(String[] now, String[] ori){
		int[] index = new int[now.length];
		for(int i=0;i<now.length;i++)
			for(int j=0;j<ori.length;j++)
				if(now[i].equals(ori[j])){
					index[i] = j;
					break;
				}

		return index;
	}
	
	/**ͳ��ԭʼ�����ļ��У����շ�������Щȡֵ
	 * @param in ԭʼ�����ļ�
	 * @return	�ַ������飬�������շ���ĸ���ȡֵ
	 */
	static String[] classReader(String in){
		String[] classValue = null;
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(in)));
			String line;
			while((line = reader.readLine())!=null){
				if(reader.readLine()==null)
					break;
			}				
			String splits[] = line.split(":");
			classValue = splits[1].split(",");
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return classValue;
	}
	
	static int getClassIndex(String classValue[], String value){
		int index = -1;
		for(int i=0;i<classValue.length;i++)
			if(classValue[i].equals(value))
				return i;
		return index;
	}
	
	/**�ú���ͨ���ۺϸ�������Ԥ�������������ͶƱȡ�����յķ��࣬���õ���Ӧ�Ļ�������
	 * @param in	����Ĳ��Լ��ļ�
	 * @param out	�������ʵ�ʷ�����жϷ����excel�ļ�
	 * @param index	��Ƭ��������ԭʼ�����±�Ķ�Ӧ��ϵ
	 * @param model	ѵ���׶β����Ĺ���ģ��
	 * @param classValue	��¼���з���ȡֵ���ַ���
	 * @return	��������
	 */
	static int[][] result(String in, String out, int[][] index, 
			Rule[][] model, String[] classValue, String[] att){
		
		//��ʼ���������󣬸�ÿ��ֵ�ó�ֵΪ0
		int[][] matrix = new int[classValue.length][classValue.length];
		for(int i=0;i<matrix.length;i++)
			for(int j=0;j<matrix[i].length;j++)
				matrix[i][j] = 0;
		
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(in)));
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
	        WritableFont wfcBl = new WritableFont(WritableFont.ARIAL,10,
					WritableFont.NO_BOLD,false,UnderlineStyle.NO_UNDERLINE,
					jxl.format.Colour.BLUE);   
	        WritableCellFormat wcfBlue = new WritableCellFormat(wfcBl);	        
	        WritableFont wfcB = new WritableFont(WritableFont.ARIAL,10,
					WritableFont.BOLD,false,UnderlineStyle.NO_UNDERLINE,
					jxl.format.Colour.BLACK);   
	        WritableCellFormat wcfBlack = new WritableCellFormat(wfcB);
	        Label pen;
	        
	        //��ͷ��˵��������ʽд
			for(int i=0;i<att.length;i++){
				pen = new Label(i,0,att[i],wcfBlack);
				ws.addCell(pen);
			}
        	pen = new Label(att.length,0,"Real",wcfBlack);
			ws.addCell(pen);
			for(int i=0;i<model.length;i++){
				pen = new Label(att.length+i+1,0,"Tree "+i,wcfBlack);
				ws.addCell(pen);
			}
			pen = new Label(att.length+model.length+1,0,"Judge",wcfBlack);
			ws.addCell(pen);
						
			String line;
			String labels[] = new String[model.length];
			boolean flag;
			int count = 0;
			//���ж�����Լ���Ϣ������ģ�ͣ�Ѱ�����Ӧ���࣬ͨ��ͶƱѡ�����շ���
			while((line = reader.readLine())!=null){
				count++;
				String[] split = line.split("\t");
				String real = split[split.length-1];
				for(int i=0;i<split.length;i++){
					pen = new Label(i,count,split[i],wcfBlue);
					ws.addCell(pen);
				}
				for(int i=0;i<model.length;i++){
					flag = false;
					for(Rule rule:model[i]){
						if(isFitRule(rule,split,index[i])){
							flag = true;
							labels[i] = rule.label;
							break;
						}
					}
					if(!flag){	//δ�ҵ������ϵĹ���
						labels[i]="null";
					}
				}
				String label = findLabel(labels);
				//�������жϵķ���д���ļ�
				for(int i=0;i<labels.length;i++){
					if(labels[i].equals(real)){
						pen = new Label(split.length+i,count,labels[i],wcfRight);
						ws.addCell(pen);
					}else{
						pen = new Label(split.length+i,count,labels[i],wcfWrong);
						ws.addCell(pen);
					}
				}
				if(label.equals("loss")){
					pen = new Label(split.length+labels.length,count,label,wcfNull);
					ws.addCell(pen);
            	}else if(label.equals(real)){
    				pen = new Label(split.length+labels.length,count,label,wcfRight);
    				ws.addCell(pen);
            	}else{
    				pen = new Label(split.length+labels.length,count,label,wcfWrong);
    				ws.addCell(pen);
            	}
				//���õ�����ؽ����ӳ������������
				int x = getClassIndex(classValue,real);
				int y = getClassIndex(classValue,label);
				if(y==-1){	//��ʾ��δ��Ԥ��������ҵ������
					System.out.print("δ�ҵ���"+count+"�ж�Ӧ�ķ��ࡪ����Ӧȡֵ:");
					for(String s:split)
						System.out.print(s+" ");
					System.out.println();
					continue;
				}
				matrix[x][y]++;
			}
			wwb.write();
            wwb.close();
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return matrix;
	}
	
	static boolean isFitRule(Rule rule, String[] values, int[] index){
		boolean satisfied = true;
	    for (Integer aid:rule.conditions.keySet()) {
	    	String cmpStr;
	    	cmpStr = values[index[aid.intValue()]];
		    if (!cmpStr.equals(rule.conditions.get(aid))){
		    	//�����ĳ�������ϣ��ü�¼Ԫ���ֵ������Ҫ���ֵ������
		    	satisfied = false;
		    	break;
		    }
	    }
	    return satisfied;
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
			if((count.get(i)>=num)&&(!lab.get(i).equals("null"))){
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
