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
		String ruleFile = "RuleFile/rule.txt";	//����·��
		ArrayList<Rule> model = new ArrayList<Rule>();
		ruleReader(ruleFile,model);		//����ѵ���׶β����Ĺ���
		String[] att = attReader("InputFile/att.txt"); 	//������򼯺ϰ���������
		String type = "gif";
		String out = "OutputFile/DrawRule.";
		drawer(type,out,model,att);
	}
	
	/**
	 *    ����������ѵ���׶����ɵĹ����ļ���ͨ��Rule���ж����parseת����������ȡ���й���
	 * ������Rule���͵Ķ�̬���鵱�С�
	 * @param in �����ļ�·��
	 * @param model ���ڷ��صĶ�̬����
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
	 * @param type	����ļ�������
	 * @param output	����ļ��ĵ�ַ��д��.֮ǰ����������Ҫд���ͺ�׺
	 * @param model	���������ɵĹ����ļ�
	 * @param att	���Եĸ���ȡֵ���Է���ͨ���±�Ѱ�Ҷ�Ӧ��������
	 */
	static void drawer(String type, String output, 
			ArrayList<Rule> model, String[] att){
		GraphViz gv = new GraphViz();
		//��ʼ��ͼ
	    gv.addln(gv.start_graph());
	    //�������򼯺ϣ����ƾ�����ͼ
	    for(Rule rule:model){
	    	
	    	/*
	    	String ruleclass[] = rule.toString().split(":");
	    	String label = ruleclass[1];
	    	String rulevalue[] = ruleclass[0].split("&");
	    	for(int i=0;i<rulevalue.length-1;i++){
	    		//�����ߵ���β
	    		String value = rulevalue[i].split(",")[1];
	    		int att1 = Integer.parseInt(rulevalue[i].split(",")[0]);
	    		String attS1 = att[att1] + i;
	    		gv.addln(attS1+"[label=\""+att[att1]+"\"]");
	    		int att2 = Integer.parseInt(rulevalue[i+1].split(",")[0]);
	    		String attS2 = att[att2] + (i+1);
	    		gv.addln(attS2+"[label=\""+att[att2]+"\"]");
	    		gv.addln(attS1+"->"+attS2+"[label=\""+value+"\"];");
	    	}
	    	//���ָ�����ĵ���д
	    	String l = rulevalue[rulevalue.length-1].split(",")[1];
	    	int a = Integer.parseInt(rulevalue[rulevalue.length-1].split(",")[0]);
	    	String attA = att[a] + (rulevalue.length-1);
    		gv.addln(attA+"[label=\""+att[a]+"\"]");
	    	gv.addln(attA+"->"+label+"[label=\""+l+"\"];");
	    	*/
	    }
	    
	    //��ͼ����
	    gv.addln(gv.end_graph());
	    //����ͼ������ӡ����Ļ��
	    System.out.println(gv.getDotSource());
	    File out = new File(output + type);	//Windows
	    gv.writeGraphToFile(gv.getGraph(gv.getDotSource(),type),out);
	}
}
