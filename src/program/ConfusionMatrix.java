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
		//统计属性文件夹下面有多少个属性文件，即用户最终构建了多少棵树
		String attFile = "splits/att/";
		File file = new File(attFile);
		String[] attFileName = file.list();
		int treeNum = attFileName.length;
		//读取相关规则文件，将几棵决策树已经生成的规则读入二维数组model中
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
		
		//读入各个规则集合所包含的属性
		String[][] att = new String[treeNum][];
		for(int i=0;i<treeNum;i++)
			att[i] = attReader(attFile+"att_"+i+".txt");
		//读入原始数据总共包含的属性
		String[] oriAtt = attReader("InputFile/oriatt.txt");
		//读入最终分类的各个取值
		String[] classValue = classReader("InputFile/oriatt.txt");
		//计算规则中所使用的属性在对应原始数据的属性下标为多少
		int attIndex[][] = new int[treeNum][];
		for(int i=0;i<treeNum;i++)
			attIndex[i] = countIndex(att[i],oriAtt);
		
		//统计每棵树的分类，并通过投票获得最终分类
		String in = "InputFile/split.txt";
		String out = "OutputFile/ForestOutput.xls";
		int matrix[][] = result(in,out,attIndex,model,classValue,oriAtt);
		//输出混淆矩阵
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
	
	/**统计原始属性文件中，最终分类有哪些取值
	 * @param in 原始属性文件
	 * @return	字符串数组，保存最终分类的各个取值
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
	
	/**该函数通过综合各棵树的预测分类结果，进行投票取得最终的分类，并得到相应的混淆矩阵
	 * @param in	读入的测试集文件
	 * @param out	用于输出实际分类和判断分类的excel文件
	 * @param index	分片的属性与原始属性下标的对应关系
	 * @param model	训练阶段产生的规则模型
	 * @param classValue	记录所有分类取值的字符串
	 * @return	混淆矩阵
	 */
	static int[][] result(String in, String out, int[][] index, 
			Rule[][] model, String[] classValue, String[] att){
		
		//初始化混淆矩阵，赋每个值得初值为0
		int[][] matrix = new int[classValue.length][classValue.length];
		for(int i=0;i<matrix.length;i++)
			for(int j=0;j<matrix[i].length;j++)
				matrix[i][j] = 0;
		
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(in)));
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
	        WritableFont wfcBl = new WritableFont(WritableFont.ARIAL,10,
					WritableFont.NO_BOLD,false,UnderlineStyle.NO_UNDERLINE,
					jxl.format.Colour.BLUE);   
	        WritableCellFormat wcfBlue = new WritableCellFormat(wfcBl);	        
	        WritableFont wfcB = new WritableFont(WritableFont.ARIAL,10,
					WritableFont.BOLD,false,UnderlineStyle.NO_UNDERLINE,
					jxl.format.Colour.BLACK);   
	        WritableCellFormat wcfBlack = new WritableCellFormat(wfcB);
	        Label pen;
	        
	        //表头列说明单独格式写
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
			//按行读入测试集信息，查找模型，寻找其对应分类，通过投票选择最终分类
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
					if(!flag){	//未找到所符合的规则
						labels[i]="null";
					}
				}
				String label = findLabel(labels);
				//将最终判断的分类写入文件
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
				//将得到的相关结果反映到混淆矩阵中
				int x = getClassIndex(classValue,real);
				int y = getClassIndex(classValue,label);
				if(y==-1){	//表示并未在预测过程中找到其分类
					System.out.print("未找到第"+count+"行对应的分类——对应取值:");
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
		    	//如果在某个属性上，该记录元组的值与条件要求的值不符合
		    	satisfied = false;
		    	break;
		    }
	    }
	    return satisfied;
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
