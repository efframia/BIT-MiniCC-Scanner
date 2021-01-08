package bit.minisys.minicc.scanner;

import java.util.ArrayList;
import java.util.HashSet;

import bit.minisys.minicc.MiniCCCfg;
import bit.minisys.minicc.internal.util.MiniCCUtil;


enum DFA_STATE{
	DFA_STATE_INITIAL,	//初始状态
	DFA_STATE_ID_0,		//标识符判定状态0，由初始字符为大小写字母作为转移到该状态的条件
	DFA_STATE_ID_1,		//标识符判定状态1，由初始字符为下划线作为转移到该状态的条件
	DFA_STATE_CON_0,	//常量判定状态0，由初始字符为数字作为转移到该状态的条件
	DFA_STATE_CON_1,	//常量判定状态1，在CON_0状态若遇到小数点则转移到该状态
	DFA_STATE_CON_2,	//常量判定状态2，由初始字符为单引号或ID_0状态遇到单引号作为转移到该状态的条件
	DFA_STATE_STR,		//字符串判定状态，由初始字符为双引号或ID_0状态遇到双引号作为转移到该状态的条件
	DFA_STATE_ADD,		//+运算符及以+为首个字符的运算符的判定状态
	DFA_STATE_MINUS,	//-运算符及以-为首个字符的运算符的判定状态
	DFA_STATE_MUL,		//*运算符及以*为首个字符的运算符的判定状态
	DFA_STATE_DIV,		//除号运算符及以除号为首个字符的运算符的判定状态
	DFA_STATE_ASSIGN,	//=运算符及以=为首个字符的运算符的判定状态
	DFA_STATE_MOD_0,	//%运算符及以%为首个字符的运算符的判定状态
	DFA_STATE_MOD_1,	//用于判定%:运算符
	DFA_STATE_MOD_2,	//用于判定%:%:运算符
	DFA_STATE_XOR,		//^运算符及以^为首个字符的运算符的判定状态
	DFA_STATE_AND,		//&运算符及以&为首个字符的运算符的判定状态
	DFA_STATE_OR,		//|运算符及以|为首个字符的运算符的判定状态
	DFA_STATE_BIT_OR,	//~运算符及以~为首个字符的运算符的判定状态
	DFA_STATE_NOT,		//!运算符及以!为首个字符的运算符的判定状态
	DFA_STATE_LESS_0,	//<运算符及以<为首个字符的运算符的判定状态
	DFA_STATE_LESS_1,	//用于判定<<=或<<运算符
	DFA_STATE_GREATER_0,//>运算符及以>为首个字符的运算符的判定状态
	DFA_STATE_GREATER_1,//用于判定>>=或>>运算符
	DFA_STATE_POUND		//#运算符及以#为首个字符的运算符的判定状态
}

public class ExampleScanner implements IMiniCCScanner {
	
	private int lIndex = 0;		//行号
	private int cIndex = 0;		//列号
	int cnt = 0;				//统计本轮判定词的结束位置
	int cnt1 = 0;				//统计本轮判定词的起始位置
	int Line = 0;				//用于补充统计换行的占位
	
    private ArrayList<String> srcLines;
    
    private HashSet<String> keywordSet;//关键字集
    
    public ExampleScanner(){
    	this.keywordSet = new HashSet<String>();
    	this.keywordSet.add("auto");
    	this.keywordSet.add("break");
    	this.keywordSet.add("case");
    	this.keywordSet.add("char");
    	this.keywordSet.add("const");
    	this.keywordSet.add("continue");
    	this.keywordSet.add("default");
    	this.keywordSet.add("do");
    	this.keywordSet.add("double");
    	this.keywordSet.add("else");
    	this.keywordSet.add("enum");
    	this.keywordSet.add("extern");
    	this.keywordSet.add("float");
    	this.keywordSet.add("for");
    	this.keywordSet.add("goto");
    	this.keywordSet.add("if");
    	this.keywordSet.add("inline");
    	this.keywordSet.add("int");
    	this.keywordSet.add("long");
    	this.keywordSet.add("register");
    	this.keywordSet.add("restrict");
    	this.keywordSet.add("return");
    	this.keywordSet.add("short");
    	this.keywordSet.add("signed");
    	this.keywordSet.add("sizeof");
    	this.keywordSet.add("static");
    	this.keywordSet.add("struct");
    	this.keywordSet.add("switch");
    	this.keywordSet.add("typedef");
    	this.keywordSet.add("union");
    	this.keywordSet.add("unsigned");
    	this.keywordSet.add("void");
    	this.keywordSet.add("volatile");
    	this.keywordSet.add("while"); 
    }

