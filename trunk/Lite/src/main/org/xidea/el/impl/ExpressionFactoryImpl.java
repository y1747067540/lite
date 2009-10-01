package org.xidea.el.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.xidea.el.OperationStrategy;
import org.xidea.el.Expression;
import org.xidea.el.ExpressionFactory;
import org.xidea.el.ExpressionSyntaxException;
import org.xidea.el.ExpressionToken;
import org.xidea.el.fn.ECMA262Impl;
import org.xidea.el.parser.ExpressionTokenizer;
import org.xidea.el.parser.TokenImpl;

public class ExpressionFactoryImpl implements ExpressionFactory {
	public static final OperationStrategy DEFAULT_CALCULATER = new OperationStrategyImpl();
	public static final Map<String, Object> DEFAULT_GLOBAL_MAP;
	private static ExpressionFactoryImpl expressionFactory = new ExpressionFactoryImpl();
	protected final Map<String, Object> globals;
	static {
		HashMap<String, Object> map = new HashMap<String, Object>();
		ECMA262Impl.setup(map);
		DEFAULT_GLOBAL_MAP = Collections.unmodifiableMap(map);
	}

	public static ExpressionFactory getInstance() {
		return expressionFactory;
	}

	public ExpressionFactoryImpl() {
		this(DEFAULT_GLOBAL_MAP);
	}

	public ExpressionFactoryImpl(Map<String, Object> globals) {
		this.globals = globals;
	}

	@SuppressWarnings("unchecked")
	public Expression create(Object elo) {
		if (elo instanceof String) {
			return new ExpressionImpl((String) elo);
		} else {
			ExpressionToken el;
//			if (elo instanceof ExpressionToken) {
//				el = (ExpressionToken) elo;
//			} else 
			{
				el = TokenImpl.toToken((List<Object>) elo);
			}

			return getOptimizedExpression(el);

		}
	}

	private Expression getOptimizedExpression(ExpressionToken el) {
		Expression ressult = OptimizeExpressionImpl.create(el,
				DEFAULT_CALCULATER, globals);
		return ressult != null ? ressult : new ExpressionImpl(null, el,
				DEFAULT_CALCULATER, globals);
	}

	private String simpleCheckEL(String expression) {
		// check it
		expression = expression.trim();
		StringBuilder buf = new StringBuilder();
		buf.append((char) 0);
		for (int i = 0; i < expression.length(); i++) {
			char c = expression.charAt(i);
			switch (c) {
			case '\'':// 39//100111//
			case '"': // 34//100010//
				sub: for (i++; i < expression.length(); i++) {
					char c2 = expression.charAt(i);
					switch (c2) {
					case '\\':
						i++;
						break;
					case '\'':// 39//100111//
					case '"': // 34//100010//
						if (c2 == c) {
							c = 0;
							break sub;
						}
						break;
					case '\r':
						break sub;// error
					case '\n':// \\r\n
						if (expression.charAt(i - 1) != '\r') {
							break sub;// error
						}
					}
				}
				if (c == 0) {
					break;
				} else {
					throw new ExpressionSyntaxException("unclosed string at"
							+ i + ":" + expression);
				}
			case '{':// 123//1111011
			case '[':// 91 //1011011
			case '(':// 40 // 101000
				buf.append(c);
				break;
			case ')':// 41 // 101001
			case ']':// 93 //1011101
			case '}':// 125//1111101
				int offset = c - buf.charAt(buf.length() - 1);
				if (offset > 0 && offset < 3) {// [1,2]
					buf.deleteCharAt(buf.length() - 1);
				} else {
					throw new ExpressionSyntaxException("expression error at"
							+ i + ":" + expression);
				}
			}
		}
		if (buf.length() != 1) {
			throw new ExpressionSyntaxException("expression error : " + buf);
		}
		return expression;
	}

	public Object parse(String el) {
		simpleCheckEL(el);
		ExpressionToken tokens = new ExpressionTokenizer(el).getResult();
		return tokens;
	}
//
//	private void check(ExpressionToken[] list) {
//		int index = list.length;
//		int pos = 0;
//		while (index-- > 0) {
//			ExpressionToken item = list[index];
//			int type = item.getType();
//			if (type > 0) {
//				if((type & ExpressionToken.BIT_PARAM) > 0){
//					pos --;
//				}
//			} else {
//				pos++;
//			}
//		}
//		if (pos != 1) {
//			throw new ExpressionSyntaxException("表达式最终计算结果数不为1");
//		}
//	}
}