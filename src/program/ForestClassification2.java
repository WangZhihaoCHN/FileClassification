package program;

import type.Rule;
import jxl.Workbook;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * @author WangZhiHao
 *   �������ڸ��ݾ���ɭ���������ɵ����о�����������Ϣ����С�������ݼ��Ϸֱ������Ӧ����
 * ����Ԥ����࣬����ʵ����Ͷ�����ķ�����д��excel����У����������Ϣ��
 */
public class ForestClassification2 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//ͳ�������ļ��������ж��ٸ������ļ������û����չ����˶��ٿ���
		String attFile = "AttFile/";
		File file = new File(attFile);
		String[] attFileName = file.list();
		int treeNum = attFileName.length;
		//��ȡ��ع����ļ������Ѿ����þ��������ɵĹ�������ά����model��
		String ruleFile = "RuleFile/rule"; 
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
		//�����������ʹ�õ������ڶ�Ӧԭʼ���ݵ������±�Ϊ����
		int attIndex[][] = new int[treeNum][];
		for(int i=0;i<treeNum;i++)
			attIndex[i] = countIndex(att[i],oriAtt);
		//ͳ�ƶ�Ӧ���࣬��д��excel��
		String in = "InputFile/split.txt";
		String out = "OutputFile/ForestOut.txt";
		result(in,out,attIndex,model);
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
	
	static void result(String in, String out, int[][] index, Rule[][] model){
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(in)));
			BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(out)));
			
			OutputStream os = new FileOutputStream("OutputFile/excel.xls");;
			//����excel�ļ�
			WritableWorkbook wwb = Workbook.createWorkbook(os);	//����xls�ļ�
			WritableSheet ws = wwb.createSheet("DecisionSystem",0);	//����sheet��
			Label pen;
			//����excel�������У�ӵ�мӴ֡���ɫ�������ʽ
            WritableFont wfc = new WritableFont(WritableFont.ARIAL,10,WritableFont.BOLD,false,            		  
                    UnderlineStyle.NO_UNDERLINE,jxl.format.Colour.BLUE);   
            WritableCellFormat wcfFC = new WritableCellFormat(wfc);
			pen = new Label(0,0," Real ",wcfFC);
			ws.addCell(pen);
			for(int i=0;i<model.length;i++){
				pen = new Label(i+1,0,"Tree "+i,wcfFC);
				ws.addCell(pen);
			}
			
			String line;
			String belong=null;
			boolean flag;
			int lineNum = 0;
			
			while((line = reader.readLine())!=null){
				lineNum++;
				String[] split = line.split("\t");
				
				pen = new Label(0,lineNum,split[split.length-1]);
				ws.addCell(pen);
				
				writer.write(split[split.length-1]);
				writer.write("\t");
				for(int i=0;i<model.length;i++){
					flag = false;
					for(Rule rule:model[i]){
						if(isFitRule(rule,split,index[i])){
							flag = true;
							belong = rule.label;
							break;
						}
					}
					if(!flag){	//δ�ҵ������ϵĹ���
						pen = new Label(i+1,lineNum,"null");
						ws.addCell(pen);
						writer.write("null");
						writer.write("\t");
					}else{		//�ҵ���Ӧ��ƥ�����
						pen = new Label(i+1,lineNum,belong);
						ws.addCell(pen);
						writer.write(belong);
						writer.write("\t");
					}
				}
				writer.newLine();
			}
            wwb.write();
            wwb.close();
			reader.close();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("������ϣ��������Ӧexcel����У�");
		return;
	}
}