	private char getNextChar() {
		char c = Character.MAX_VALUE;
		while(true) {
			if(lIndex < this.srcLines.size()) {
				String line = this.srcLines.get(lIndex);
				if(cIndex < line.length()) {
					c = line.charAt(cIndex);
					cIndex++;
					break;
				}else {
					lIndex++;
					cIndex = 0;
				}
			}else {
				break;
			}
		}
		if(c == '\u001a') {
			c = Character.MAX_VALUE;
		}
		return c;
	}

	private boolean isAlpha(char c) {		//用于识别大小写字母
		return Character.isAlphabetic(c);
	}

	private boolean isDigit(char c) {		//用于识别数字
		return Character.isDigit(c);
	}

	private boolean isAlphaOrDigit(char c) {//用于识别大小写字母或数字
		return Character.isLetterOrDigit(c);
	}
	
	private String genToken(int num, String lexme, String type) {
		return genToken(num, lexme, type, this.cIndex - 1, this.lIndex);
	}
	private String genToken2(int num, String lexme, String type) {
		return genToken(num, lexme, type, this.cIndex - 2, this.lIndex);
	}
	private String genToken(int num, String lexme, String type, int cIndex, int lIndex) {
		String strToken = "";
		
		if(lexme.equals("else") )	{	//两个特殊情况
			lIndex--;
			cIndex = cIndex + 5;
		}else if(lexme.equals("do"))	{
			lIndex--;
			cIndex = cIndex + 3;			
		}
		if(lIndex>Line)	{				//用于检测换行
			cnt = cnt + 2;
			cnt1 = cnt1 + 2;
		}	
		//strToken += "[@" + num + "," + (cIndex - lexme.length() + 1) + ":" + cIndex;
		strToken += "[@" + num + "," + cnt1 + ":" + (cnt + lexme.length() - 1);
		//strToken += "='" + lexme + "',<" + type + ">," + (lIndex + 1) + ":" + (cIndex - lexme.length() + 1) + "]\n";
		strToken += "='" + lexme + "',<" + type + ">," + (lIndex + 1) + ":" + (cIndex - lexme.length() + 1) + "]\n";
		Line = lIndex;					//保存本轮的行号
		return strToken;
	}
	
