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
 *   本类用于根据决策森林最终生成的所有决策树规则信息，将小测试数据集合分别根据相应规则
 * 进行预测分类，将真实分类和多棵树的分类结果写入excel表格中，保存相关信息。
 */
public class ForestClassification2 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//统计属性文件夹下面有多少个属性文件，即用户最终构建了多少棵树
		String attFile = "AttFile/";
		File file = new File(attFile);
		String[] attFileName = file.list();
		int treeNum = attFileName.length;
		//读取相关规则文件，将已经几棵决策树生成的规则读入二维数组model中
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
		//读入各个规则集合所包含的属性
		String[][] att = new String[treeNum][];
		for(int i=0;i<treeNum;i++)
			att[i] = attReader(attFile+"att_"+i+".txt");
		//读入原始数据总共包含的属性
		String[] oriAtt = attReader("InputFile/oriatt.txt");
		//计算规则中所使用的属性在对应原始数据的属性下标为多少
		int attIndex[][] = new int[treeNum][];
		for(int i=0;i<treeNum;i++)
			attIndex[i] = countIndex(att[i],oriAtt);
		//统计对应分类，并写入excel中
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
		    	//如果在某个属性上，该记录元组的值与条件要求的值不符合
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
			//创建excel文件
			WritableWorkbook wwb = Workbook.createWorkbook(os);	//创建xls文件
			WritableSheet ws = wwb.createSheet("DecisionSystem",0);	//设置sheet名
			Label pen;
			//设置excel表格的首行，拥有加粗、蓝色的字体格式
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
					if(!flag){	//未找到所符合的规则
						pen = new Label(i+1,lineNum,"null");
						ws.addCell(pen);
						writer.write("null");
						writer.write("\t");
					}else{		//找到相应的匹配规则
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
		System.out.println("处理完毕，结果在相应excel表格中！");
		return;
	}
}
