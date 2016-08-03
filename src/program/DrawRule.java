package program;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import type.GraphViz;
import type.Rule;

public class DrawRule {
	public static void main(String[] args){
		String ruleFile = "RuleFile/rule.txt";	//规则路径
		ArrayList<Rule> model = new ArrayList<Rule>();
		ruleReader(ruleFile,model);		//读入训练阶段产生的规则
		String[] att = attReader("InputFile/att.txt"); 	//读入规则集合包含的属性
		String type = "gif";
		String out = "OutputFile/DrawRule.";
		drawer(type,out,model,att);
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
	
	/**
	 * @param type	输出文件的类型
	 * @param output	输出文件的地址，写到.之前，即不不需要写类型后缀
	 * @param model	决策树生成的规则文件
	 * @param att	属性的各个取值，以方便通过下标寻找对应属性名称
	 */
	static void drawer(String type, String output, 
			ArrayList<Rule> model, String[] att){
		GraphViz gv = new GraphViz();
		//开始画图
	    gv.addln(gv.start_graph());
	    //遍历规则集合，绘制决策树图
	    for(Rule rule:model){
	    	
	    	/*
	    	String ruleclass[] = rule.toString().split(":");
	    	String label = ruleclass[1];
	    	String rulevalue[] = ruleclass[0].split("&");
	    	for(int i=0;i<rulevalue.length-1;i++){
	    		//链接线的首尾
	    		String value = rulevalue[i].split(",")[1];
	    		int att1 = Integer.parseInt(rulevalue[i].split(",")[0]);
	    		String attS1 = att[att1] + i;
	    		gv.addln(attS1+"[label=\""+att[att1]+"\"]");
	    		int att2 = Integer.parseInt(rulevalue[i+1].split(",")[0]);
	    		String attS2 = att[att2] + (i+1);
	    		gv.addln(attS2+"[label=\""+att[att2]+"\"]");
	    		gv.addln(attS1+"->"+attS2+"[label=\""+value+"\"];");
	    	}
	    	//最后指向分类的单独写
	    	String l = rulevalue[rulevalue.length-1].split(",")[1];
	    	int a = Integer.parseInt(rulevalue[rulevalue.length-1].split(",")[0]);
	    	String attA = att[a] + (rulevalue.length-1);
    		gv.addln(attA+"[label=\""+att[a]+"\"]");
	    	gv.addln(attA+"->"+label+"[label=\""+l+"\"];");
	    	*/
	    }
	    
	    //画图结束
	    gv.addln(gv.end_graph());
	    //将画图的语句打印到屏幕上
	    System.out.println(gv.getDotSource());
	    File out = new File(output + type);	//Windows
	    gv.writeGraphToFile(gv.getGraph(gv.getDotSource(),type),out);
	}
}
