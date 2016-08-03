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
 *   �������ڸ��ݵ���C4.5���������ɵĹ��򣬽�С���Լ���ÿ�����ݸ��ݹ����ж����շ��࣬
 * ��ͳ�����ս����ʵ�ʷ����Ƿ������������Ӧ���ж���ȷ�ʡ�
 */
public class FileClassification {
	
	public static void main(String args[]){
		String ruleFile = "RuleFile/rule.txt";		//����·��
		ArrayList<Rule> model = new ArrayList<Rule>();
		ruleReader(ruleFile,model);		//����ѵ���׶β����Ĺ���
		String[] att = attReader("InputFile/att.txt"); 	//������򼯺ϰ���������
		String[] oriAtt = attReader("InputFile/oriatt.txt");
		int[] attIndex = countIndex(att,oriAtt);	//ͳ�ƹ�����ʹ�õ�������ԭ���Լ����е��±�
		double ratio = result("InputFile/split.txt","OutputFile/output.txt",attIndex,model);
		System.out.println(ratio);
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
	
	/** �ж�һ��������¼�Ƿ���Ϲ���Ҫ�� */
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
							label = "��ȷ��";
							right++;
						}else{
							label = "����";
							wrong++;
						}
						break;
					}
				}
				if(!flag){
					writer.write(line+"\tδ�ҵ�\t"+"����");
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
