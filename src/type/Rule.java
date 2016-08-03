package type;
import java.util.HashMap;
import java.util.Map;

/** ��������һ���ڵ��Ӧ�ľ��߹��� */
public class Rule {
	// �þ��߹�����������е�����
	// conditions��Key������ID����1��ʼ��ţ���value�Ǹ�����ȡֵ
	// ���conditions����{<1,middle>,<3,no>}�ͱ�ʾ
	// "����1 == middle && ����3 == no"�������
	public Map<Integer, String> conditions = new HashMap<Integer, String>();
	// �ù�������ָ���label,�����""��ʾ��ʱû��ȷ�����������û�й�����ȫ
	public String label = "";

	// ���ù��������������ݵĸ�ʽ���£�
	// "condition:label"����������conditionʱ�ù������label
	// condition���������ʽ��֯��aid1,avalue1&aid2,avalue2&...
	// aid1��ʾ�����е�һ�����Ե�id��avalue1��ʾ�ڸ������ϵ�ȡֵ��
	// �������֮��ʹ��'&'�ָ���
	// ����������ʾ�ˣ�aid1 == avalue1) && (aid2 == avalue2)�������
	public String toString() {
		StringBuilder str = new StringBuilder();
		// �����������
	    for (Integer aid : conditions.keySet()) {
	      str.append(aid.toString() + "," + conditions.get(aid) + "&");
	    }
	    // ��strĩβ��&�滻Ϊlabel�ָ���':'
	    str.setCharAt(str.length() - 1, ':');
	    // ���label
	    str.append(this.label);
	    return str.toString();
	}

	// ����һ���ı��������������
	// �����ı��ĸ�ʽ��toString()������ע��
	public static Rule parse(String source){
		Rule rule = new Rule();
		if (source.length() <= 1)
			return null;// ֻ��һ���ַ���Ӧ���ǡ�������������Ч��
		if (source.charAt(0) == ':' && source.length() > 1) {
			// ˵��û��������ֻ�б�ǩ
			rule.label = source.split(":")[1];
			return rule;
		}
		String conditionPart = source.split(":")[0];
		for (String condition : conditionPart.split("\\&")) {
			String aid = condition.split("\\,")[0];
			String value = condition.split("\\,")[1];
			rule.conditions.put(new Integer(aid), value);
		}
		// ��������ǩ���ȡ��ǩ
		if (source.split(":").length == 2)
			rule.label = source.split(":")[1];
		return rule;
	}
}
