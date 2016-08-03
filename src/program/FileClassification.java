package program;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import type.Rule;

/**
 * @author wangZhiHao
 *   本类用于根据单棵C4.5决策树生成的规则，将小测试集合每条数据根据规则判断最终分类，
 * 并统计最终结果与实际分类是否相符，计算相应的判断正确率。
 */
public class FileClassification {
	
	public static void main(String args[]){
		String ruleFile = "RuleFile/rule.txt";		//规则路径
		ArrayList<Rule> model = new ArrayList<Rule>();
		ruleReader(ruleFile,model);		//读入训练阶段产生的规则
		String[] att = attReader("InputFile/att.txt"); 	//读入规则集合包含的属性
		String[] oriAtt = attReader("InputFile/oriatt.txt");
		int[] attIndex = countIndex(att,oriAtt);	//统计规则中使用的属性在原属性集合中的下标
		double ratio = result("InputFile/split.txt","OutputFile/output.txt",attIndex,model);
		System.out.println(ratio);
	}
	
	
	/**
	 *    本函数读入训练阶段生成的规则文件，通过Rule类中定义的parse转换方法，读取所有规则到
	 * 给定的Rule类型的动态数组当中。
	 * @param in 规则文件路径
	 * @param model 用于返回的动态数组
	 */
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
	
	/** 判断一个样本记录是否符合规则要求 */
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
	
	static double result(String in, String out, int[] index, ArrayList<Rule> model){
		int right = 0, wrong = 0,total = 0;
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(in)));
			BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(out)));
			String line;
			String belong=null,label=null;
			boolean flag;
			while((line = reader.readLine())!=null){
				total++;
				String[] split = line.split("\t");
				flag = false;
				for(Rule rule:model){
					if(isFitRule(rule,split,index)){
						flag = true;
						belong = rule.label;
						//System.out.println(belong+" "+split[split.length-1]);
						if(belong.equals(split[split.length-1])){
							label = "正确！";
							right++;
						}else{
							label = "错误！";
							wrong++;
						}
						break;
					}
				}
				if(!flag){
					writer.write(line+"\t未找到\t"+"错误！");
					writer.newLine();
				}else{
					writer.write(line+"\t"+belong+"\t"+label);
					writer.newLine();
				}
			}
			reader.close();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (double)right/total;
	}
}