	@Override
	public String run(String iFile) throws Exception {
		
		System.out.println("Scanning...");
		String strTokens = "";
		int iTknNum = 0;

		this.srcLines = MiniCCUtil.readFile(iFile);
        
        DFA_STATE state = DFA_STATE.DFA_STATE_INITIAL;		//FA state
		String lexme 	= "";		//token lexme
		char c 			= ' ';		//next char
		boolean keep 	= false;	//keep current char
		boolean end 	= false;
		
		while(!end) {				//scanning loop
			if(!keep) {
				c = getNextChar();
			}
			
			keep = false;

			switch(state) {
			case DFA_STATE_INITIAL:	
				lexme = "";
				cnt1 = cnt;
				
				if(isAlpha(c)) {
					state = DFA_STATE.DFA_STATE_ID_0;
					lexme = lexme + c;
				}else if(isDigit(c))	{
					state = DFA_STATE.DFA_STATE_CON_0;
					lexme = lexme + c;
				}else if(c == '+') {
					state = DFA_STATE.DFA_STATE_ADD;
					lexme = lexme + c;
				}else if(c == '-') {
					state = DFA_STATE.DFA_STATE_MINUS;
					lexme = lexme + c;
				}else if(c == '*')	{
					state = DFA_STATE.DFA_STATE_MUL;
					lexme = lexme + c;
				}else if(c == '/')	{
					state = DFA_STATE.DFA_STATE_DIV;
					lexme = lexme + c;
				}else if(c == '=')	{
					state = DFA_STATE.DFA_STATE_ASSIGN;
					lexme = lexme + c;
				}else if(c == '%')	{
					state = DFA_STATE.DFA_STATE_MOD_0;
					lexme = lexme + c;
				}else if(c == '^')	{
					state = DFA_STATE.DFA_STATE_XOR;
					lexme = lexme + c;
				}else if(c == '&')	{
					state = DFA_STATE.DFA_STATE_AND;
					lexme = lexme + c;
				}else if(c == '|')	{
					state = DFA_STATE.DFA_STATE_OR;
					lexme = lexme + c;
				}else if(c == '~')	{
					state = DFA_STATE.DFA_STATE_BIT_OR;
					lexme = lexme + c;
				}else if(c == '!')	{
					state = DFA_STATE.DFA_STATE_NOT;
					lexme = lexme + c;
				}else if(c == '<')	{
					state = DFA_STATE.DFA_STATE_LESS_0;
					lexme = lexme + c;
				}else if(c == '>')	{
					state = DFA_STATE.DFA_STATE_GREATER_0;
					lexme = lexme + c;
				}else if(c == '#')	{
					state = DFA_STATE.DFA_STATE_POUND;
					lexme = lexme + c;
				}else if(c == '?') {
					strTokens += genToken(iTknNum, "?", "'?'");
					iTknNum++;
					cnt++;
					state = DFA_STATE.DFA_STATE_INITIAL;
				}else if(c == '{') {
					strTokens += genToken(iTknNum, "{", "'{'");
					iTknNum++;
					cnt++;
					state = DFA_STATE.DFA_STATE_INITIAL;
				}else if(c == '}') {
					strTokens += genToken(iTknNum, "}", "'}'");
					iTknNum++;
					cnt++;
					state = DFA_STATE.DFA_STATE_INITIAL;
				}else if(c == '(') {
					strTokens += genToken(iTknNum, "(", "'('");
					iTknNum++;
					cnt++;
					state = DFA_STATE.DFA_STATE_INITIAL;
				}else if(c == ')') {
					strTokens += genToken(iTknNum, ")", "')'");
					iTknNum++;
					cnt++;
					state = DFA_STATE.DFA_STATE_INITIAL;
				}else if(c == '[') {
					strTokens += genToken(iTknNum, "[", "'['");
					iTknNum++;
					cnt++;
					state = DFA_STATE.DFA_STATE_INITIAL;
				}else if(c == ']') {
					strTokens += genToken(iTknNum, "]", "']'");
					iTknNum++;
					cnt++;
					state = DFA_STATE.DFA_STATE_INITIAL;
				}else if(c == ';') {
					strTokens += genToken(iTknNum, ";", "';'");
					iTknNum++;
					cnt++;
					state = DFA_STATE.DFA_STATE_INITIAL;
				}
				else if(c == ',') {
					strTokens += genToken(iTknNum, ",", "','");
					iTknNum++;
					cnt++;
					state = DFA_STATE.DFA_STATE_INITIAL;
				}else if(c == ':') {
					strTokens += genToken(iTknNum, ":", "':'");
					iTknNum++;
					cnt++;
					state = DFA_STATE.DFA_STATE_INITIAL;
				}else if(c == '…') {
					strTokens += genToken(iTknNum, "…", "'…'");
					iTknNum++;
					cnt++;
					state = DFA_STATE.DFA_STATE_INITIAL;
				}else if(c == '.') {
					strTokens += genToken(iTknNum, ".", "'.'");
					iTknNum++;
					cnt++;
					state = DFA_STATE.DFA_STATE_INITIAL;
				}else if(c == '\'')	{
					state = DFA_STATE.DFA_STATE_CON_2;
					lexme = lexme + c;					
				}else if(c == '\"')	{
					state = DFA_STATE.DFA_STATE_STR;
					lexme = lexme + c;					
				}else if(c == '_')	{
					state = DFA_STATE.DFA_STATE_ID_1;
					lexme = lexme + c;	
				}else if(Character.isWhitespace(c)) {
					cnt++;
				}else if(c == Character.MAX_VALUE) {
					cIndex = 5;
					strTokens += genToken(iTknNum, "<EOF>", "EOF");
					end = true;
				}
				break;
			case DFA_STATE_ADD:
				if(c == '+') {	
					strTokens += genToken(iTknNum, "++", "'++'");
					iTknNum++;
					cnt = cnt + 2;
					state = DFA_STATE.DFA_STATE_INITIAL;
					//keep = true;
				}else if(c == '=')	{
					strTokens += genToken(iTknNum, "+=", "'+='");
					iTknNum++;	
					cnt = cnt + 2;
					state = DFA_STATE.DFA_STATE_INITIAL;
					//keep = true;				
				}else {
					strTokens += genToken2(iTknNum, "+", "'+'");
					iTknNum++;		
					cnt++;
					state = DFA_STATE.DFA_STATE_INITIAL;
					keep = true;
				}
				state = DFA_STATE.DFA_STATE_INITIAL;
				break;		
			case DFA_STATE_MINUS:
				if(c == '-') {	
					strTokens += genToken(iTknNum, "--", "'--'");
					iTknNum++;		
					cnt = cnt + 2;
					state = DFA_STATE.DFA_STATE_INITIAL;
				}else if(c == '=')	{
					strTokens += genToken(iTknNum, "-=", "'-='");
					iTknNum++;		
					cnt = cnt + 2;
					state = DFA_STATE.DFA_STATE_INITIAL;				
				}else if(c == '>')	{
					strTokens += genToken(iTknNum, "->", "'->'");
					iTknNum++;		
					cnt = cnt + 2;
					state = DFA_STATE.DFA_STATE_INITIAL;				
				}else {
					strTokens += genToken2(iTknNum, "-", "'-'");
					iTknNum++;	
					cnt++;
					state = DFA_STATE.DFA_STATE_INITIAL;
					keep = true;
				}
				state = DFA_STATE.DFA_STATE_INITIAL;
				break;	
			case DFA_STATE_MUL:
				if(c == '=') {	
					strTokens += genToken(iTknNum, "*=", "'*='");
					iTknNum++;		
					cnt = cnt + 2;
					state = DFA_STATE.DFA_STATE_INITIAL;
				}else {
					strTokens += genToken2(iTknNum, "*", "'*'");
					iTknNum++;		
					cnt++;
					state = DFA_STATE.DFA_STATE_INITIAL;
					keep = true;
				}
				state = DFA_STATE.DFA_STATE_INITIAL;
				break;	
			case DFA_STATE_DIV:
				if(c == '=') {	
					strTokens += genToken(iTknNum, "/=", "'/='");
					iTknNum++;		
					cnt = cnt + 2;
					state = DFA_STATE.DFA_STATE_INITIAL;
				}else {
					strTokens += genToken2(iTknNum, "/", "'/'");
					iTknNum++;	
					cnt++;
					state = DFA_STATE.DFA_STATE_INITIAL;
					keep = true;
				}
				state = DFA_STATE.DFA_STATE_INITIAL;
				break;
			case DFA_STATE_ASSIGN:
				if(c == '=') {	
					strTokens += genToken(iTknNum, "==", "'=='");
					iTknNum++;	
					cnt = cnt + 2;
					state = DFA_STATE.DFA_STATE_INITIAL;
				}else {
					strTokens += genToken2(iTknNum, "=", "'='");
					iTknNum++;		
					cnt++;
					state = DFA_STATE.DFA_STATE_INITIAL;
					keep = true;
				}
				state = DFA_STATE.DFA_STATE_INITIAL;
				break;
			case DFA_STATE_MOD_0:
				if(c == '=') {	
					strTokens += genToken(iTknNum, "%=", "'%='");
					iTknNum++;		
					cnt = cnt + 2;
					state = DFA_STATE.DFA_STATE_INITIAL;
				}else if(c == '>') {	
					strTokens += genToken(iTknNum, "%>", "'%>'");
					iTknNum++;		
					cnt = cnt + 2;
					state = DFA_STATE.DFA_STATE_INITIAL;
				}else if(c == ':') {
					lexme = lexme + c;
					state = DFA_STATE.DFA_STATE_MOD_1;
					//strTokens += genToken(iTknNum, "%:", "'%:'");
					//iTknNum++;		
					//cnt = cnt + 2;
					//state = DFA_STATE.DFA_STATE_INITIAL;
				}else {
					strTokens += genToken2(iTknNum, "%", "'%'");
					iTknNum++;		
					cnt++;
					state = DFA_STATE.DFA_STATE_INITIAL;
					keep = true;
				}
				break;
			case DFA_STATE_MOD_1:
				if(c == '%')	{
					lexme = lexme + c;
					state = DFA_STATE.DFA_STATE_MOD_2;
				}else	{
					strTokens += genToken2(iTknNum, "%:", "'%:'");
					iTknNum++;		
					cnt = cnt + 2;
					state = DFA_STATE.DFA_STATE_INITIAL;
					keep = true;					
				}
				break;
			case DFA_STATE_MOD_2:
				if(c == ':')	{
					strTokens += genToken(iTknNum, "%:%:", "'%:%:'");
					iTknNum++;	
					cnt = cnt + 4;
					state = DFA_STATE.DFA_STATE_INITIAL;
				}
				break;			
			case DFA_STATE_XOR:
				if(c == '=') {	
					strTokens += genToken(iTknNum, "^=", "'^='");
					iTknNum++;	
					cnt = cnt + 2;
					state = DFA_STATE.DFA_STATE_INITIAL;
				}else {
					strTokens += genToken2(iTknNum, "^", "'^'");
					iTknNum++;	
					cnt++;
					state = DFA_STATE.DFA_STATE_INITIAL;
					keep = true;
				}
				state = DFA_STATE.DFA_STATE_INITIAL;
				break;
			case DFA_STATE_AND:
				if(c == '=') {	
					strTokens += genToken(iTknNum, "&=", "'&='");
					iTknNum++;
					cnt = cnt + 2;
					state = DFA_STATE.DFA_STATE_INITIAL;
				}else if(c == '&')	{
					strTokens += genToken(iTknNum, "&&", "'&&'");
					iTknNum++;
					cnt = cnt + 2;
					state = DFA_STATE.DFA_STATE_INITIAL;			
				}else {
					strTokens += genToken2(iTknNum, "&", "'&'");
					iTknNum++;	
					cnt++;
					state = DFA_STATE.DFA_STATE_INITIAL;
					keep = true;
				}
				state = DFA_STATE.DFA_STATE_INITIAL;
				break;	
			case DFA_STATE_OR:
				if(c == '=') {	
					strTokens += genToken(iTknNum, "|=", "'|='");
					iTknNum++;	
					cnt = cnt + 2;
					state = DFA_STATE.DFA_STATE_INITIAL;
				}else if(c == '|')	{
					strTokens += genToken(iTknNum, "||", "'||'");
					iTknNum++;		
					cnt = cnt + 2;
					state = DFA_STATE.DFA_STATE_INITIAL;			
				}else {
					strTokens += genToken2(iTknNum, "|", "'|'");
					iTknNum++;
					cnt++;
					state = DFA_STATE.DFA_STATE_INITIAL;
					keep = true;
				}
				state = DFA_STATE.DFA_STATE_INITIAL;
				break;
			case DFA_STATE_BIT_OR:
				if(c == '=') {	
					strTokens += genToken(iTknNum, "~=", "'~='");
					iTknNum++;		
					cnt = cnt + 2;
					state = DFA_STATE.DFA_STATE_INITIAL;
				}else {
					strTokens += genToken2(iTknNum, "~", "'~'");
					iTknNum++;	
					cnt++;
					state = DFA_STATE.DFA_STATE_INITIAL;
					keep = true;
				}
				state = DFA_STATE.DFA_STATE_INITIAL;
				break;
			case DFA_STATE_NOT:
				if(c == '=') {	
					strTokens += genToken(iTknNum, "!=", "'!='");
					iTknNum++;		
					cnt = cnt + 2;
					state = DFA_STATE.DFA_STATE_INITIAL;
				}else {
					strTokens += genToken2(iTknNum, "!", "'!'");
					iTknNum++;	
					cnt++;
					state = DFA_STATE.DFA_STATE_INITIAL;
					keep = true;
				}
				state = DFA_STATE.DFA_STATE_INITIAL;
				break;
			case DFA_STATE_LESS_0:
				if(c == '=') {	
					strTokens += genToken(iTknNum, "<=", "'<='");
					iTknNum++;	
					cnt = cnt + 2;
					state = DFA_STATE.DFA_STATE_INITIAL;
				}else if(c == '%') {	
					strTokens += genToken(iTknNum, "<%", "'<%'");
					iTknNum++;	
					cnt = cnt + 2;
					state = DFA_STATE.DFA_STATE_INITIAL;
				}else if(c == '<')	{
					lexme = lexme + c;
					state = DFA_STATE.DFA_STATE_LESS_1;
					//strTokens += genToken2(iTknNum, "<<", "'<<'");
					//iTknNum++;			
					//state = DFA_STATE.DFA_STATE_INITIAL;
					//keep = true;				
				}else {
					strTokens += genToken2(iTknNum, "<", "'<'");
					iTknNum++;	
					cnt++;
					state = DFA_STATE.DFA_STATE_INITIAL;
					keep = true;
				}
				break;	
			case DFA_STATE_LESS_1:
				if(c == '=') {	
					strTokens += genToken(iTknNum, "<<=", "'<<='");
					iTknNum++;	
					cnt = cnt + 3;
					state = DFA_STATE.DFA_STATE_INITIAL;
				}else {
					strTokens += genToken2(iTknNum, "<<", "'<<'");
					iTknNum++;		
					cnt = cnt + 2;
					state = DFA_STATE.DFA_STATE_INITIAL;
					keep = true;
				}
				state = DFA_STATE.DFA_STATE_INITIAL;
				break;
			case DFA_STATE_GREATER_0:
				if(c == '=') {	
					strTokens += genToken(iTknNum, ">=", "'>='");
					iTknNum++;		
					cnt = cnt + 2;
					state = DFA_STATE.DFA_STATE_INITIAL;
				}else if(c == '>')	{
					lexme = lexme + c;
					state = DFA_STATE.DFA_STATE_GREATER_1;		
				}else {
					strTokens += genToken2(iTknNum, ">", "'>'");
					iTknNum++;		
					cnt++;
					state = DFA_STATE.DFA_STATE_INITIAL;
					keep = true;
				}
				break;	
			case DFA_STATE_GREATER_1:
				if(c == '=') {	
					strTokens += genToken(iTknNum, ">>=", "'>>='");
					iTknNum++;	
					cnt = cnt + 3;
					state = DFA_STATE.DFA_STATE_INITIAL;
				}else {
					strTokens += genToken2(iTknNum, ">>", "'>>'");
					iTknNum++;	
					cnt = cnt + 2;
					state = DFA_STATE.DFA_STATE_INITIAL;
					keep = true;
				}
				state = DFA_STATE.DFA_STATE_INITIAL;
				break;
			case DFA_STATE_POUND:
				if(c == '#') {	
					strTokens += genToken(iTknNum, "##", "'##'");
					iTknNum++;		
					cnt = cnt + 2;
					state = DFA_STATE.DFA_STATE_INITIAL;
				}else {
					strTokens += genToken2(iTknNum, "#", "'#'");
					iTknNum++;	
					cnt++;
					state = DFA_STATE.DFA_STATE_INITIAL;
					keep = true;
				}
				state = DFA_STATE.DFA_STATE_INITIAL;
				break;
			case DFA_STATE_ID_0:
				if(isAlphaOrDigit(c) || c == '_') {
					lexme = lexme + c;
				}else if(c == '\'') {
					lexme = lexme + c;
					state = DFA_STATE.DFA_STATE_CON_2;
				}else if(c == '\"')	{
					lexme = lexme + c;
					state = DFA_STATE.DFA_STATE_STR;					
				}else {
					if(this.keywordSet.contains(lexme)) {
						strTokens += genToken2(iTknNum, lexme, "'" + lexme + "'");
					}else {
						strTokens += genToken2(iTknNum, lexme, "Identifier");
					}
					iTknNum++;
					cnt = cnt + lexme.length();
					state = DFA_STATE.DFA_STATE_INITIAL;
					keep = true;
				}
				break;
			case DFA_STATE_ID_1:
				if(isAlphaOrDigit(c) || c == '_')	{
					lexme = lexme + c;
				}else	{
					strTokens += genToken2(iTknNum, lexme, "Identifier");
					iTknNum++;
					cnt = cnt + lexme.length();
					state = DFA_STATE.DFA_STATE_INITIAL;
					keep = true;
				}
				break;
			case DFA_STATE_CON_0:
				if(isAlphaOrDigit(c)) {
					lexme = lexme + c;
				}else	{
					if(c == '.')
					{
						lexme = lexme + c;
						state = DFA_STATE.DFA_STATE_CON_1;
					}else	{
						strTokens += genToken2(iTknNum, lexme, "Constant");	
						iTknNum++;
						cnt = cnt + lexme.length();
						state = DFA_STATE.DFA_STATE_INITIAL;
						keep = true;
					}
				}
				break;
			case DFA_STATE_CON_1:
				if(isAlphaOrDigit(c)) {
					lexme = lexme + c;
				}else	{
					strTokens += genToken2(iTknNum, lexme, "Constant");	
					iTknNum++;
					cnt = cnt + lexme.length();
					state = DFA_STATE.DFA_STATE_INITIAL;
					keep = true;
				}
				break;
			case DFA_STATE_CON_2:
				if(c == '\'')	{
					lexme = lexme + c;
					strTokens += genToken(iTknNum, lexme, "Constant");	
					iTknNum++;
					cnt = cnt + lexme.length();
					state = DFA_STATE.DFA_STATE_INITIAL;
				}else	{
					lexme = lexme + c;
					state = DFA_STATE.DFA_STATE_CON_2;
				}
				break;
			case DFA_STATE_STR:
				if(c == '\"')	{
					lexme = lexme + c;
					strTokens += genToken(iTknNum, lexme, "StringLiteral");
					iTknNum++;
					cnt = cnt + lexme.length();
					state = DFA_STATE.DFA_STATE_INITIAL;
				}else	{
					lexme = lexme + c;
					state = DFA_STATE.DFA_STATE_STR;
				}
				break;	
			default:
				System.out.println("[ERROR]Scanner:line " + lIndex + ", column=" + cIndex + ", unreachable state!");
				break;
			}
		}
		
	
		String oFile = MiniCCUtil.removeAllExt(iFile) + MiniCCCfg.MINICC_SCANNER_OUTPUT_EXT;
		MiniCCUtil.createAndWriteFile(oFile, strTokens);
		
		return oFile;
	}

}
